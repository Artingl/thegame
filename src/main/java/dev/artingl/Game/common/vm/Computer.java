package dev.artingl.Game.common.vm;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.timer.TickListener;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Game.Constants;
import dev.artingl.Game.common.vm.components.ComputerComponent;
import dev.artingl.Game.common.vm.components.UARTDevice;
import li.cil.sedna.api.device.MemoryMappedDevice;
import li.cil.sedna.buildroot.Buildroot;
import li.cil.sedna.memory.MemoryMaps;
import li.cil.sedna.riscv.R5Board;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class Computer implements TickListener, Runnable {

    private final Engine engine;
    private final List<ComputerComponent> components;
    private final R5Board board;

    private final AtomicInteger timeQuotaInMillis = new AtomicInteger();
    private Future<?> lastSchedule;
    private long cycleLimit, cycles;

    public Computer(ComputerComponent ...components) {
        this.components = new ArrayList<>();
        this.engine = Engine.getInstance();
        this.components.addAll(Arrays.asList(components));
        this.board = new R5Board();
    }

    /**
     * Get collection of components in the computer
     * */
    public Collection<ComputerComponent> getComponents() {
        return components;
    }

    /**
     * Turn on the computer
     * */
    public void turnOn() {
        if (!this.board.isRunning()) {
            this.board.reset();
            this.board.setRunning(true);
        }
    }

    /**
     * Turn off the computer
     * */
    public void turnOff() {
        if (this.board.isRunning()) {
            this.board.reset();
            this.board.setRunning(false);
        }
    }

    /**
     * Is the computer currently running
     * */
    public boolean isRunning() {
        return this.board.isRunning();
    }

    /**
     * Reboots the computer
     * */
    public void reboot() {
        this.board.reset();

        for (ComputerComponent component: this.components)
            component.reset();
    }

    public void init() throws EngineException {
        try {
            MemoryMappedDevice uart = null;

            /* Initialize all components */
            for (ComputerComponent component : this.components) {
                MemoryMappedDevice dev = component.getDevice(this, board);
                if (dev == null)
                    continue;

                if (component instanceof UARTDevice) {
                    if (uart == null)
                        uart = dev;
                }

                this.board.addDevice(dev);
            }
            if (uart != null)
                this.board.setStandardOutputDevice(uart);

            /* Initialize CPU */
            this.board.getCpu().setFrequency(Constants.CPU_FREQUENCY);
            this.board.setBootArguments("root=/dev/vda rw");
            this.board.reset();
            this.board.initialize();
            this.board.setRunning(false);

            /* Load the firmware and linux images
             * TODO: maybe custom firmware and images? */
            long startAddress = this.board.getDefaultProgramStart();
            MemoryMaps.store(this.board.getMemoryMap(), startAddress, Buildroot.getFirmware());
            MemoryMaps.store(this.board.getMemoryMap(), startAddress + 0x200000, Buildroot.getLinuxImage());
        } catch (IOException e) {
            this.engine.getLogger().exception(e, "Unable to initialize the R5 CPU");
        }

        this.engine.getTimer().subscribe(this);
    }

    public void cleanup() {
        this.engine.getTimer().unsubscribe(this);
        this.turnOff();

        if (this.lastSchedule != null)
            this.lastSchedule.cancel(true);

        for (ComputerComponent component: this.components) {
            component.cleanup();
        }

        this.components.clear();
    }

    /**
     * Return instance of a component in the computer. If no components found, the result is null.
     *
     * @param type The class of the target component
     * */
    @Nullable
    public <T> T getDevice(Class<? extends ComputerComponent> type) {
        for (ComputerComponent component: components) {
            if (component.getClass().getName().equals(type.getName()) || type.isAssignableFrom(component.getClass())) {
                return (T) component;
            }
        }

        return null;
    }

    @Override
    public void tick(Timer timer) {
        this.cycleLimit = getCyclesPerTick();

        int timeSlice = (int) (500 / timer.getTickPerSecond());
        int timeQuota = timeQuotaInMillis.updateAndGet(x -> Math.min(x + timeSlice, timeSlice));
        boolean needsScheduling = lastSchedule == null || lastSchedule.isDone() || lastSchedule.isCancelled();
        if (cycleLimit > 0 && timeQuota > 0 && needsScheduling) {
            this.lastSchedule = engine.getThreadsManager().submit(this);
        }
    }

    @Override
    public void run() {
        do {
            long start = System.currentTimeMillis();

            int cycleBudget = getCyclesPerTick();
            int cyclesPerStep = 1_000;
            int maxSteps = cycleBudget / cyclesPerStep;

            if (!board.isRunning()) {
                break;
            }

            for (int i = 0; i < maxSteps; i++) {
                this.cycles += cyclesPerStep;
                this.board.step(cyclesPerStep);

                for (ComputerComponent component: components)
                    component.step();

                if (System.currentTimeMillis() - start > timeQuotaInMillis.get()) {
                    break;
                }
            }

            int elapsed = (int) (System.currentTimeMillis() - start);
            this.timeQuotaInMillis.addAndGet(-elapsed);
        } while (cycles < cycleLimit && timeQuotaInMillis.get() > 0);
    }

    private int getCyclesPerTick() {
        return Constants.CPU_FREQUENCY / 128;
    }
}

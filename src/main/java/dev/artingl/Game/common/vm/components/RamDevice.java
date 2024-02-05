package dev.artingl.Game.common.vm.components;

import dev.artingl.Game.common.vm.Computer;
import dev.artingl.Game.common.vm.VMException;
import li.cil.sedna.api.Board;
import li.cil.sedna.api.device.MemoryMappedDevice;
import li.cil.sedna.api.device.PhysicalMemory;
import li.cil.sedna.device.memory.Memory;

public class RamDevice implements ComputerComponent {

    private final PhysicalMemory memory;

    public RamDevice(int capacity) {
        this.memory = Memory.create(capacity);
    }

    @Override
    public String getName() {
        return "Memory Device";
    }

    @Override
    public MemoryMappedDevice getDevice(Computer computer, Board board) {
        return memory;
    }

    @Override
    public void cleanup() throws VMException {
        try {
            this.memory.close();
        } catch (Exception e) {
            throw new VMException(e);
        }
    }

    @Override
    public void step() {

    }

    @Override
    public void reset() {

    }

    public int getCapacity() {
        return memory.getLength();
    }

}

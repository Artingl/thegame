package dev.artingl.Game.common.vm.components;

import dev.artingl.Game.Constants;
import dev.artingl.Game.common.vm.Computer;
import dev.artingl.Game.common.vm.VMException;
import li.cil.sedna.api.Board;
import li.cil.sedna.api.device.MemoryMappedDevice;
import li.cil.sedna.device.serial.UART16550A;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class UARTDevice implements ComputerComponent {
    private UART16550A uart;
    private final ByteBuffer receiveBuffer;
    private final ByteBuffer transmitBuffer;

    public UARTDevice() {
        this.uart = new UART16550A();
        this.receiveBuffer = ByteBuffer.allocate(4 * Constants.KILOBYTE);
        this.transmitBuffer = ByteBuffer.allocate(4 * Constants.KILOBYTE);
    }

    @Override
    public String getName() {
        return "Terminal";
    }

    @Override
    public void cleanup() throws VMException {
        this.uart.reset();
    }

    @Override
    public void step() {
        int value;
        while ((value = uart.read()) != -1) {
            if (receiveBuffer.hasRemaining()) {
                this.receiveBuffer.put((byte) value);
            }
        }
        this.receiveBuffer.flip();
    }

    public byte[] getReceiveBuffer() {
        byte[] data = receiveBuffer.array();
        int len = receiveBuffer.remaining();
        this.receiveBuffer.clear();
        return Arrays.copyOfRange(data, 0, len);
    }


    @Override
    public void reset() {
        this.uart.reset();
        this.receiveBuffer.rewind();
        this.receiveBuffer.clear();
    }

    @Override
    public MemoryMappedDevice getDevice(Computer computer, Board board) {
        return this.uart;
    }

}

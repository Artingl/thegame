package dev.artingl.Game.common.vm.components;

import dev.artingl.Game.common.vm.Computer;
import dev.artingl.Game.common.vm.VMException;
import li.cil.sedna.api.Board;
import li.cil.sedna.api.device.MemoryMappedDevice;

public interface ComputerComponent {

    String getName();
    MemoryMappedDevice getDevice(Computer computer, Board board);
    void cleanup() throws VMException;
    void step();
    void reset();


}

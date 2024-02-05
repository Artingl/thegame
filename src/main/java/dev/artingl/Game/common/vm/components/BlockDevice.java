package dev.artingl.Game.common.vm.components;

import dev.artingl.Engine.resources.Resource;
import dev.artingl.Game.common.vm.Computer;
import dev.artingl.Game.common.vm.VMException;
import li.cil.sedna.api.Board;
import li.cil.sedna.api.device.MemoryMappedDevice;
import li.cil.sedna.device.block.ByteBufferBlockDevice;
import li.cil.sedna.device.virtio.VirtIOBlockDevice;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BlockDevice implements ComputerComponent {

    private final li.cil.sedna.api.device.BlockDevice blk;
    private final InputStream stream;
    private VirtIOBlockDevice vfs;

    public BlockDevice(int capacity) throws IOException {
        this(new ByteArrayInputStream(new byte[capacity]));
    }

    public BlockDevice(Resource source) throws IOException {
        this(source.load());
    }

    public BlockDevice(InputStream stream) throws IOException {
        try {
            this.stream = stream;
            this.blk = ByteBufferBlockDevice.createFromStream(stream, false);
        } catch (IOException e) {
            throw new VMException(e);
        }
    }

    public li.cil.sedna.api.device.BlockDevice getBlockDevice() {
        return blk;
    }

    public long getCapacity() {
        return blk.getCapacity();
    }

    @Override
    public String getName() {
        return "HardDrive";
    }

    @Override
    public void cleanup() throws VMException {
        try {
            this.blk.close();
            this.stream.close();
            if (vfs != null)
                this.vfs.close();
        } catch (IOException e) {
            throw new VMException(e);
        }
    }

    @Override
    public void step() {
    }

    @Override
    public void reset() {
    }

    @Override
    public MemoryMappedDevice getDevice(Computer computer, Board board) {
        if (vfs == null) {
            this.vfs = new VirtIOBlockDevice(board.getMemoryMap(), blk);
            this.vfs.getInterrupt().set(0x10, board.getInterruptController());
        }
        return vfs;
    }

}

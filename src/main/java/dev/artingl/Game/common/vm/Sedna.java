package dev.artingl.Game.common.vm;

import li.cil.sedna.api.device.InterruptSource;
import li.cil.sedna.api.device.MemoryMappedDevice;
import li.cil.sedna.api.device.PhysicalMemory;
import li.cil.sedna.device.flash.FlashMemoryDevice;
import li.cil.sedna.device.rtc.GoldfishRTC;
import li.cil.sedna.device.serial.UART16550A;
import li.cil.sedna.device.syscon.AbstractSystemController;
import li.cil.sedna.device.virtio.AbstractVirtIODevice;
import li.cil.sedna.device.virtio.VirtIOBlockDevice;
import li.cil.sedna.devicetree.DeviceTreeRegistry;
import li.cil.sedna.devicetree.provider.*;
import li.cil.sedna.riscv.device.R5CoreLocalInterrupter;
import li.cil.sedna.riscv.device.R5PlatformLevelInterruptController;
import li.cil.sedna.riscv.devicetree.R5CoreLocalInterrupterProvider;
import li.cil.sedna.riscv.devicetree.R5PlatformLevelInterruptControllerProvider;

public class Sedna {

    public static void init() {
        DeviceTreeRegistry.putProvider(FlashMemoryDevice.class, new FlashMemoryProvider());
        DeviceTreeRegistry.putProvider(GoldfishRTC.class, new GoldfishRTCProvider());
        DeviceTreeRegistry.putProvider(InterruptSource.class, new InterruptSourceProvider());
        DeviceTreeRegistry.putProvider(MemoryMappedDevice.class, new MemoryMappedDeviceProvider());
        DeviceTreeRegistry.putProvider(PhysicalMemory.class, new PhysicalMemoryProvider());
        DeviceTreeRegistry.putProvider(AbstractSystemController.class, new SystemControllerProvider());
        DeviceTreeRegistry.putProvider(UART16550A.class, new UART16550AProvider());
        DeviceTreeRegistry.putProvider(AbstractVirtIODevice.class, new VirtIOProvider());
        DeviceTreeRegistry.putProvider(R5CoreLocalInterrupter.class, new R5CoreLocalInterrupterProvider());
        DeviceTreeRegistry.putProvider(R5PlatformLevelInterruptController.class, new R5PlatformLevelInterruptControllerProvider());
    }

}

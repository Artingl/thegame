package dev.artingl.Game.common.vm.components;

import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class TerminalDevice extends UARTDevice {

    private final int width;
    private final int height;
    private final BufferedImage firstBuffer;
    private final BufferedImage secondBuffer;
    private String buffer;

    public TerminalDevice(int width, int height) {
        this.width = width;
        this.height = height;
        this.firstBuffer = new BufferedImage(width * 10, height * 12, BufferedImage.TYPE_INT_RGB);
        this.secondBuffer = new BufferedImage(width * 10, height * 12, BufferedImage.TYPE_INT_RGB);
    }

    /**
     * Get image of the computers screen buffer
     */
    @Nullable
    public BufferedImage getFramebufferImage() {
        return secondBuffer;
    }

    @Override
    public void step() {
        super.step();

        byte[] data = getReceiveBuffer();
        if (data.length == 0)
            return;

        this.buffer += new String(data);
        this.buffer = this.buffer.substring(Math.max(0, buffer.length() - (width * height)));

        Graphics graphics = this.firstBuffer.getGraphics();
        graphics.clearRect(0, 0, firstBuffer.getWidth(), firstBuffer.getHeight());

        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Consolas", Font.BOLD, 12));

        int offset = 0;
        String[] lines = this.buffer.split("\n");
        lines = Arrays.copyOfRange(lines, Math.max(0, lines.length - height), lines.length);
        for (String line : lines) {
            graphics.drawString(line, 0, offset);
            offset += 12;
        }

        this.secondBuffer.getGraphics().drawImage(
                this.firstBuffer, 0, 0,
                firstBuffer.getWidth(), firstBuffer.getHeight(),
                null);
    }
}

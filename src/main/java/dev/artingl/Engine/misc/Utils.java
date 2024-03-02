package dev.artingl.Engine.misc;

import dev.artingl.Engine.input.Input;
import org.joml.*;
import org.lwjgl.BufferUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Random;

import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.system.MemoryUtil.memSlice;

public class Utils {

    public static String getExceptionDetails(Throwable e) {
        StringBuilder sb = new StringBuilder();
        sb.append("\t").append(e.toString()).append("\n");
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\t\t").append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    public static String getCallerInfo(int depth) {
        StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
        StackTraceElement e = stacktrace[3 + depth];
        return e.getClassName() + "@" + e.getMethodName();
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     * The difference between min and max can be at most
     *
     * @param min Minimum value
     * @param max Maximum value.  Must be greater than min.
     * @return Integer between min and max, inclusive.
     * @see java.util.Random#nextInt(int)
     */
    public static int randInt(int min, int max) {
        return randInt(new Random(), min, max);
    }

    public static int randInt(Random random, int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    public static int randInt() {
        return randInt(-0xfffff, 0xfffff);
    }

    public static float[] wrapVector(Vector3f v) {
        return new float[]{v.x, v.y, v.z};
    }

    public static double[] wrapVector(Vector3d v) {
        return new double[]{v.x, v.y, v.z};
    }

    public static int[] wrapVector(Vector3i v) {
        return new int[]{v.x, v.y, v.z};
    }

    public static float[] wrapVector(Vector2f v) {
        return new float[]{v.x, v.y};
    }

    public static double[] wrapVector(Vector2d v) {
        return new double[]{v.x, v.y};
    }

    public static int[] wrapVector(Vector2i v) {
        return new int[]{v.x, v.y};
    }

    public static String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.toLowerCase().substring(1);
    }

    public static String prettify(String value) {
        if (value.startsWith("m_")) {
            value = value.substring(2);
        }
        else if (value.startsWith("_")) {
            value = value.substring(1);
        }

        value = value.substring(0, 1).toUpperCase() + value.substring(1);
        return value.replaceAll("\\d+", "").replaceAll("(.)([A-Z])", "$1 $2");
    }

    public static int countMatches(String line, String s) {
        return line.length() - line.replace(s, "").length();
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param source     the source to load
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    public static ByteBuffer inputToByteBuffer(InputStream source, int bufferSize) throws IOException {
        ByteBuffer buffer;

            try (ReadableByteChannel rbc = Channels.newChannel(source)) {
                buffer = createByteBuffer(bufferSize);

                while (true) {
                    int bytes = rbc.read(buffer);
                    if (bytes == -1) {
                        break;
                    }
                    if (buffer.remaining() == 0) {
                        buffer = resizeBuffer(buffer, buffer.capacity() * 3 / 2); // 50%
                    }
                }
            }

        buffer.flip();
        return memSlice(buffer);
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    public static com.jme3.math.Vector3f joml2jme(Vector3f v) {
        return new com.jme3.math.Vector3f(v.x, v.y, v.z);
    }

    public static com.jme3.math.Vector2f joml2jme(Vector2f v) {
        return new com.jme3.math.Vector2f(v.x, v.y);
    }

    public static com.jme3.math.Vector4f joml2jme(Vector4f v) {
        return new com.jme3.math.Vector4f(v.x, v.y, v.z, v.w);
    }

    public static Vector3f jme2joml(com.jme3.math.Vector3f v) {
        return new Vector3f(v.x, v.y, v.z);
    }

    public static Vector2f jme2joml(com.jme3.math.Vector2f v) {
        return new Vector2f(v.x, v.y);
    }

    public static Vector4f jme2joml(com.jme3.math.Vector4f v) {
        return new Vector4f(v.x, v.y, v.z, v.w);
    }

    private static byte[] createChecksum(String filename) throws Exception {
        InputStream fis =  new FileInputStream(filename);

        byte[] buffer = new byte[1024];
        MessageDigest complete = MessageDigest.getInstance("MD5");
        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();
        return complete.digest();
    }

    public static String getMD5Checksum(String filename) throws Exception {
        byte[] b = createChecksum(filename);
        StringBuilder result = new StringBuilder();

        for (byte value : b) {
            result.append(Integer.toString((value & 0xff) + 0x100, 16).substring(1));
        }

        return result.toString();
    }
}

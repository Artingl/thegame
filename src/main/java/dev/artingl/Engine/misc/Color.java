package dev.artingl.Engine.misc;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class Color {

    public static final Color BLACK = Color.from("#000000");
    public static final Color WHITE = Color.from("#ffffff");
    public static final Color TRANSPARENT = Color.from("#00000000");
    public static final Color RED = Color.from("#ff0000");
    public static final Color GREEN = Color.from("#00ff00");
    public static final Color BLUE = Color.from("#0000ff");

    // -----------

    public static Color from(String clr) {
        // Remove the hashtag prefix from the color if appears
        clr = clr.startsWith("#") ? clr.substring(1) : clr;

        // Check if the color value is invalid
        if (clr.length() != 8 && clr.length() != 6) {
            return new Color(0, 0, 0, 1);
        }

        int red = Integer.parseInt(clr.substring(0, 2), 16),
                green = Integer.parseInt(clr.substring(2, 4), 16),
                blue = Integer.parseInt(clr.substring(4, 6), 16);
        int alpha;

        if (clr.length() == 6) {
            // The color does not have alpha values
            alpha = 0xff;
        } else {
            // It does have alpha
            alpha = Integer.parseInt(clr.substring(6), 16);
        }

        return new Color(red, green, blue, alpha);
    }

    public static Color from(int clr) {
        int red = clr >> 24 & 0xff,
                green = clr >> 16 & 0xff,
                blue = clr >> 8 & 0xff,
                alpha = clr & 0xff;

        return new Color(red, green, blue, alpha);
    }

    public static Color from(int red, int green, int blue) {
        return new Color(red, green, blue, 0xff);
    }

    public static Color from(int red, int green, int blue, int alpha) {
        return new Color(red, green, blue, alpha);
    }

    public static Color from(float[] color) {
        int r = 255, g = 255, b = 255, a = 255;

        if (color.length > 0) r = (int) (color[0] * 255);
        if (color.length > 1) g = (int) (color[1] * 255);
        if (color.length > 2) b = (int) (color[2] * 255);
        if (color.length > 3) a = (int) (color[3] * 255);

        return new Color(r, g, b, a);
    }

    public static Color from(int[] color) {
        int r = 255, g = 255, b = 255, a = 255;

        if (color.length > 0) r = color[0];
        if (color.length > 1) g = color[1];
        if (color.length > 2) b = color[2];
        if (color.length > 3) a = color[3];

        return new Color(r, g, b, a);
    }

    public static Color random() {
        return new Color(Utils.randInt(0, 255), Utils.randInt(0, 255), Utils.randInt(0, 255), 255);
    }

    private int r, g, b, a;

    private Color(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }

    public int red() {
        return this.r;
    }

    public int green() {
        return this.g;
    }

    public int blue() {
        return this.b;
    }

    public int alpha() {
        return this.a;
    }

    public int rgba() {
        return (((r << 24) | g << 16) | b << 8) | a;
    }

    public int argb() {
        return (((a << 24) | r << 16) | g << 8) | b;
    }

    @Override
    public String toString() {
        return "Color{r=" + red() + ", g=" + green() + ", b=" + blue() + ", a=" + alpha() + "}";
    }

    /**
     * Return the color value as vec3f in normalized form (to be used in the OpenGL)
     */
    public Vector3f asVector3f() {
        return new Vector3f(
                red() / 255.f,
                green() / 255.f,
                blue() / 255.f
        );
    }

    /**
     * Return the color value as vec4f in normalized form (to be used in the OpenGL)
     */
    public Vector4f asVector4f() {
        return new Vector4f(
                red() / 255.f,
                green() / 255.f,
                blue() / 255.f,
                alpha() / 255.f
        );
    }

    /**
     * Add color values to this color
     *
     * @param color Target color
     */
    public Color add(Color color) {
        r += color.r;
        g += color.g;
        b += color.b;
        a += color.a;
        return this;
    }

    /**
     * Change current color values to provided
     *
     * @param color Target color
     */
    public Color set(Color color) {
        r = color.r;
        g = color.g;
        b = color.b;
        a = color.a;
        return this;
    }

    /**
     * Divide RGB values with a divider
     *
     * @param divider Divider to be used
     */
    public Color div3(float divider) {
        r = (int) (r / divider);
        g = (int) (g / divider);
        b = (int) (b / divider);
        return this;
    }

    /**
     * Divide RGBA values with a divider
     *
     * @param divider Divider to be used
     */
    public Color div4(float divider) {
        r = (int) (r / divider);
        g = (int) (g / divider);
        b = (int) (b / divider);
        a = (int) (a / divider);
        return this;
    }

    /**
     * Multiply RGB values with a value
     *
     * @param v Value to be used
     */
    public Color mul3(float v) {
        r = (int) (r * v);
        g = (int) (g * v);
        b = (int) (b * v);
        return this;
    }

    /**
     * Multiply RGBA values with a value
     *
     * @param v Value to be used
     */
    public Color mul4(float v) {
        r = (int) (r * v);
        g = (int) (g * v);
        b = (int) (b * v);
        a = (int) (a * v);
        return this;
    }
}

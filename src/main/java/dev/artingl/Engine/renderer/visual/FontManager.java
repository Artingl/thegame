package dev.artingl.Engine.renderer.visual;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.mesh.BaseMesh;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.texture.Texture;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.jme3.renderer.opengl.GL.GL_RGBA;
import static org.lwjgl.stb.STBTruetype.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class FontManager {

    private static final int BITMAP_W = 512;
    private static final int BITMAP_H = 512;

    private final Logger logger;
    private final Renderer renderer;
    private final ConcurrentHashMap<String, BaseMesh> cachedText;
    private final ConcurrentHashMap<Resource, FontInfo> fontsMap;
    private final List<Resource> lazyFonts;
    private long cacheClearTime;

    public FontManager(Logger logger, Renderer renderer) {
        this.logger = logger;
        this.renderer = renderer;
        this.fontsMap = new ConcurrentHashMap<>();
        this.cachedText = new ConcurrentHashMap<>();
        this.lazyFonts = new ArrayList<>();
    }

    /**
     * Load font from resource to be used later.
     * Do not call this function directly.
     *
     * @param font Resource at which the font is located
     */
    public void loadFont(Resource font) throws IOException, EngineException, FontFormatException {
        if (!font.exists())
            throw new EngineException("Font file " + font + " does not exist!");

        logger.log(LogLevel.INFO, "Loading font " + font);
        Resource fontResource = new Resource(font.getNamespace(), font.getPath().replaceFirst("[.][^.]+$", ""));

        // Load the file
        try (InputStream stream = font.load()) {
            int ascent, descent, lineGap;
            byte[] data = stream.readAllBytes();
            ByteBuffer ttf = MemoryUtil.memCalloc(data.length);
            ttf.put(data);
            ttf.flip();
            STBTTFontinfo info = STBTTFontinfo.create();

            if (!stbtt_InitFont(info, ttf))
                throw new EngineException("Unable to load font " + font);

            try (MemoryStack stack = stackPush()) {
                IntBuffer pAscent = stack.mallocInt(1);
                IntBuffer pDescent = stack.mallocInt(1);
                IntBuffer pLineGap = stack.mallocInt(1);

                stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);

                ascent = pAscent.get(0);
                descent = pDescent.get(0);
                lineGap = pLineGap.get(0);
            }

            Font awtFont = Font.createFont(Font.TRUETYPE_FONT, font.load());
            this.fontsMap.put(fontResource, new FontInfo(awtFont, info, ttf, new HashMap<>(), ascent, descent, lineGap));
        }
    }

    /**
     * Renders text at position
     *
     * @param renderer The renderer
     * @param font     The font to be used in format namespace:font/filename_without_extension
     * @param position The position at which text will be rendered
     * @param text     Text to be rendered
     * @param size     Font size
     */
    public void renderText(Renderer renderer, Resource font, String text, Vector3f position, Color color, int size) {
        String cacheKey = text + " " + size + " " + font.toString();
        BaseMesh mesh;
        position = new Vector3f(position);

        synchronized (this.cachedText) {
            if ((mesh = this.cachedText.get(cacheKey)) == null) {
                FontInfo info = this.fontsMap.get(font);

                if (info == null)
                    return;

                VerticesBuffer vertices = new VerticesBuffer(
                        VerticesBuffer.Attribute.VEC3F,
                        VerticesBuffer.Attribute.VEC3F,
                        VerticesBuffer.Attribute.VEC2F);

                float factorX = 1.0f;
                float factorY = 1.0f;
                float lineY = 0.0f;
                FontSizeInfo sizeInfo = info.sizes.get(size);

                if (sizeInfo == null || !sizeInfo.isValid) {
                    sizeInfo = new FontSizeInfo(info, size);
                    info.sizes.put(size, sizeInfo);
                }

                if (!sizeInfo.isValid)
                    return;

                try (MemoryStack stack = stackPush()) {
                    STBTTAlignedQuad q = STBTTAlignedQuad.malloc(stack);

                    IntBuffer pCodePoint = stack.mallocInt(1);
                    FloatBuffer x = stack.floats(0.0f);
                    FloatBuffer y = stack.floats(0.0f);

                    for (int i = 0, to = text.length(); i < to;) {
                        i += getCP(text, to, i, pCodePoint);

                        int cp = pCodePoint.get(0);
                        if (cp == '\n') {
                            y.put(0, lineY = y.get(0) + (info.ascent - info.descent + info.lineGap) * sizeInfo.scale);
                            x.put(0, 0.0f);
                            continue;
                        } else if (cp < 32 || 128 <= cp) {
                            continue;
                        }

                        float cpX = x.get(0);
                        stbtt_GetBakedQuad(sizeInfo.cdata, BITMAP_W, BITMAP_H, cp - 32, x, y, q, true);
                        x.put(0, scale(cpX, x.get(0), factorX));

                        float x0 = scale(cpX, q.x0(), factorX),
                                x1 = scale(cpX, q.x1(), factorX),
                                y0 = scale(lineY, q.y0(), factorY),
                                y1 = scale(lineY, q.y1(), factorY);

                        vertices.addAttribute(new Vector3f(x0, 1 - y0, 0)).addAttribute(new Vector3f()).addAttribute(new Vector2f(q.s0(), q.t0()));
                        vertices.addAttribute(new Vector3f(x1, 1 - y0, 0)).addAttribute(new Vector3f()).addAttribute(new Vector2f(q.s1(), q.t0()));
                        vertices.addAttribute(new Vector3f(x0, 1 - y1, 0)).addAttribute(new Vector3f()).addAttribute(new Vector2f(q.s0(), q.t1()));

                        vertices.addAttribute(new Vector3f(x1, 1 - y1, 0)).addAttribute(new Vector3f()).addAttribute(new Vector2f(q.s1(), q.t1()));
                        vertices.addAttribute(new Vector3f(x0, 1 - y1, 0)).addAttribute(new Vector3f()).addAttribute(new Vector2f(q.s0(), q.t1()));
                        vertices.addAttribute(new Vector3f(x1, 1 - y0, 0)).addAttribute(new Vector3f()).addAttribute(new Vector2f(q.s1(), q.t0()));
                    }

                    mesh = new BaseMesh(vertices);
                    mesh.enableFade(false);
                    mesh.setTexture(sizeInfo.getTexture());
                    mesh.bake();
                    this.cachedText.put(cacheKey, mesh);
                }
            }

            mesh.setColor(color);
            mesh.transform(new Matrix4f()
                    .scale(new Vector3f(0.05f))
                    .translate(position)
                    .rotateXYZ(new Vector3f()));
            mesh.render(renderer);
        }
    }

    /**
     * Returns text's width
     *
     * @param font     The font to be used in format namespace:font/filename_without_extension
     * @param text     Text to be rendered
     * @param size     Font size
     */
    public float getTextWidth(Resource font, String text, int size) {
        FontInfo info = this.fontsMap.get(font);
        int width = 0;

        if (info == null)
            return 0;

        FontSizeInfo sizeInfo = info.sizes.get(size);
        if (sizeInfo == null || !sizeInfo.isValid) {
            sizeInfo = new FontSizeInfo(info, size);
            info.sizes.put(size, sizeInfo);
        }

        if (!sizeInfo.isValid)
            return 0;

        try (MemoryStack stack = stackPush()) {
            IntBuffer pCodePoint       = stack.mallocInt(1);
            IntBuffer pAdvancedWidth   = stack.mallocInt(1);
            IntBuffer pLeftSideBearing = stack.mallocInt(1);

            for (int i = 0, to = text.length(); i < to;) {
                i += getCP(text, to, i, pCodePoint);
                int cp = pCodePoint.get(0);

                stbtt_GetCodepointHMetrics(info.font, cp, pAdvancedWidth, pLeftSideBearing);
                width += pAdvancedWidth.get(0);
            }
        }

        return width * sizeInfo.scale;
    }

    private static float scale(float center, float offset, float factor) {
        return (offset - center) * factor + center;
    }

    private static int getCP(String text, int to, int i, IntBuffer cpOut) {
        char c1 = text.charAt(i);
        if (Character.isHighSurrogate(c1) && i + 1 < to) {
            char c2 = text.charAt(i + 1);
            if (Character.isLowSurrogate(c2)) {
                cpOut.put(0, Character.toCodePoint(c1, c2));
                return 2;
            }
        }
        cpOut.put(0, c1);
        return 1;
    }

    /**
     * Get AWT font instance from previously loaded font.
     * If not fonts found, null is returned.
     *
     * @param font The font to be used in format namespace:font/filename_without_extension
     * @param size The font size
     */
    @Nullable
    public Font getAWTFont(Resource font, int size) {
        if (this.fontsMap.containsKey(font)) {
            Font result = this.fontsMap.get(font).awtFont;
            return result.deriveFont(Font.BOLD, size);
        }
        return null;
    }

    public void cleanup() {
        synchronized (this.fontsMap) {
            for (FontInfo info : this.fontsMap.values()) {
                if (info.font != null)
                    info.font.close();
                if (info.buffer != null)
                    MemoryUtil.memFree(info.buffer);
                for (FontSizeInfo sizeInfo: info.sizes().values())
                    sizeInfo.cleanup();
                info.sizes.clear();
            }
            this.fontsMap.clear();
        }
    }

    public void init() {}

    public void frame() {
        // Initialize all lazy fonts
        synchronized (this.lazyFonts) {
            for (Resource font: this.lazyFonts) {
                try {
                    this.loadFont(font);
                } catch (Exception e) {
                    logger.exception(e, "Unable to load font " + font);
                }
            }
            this.lazyFonts.clear();
        }

        // Clear the cache every 30 seconds
        if (cacheClearTime + 30 * 1000 < System.currentTimeMillis()) {
            this.cacheClearTime = System.currentTimeMillis();
            synchronized (this.cachedText) {
                for (IMesh mesh : this.cachedText.values())
                    mesh.cleanup();
                this.cachedText.clear();
            }
        }
    }

    public void addFont(Resource resource) throws EngineException {
        if (!resource.exists())
            throw new EngineException("Font " + resource + " is invalid!");
        synchronized (this.lazyFonts) {
            this.lazyFonts.add(resource);
        }
    }

    private record FontInfo(Font awtFont, STBTTFontinfo font, ByteBuffer buffer, Map<Integer, FontSizeInfo> sizes, int ascent, int descent, int lineGap) {
    }

    private class FontSizeInfo {

        private final FontInfo font;
        private final float scale;
        private final STBTTBakedChar.Buffer cdata;
        private final ByteBuffer bitmap;
        private final Texture texture;
        private final boolean isValid;

        public FontSizeInfo(FontInfo font, int size) {
            this.font = font;
            this.scale = stbtt_ScaleForPixelHeight(font.font, size);
            this.cdata = STBTTBakedChar.malloc(96);
            this.bitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H);
            int result = stbtt_BakeFontBitmap(font.buffer, size, bitmap, BITMAP_W, BITMAP_H, 32, cdata);

            if (result < 0) {
                logger.log(LogLevel.WARNING, "stbtt_BakeFontBitmap: " + result);
                stbtt_FreeBitmap(bitmap);
                cdata.free();
                this.isValid = false;
                this.texture = null;
                return;
            }

            ByteBuffer textureBitmap = BufferUtils.createByteBuffer(BITMAP_W * BITMAP_H * 4);
            for (int i = 0; i < BITMAP_W; i++)
                for (int j = 0; j < BITMAP_H; j++) {
                    textureBitmap.put((j * BITMAP_W + i) * 4, bitmap.get(j * BITMAP_W + i));
                    textureBitmap.put((j * BITMAP_W + i) * 4 + 1, bitmap.get(j * BITMAP_W + i));
                    textureBitmap.put((j * BITMAP_W + i) * 4 + 2, bitmap.get(j * BITMAP_W + i));
                    textureBitmap.put((j * BITMAP_W + i) * 4 + 3, (byte) (bitmap.get(j * BITMAP_W + i) == 0 ? 0 : 255));
                }

            this.texture = new Texture(textureBitmap, GL_RGBA, BITMAP_W, BITMAP_H);
            this.isValid = true;
        }

        public Texture getTexture() {
            return texture;
        }

        public float getScale() {
            return scale;
        }

        public FontInfo getFont() {
            return font;
        }

        public ByteBuffer getBitmap() {
            return bitmap;
        }

        public STBTTBakedChar.Buffer getCdata() {
            return cdata;
        }

        public void cleanup() {
            stbtt_FreeBitmap(bitmap);
            cdata.free();
        }

    }

}

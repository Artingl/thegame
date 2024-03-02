package dev.artingl.Engine.renderer;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.renderer.mesh.IMesh;
import dev.artingl.Engine.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.system.MemoryStack;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.ConcurrentHashMap;

import static org.lwjgl.stb.STBTruetype.stbtt_GetFontVMetrics;
import static org.lwjgl.stb.STBTruetype.stbtt_InitFont;
import static org.lwjgl.system.MemoryStack.stackPush;

public class FontManager {
    private final Logger logger;
    private final Renderer renderer;
    private final ConcurrentHashMap<Resource, FontInfo> fontsMap;

    public FontManager(Logger logger, Renderer renderer) {
        this.logger = logger;
        this.renderer = renderer;
        this.fontsMap = new ConcurrentHashMap<>();
    }

    /**
     * Load font from resource to be used later.
     *
     * @param font Resource at which the font is located
     */
    public void loadFont(Resource font) throws IOException, EngineException, FontFormatException {
        if (!font.exists())
            return;

        Resource fontResource = new Resource(font.getNamespace(), font.getPath().replaceFirst("[.][^.]+$", ""));
        Font awtFont = Font.createFont(Font.TRUETYPE_FONT, font.load());
        this.fontsMap.put(fontResource, new FontInfo(awtFont, null, 0, 0, 0));

        if (true)
            return;
        // Load the file
        try (InputStream stream = font.load()) {
            ByteBuffer ttf = ByteBuffer.wrap(stream.readAllBytes());
            STBTTFontinfo info = STBTTFontinfo.create();

            if (!stbtt_InitFont(info, ttf))
                throw new EngineException("Unable to load font " + font);

            try (MemoryStack stack = stackPush()) {
                IntBuffer pAscent = stack.mallocInt(1);
                IntBuffer pDescent = stack.mallocInt(1);
                IntBuffer pLineGap = stack.mallocInt(1);

                stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap);

//                this.fontsMap.put(fontResource, new FontInfo(null, info, pAscent.get(0), pDescent.get(0), pLineGap.get(0)));
            }
        }
    }

    /**
     * Prepare text mesh for rendering later.
     * Note: You'd need to manually cleanup thr mesh after using it
     *
     * @param context Current renderer context
     * @param font    The font to be used in format namespace:font/filename_without_extension
     * @param text    Text to be rendered
     */
    public IMesh prepareTextMesh(RenderContext context, Resource font, String text) {
        return null;

//        BaseMesh mesh = new BaseMesh();
//        mesh.bake();
//        return mesh;
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

    public void init() {
    }

    public void cleanup() {
        synchronized (this.fontsMap) {
            for (FontInfo info : this.fontsMap.values())
                if (info.font != null)
                    info.font.close();
            this.fontsMap.clear();
        }
    }

    private record FontInfo(Font awtFont, STBTTFontinfo font, int ascent, int descent, int lineGap) {
    }

}

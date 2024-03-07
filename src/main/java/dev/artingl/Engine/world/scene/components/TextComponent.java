package dev.artingl.Engine.world.scene.components;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.visual.FontManager;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Vector3f;

public class TextComponent extends Component {

    public boolean enableRendering = true;

    public String text;
    public int size;
    public Vector3f position;
    public Resource font;

    public TextComponent(Resource font, String text, int size) {
        this.text = text;
        this.size = size;
        this.position = new Vector3f();
        this.font = font;
    }

    @Override
    public void render(SceneNode node, Renderer renderer) {
        if (enableRendering) {
            FontManager fontManager = getEngine().getRenderer().getFontManager();
            fontManager.renderText(renderer, font, text, new Vector3f(position).sub(1.2f, 1.2f, 0), Color.from(130, 130, 130), size);
            fontManager.renderText(renderer, font, text, position, Color.WHITE, size);
        }
    }
}

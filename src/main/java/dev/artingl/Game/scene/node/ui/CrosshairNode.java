package dev.artingl.Game.scene.node.ui;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.mesh.SquareMesh;
import dev.artingl.Engine.resources.Resource;
import dev.artingl.Engine.resources.Text;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.components.TextComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Vector3f;

public class CrosshairNode extends SceneNode {
    public static final Color CROSSHAIR_COLOR = Color.WHITE;
    private final TextComponent tooltipText;

    public CrosshairNode() {
        SquareMesh mesh0 = new SquareMesh(0.3f, 0.05f, CROSSHAIR_COLOR, null);
        SquareMesh mesh1 = new SquareMesh(0.05f, 0.3f, CROSSHAIR_COLOR, null);
        this.tooltipText = new TextComponent(new Resource("thegame", "font/main"), "", 12);

        this.addComponent(new MeshComponent(mesh0));
        this.addComponent(new MeshComponent(mesh1));
        this.addComponent(this.tooltipText);

        getTransform().position.z = -1;
    }

    /**
     * Set tooltip text under the crosshair
     *
     * @param tooltip The tooltip text to be set
     * */
    public void setTooltip(Text tooltip) {
        float textWidth = getEngine().getRenderer().getFontManager().getTextWidth(
                new Resource("thegame", "font/main"),
                tooltip.getText(), 12);
        this.tooltipText.text = tooltip.getText();
        this.tooltipText.position = new Vector3f(-(textWidth / 2f), -12, -2);
    }

}

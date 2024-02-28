package dev.artingl.Game.scene.node.ui;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.mesh.SquareMesh;
import dev.artingl.Engine.resources.Text;
import dev.artingl.Engine.resources.texture.Texture;
import dev.artingl.Engine.world.scene.components.MeshComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;

public class CrosshairNode extends SceneNode {
    public static final Color CROSSHAIR_COLOR = Color.from("#ffffff99");

    public CrosshairNode() {
        SquareMesh mesh0 = new SquareMesh(0.1f, 0.1f, CROSSHAIR_COLOR, Texture.MISSING);
        this.addComponent(new MeshComponent(mesh0));

        getTransform().position.z = -1;
    }

    /**
     * Set tooltip text under the crosshair
     *
     * @param tooltip The tooltip text to be set
     * */
    public void setTooltip(Text tooltip) {
        System.out.println(tooltip);
    }

}

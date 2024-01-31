package dev.artingl.Game.scene.node;

import dev.artingl.Engine.scene.components.MeshComponent;
import dev.artingl.Engine.scene.nodes.SceneNode;
import dev.artingl.Game.level.ambient.Sky;

public class SkyNode extends SceneNode {

    public SkyNode(Sky sky) {
        this.addComponent(new MeshComponent(sky.getMesh()));
    }

}

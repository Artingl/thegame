package dev.artingl.Engine.world.scene;

import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Vector3f;

import java.util.Comparator;

public class SceneNodesSorter implements Comparator<SceneNode> {

    private final CameraNode camera;
    private final Vector3f cameraPos;

    public SceneNodesSorter(CameraNode camera) {
        this.camera = camera;
        this.cameraPos = new Vector3f(camera.getTransform().position);
    }

    @Override
    public int compare(SceneNode o0, SceneNode o1) {
        if (o0 == null || o1 == null)
            return 0;

        Vector3f pos0 = o0.getTransform().position;
        Vector3f pos1 = o1.getTransform().position;

        if (pos0.distance(cameraPos) == pos1.distance(cameraPos))
            return 0;

        return pos0.distance(cameraPos) < pos1.distance(cameraPos) ? 1 : -1;
    }
}

package dev.artingl.Engine.world.scene.components.transform;

import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.components.Component;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class TransformComponent extends Component {

    public Vector3f position;
    public Vector3f rotation;
    public Vector3f scale;

    public TransformComponent() {
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.scale = new Vector3f(1, 1, 1);
    }

    @Override
    public String getName() {
        return "Transform";
    }

    public Matrix4f getMatrix() {
        /* If the node is a child, calculate relative position to the parent transform
        * TODO: also calculate rotation and scale */
        Vector3f relativePosition = new Vector3f();
        SceneNode node = getNode();

        if (node != null) {
            if (node.isChild()) {
                relativePosition.set(node.getParent().getTransform().position);
            }
        }

        return new Matrix4f()
                .scale(scale)
                .translate(relativePosition.add(position))
                .rotateXYZ(
                        (float) Math.toRadians(rotation.x),
                        (float) Math.toRadians(rotation.y),
                        (float) Math.toRadians(rotation.z));
    }

    public void copy(TransformComponent transform) {
        this.position = new Vector3f(transform.position);
        this.rotation = new Vector3f(transform.rotation);
        this.scale = new Vector3f(transform.scale);
    }
}

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
    public Vector3f pivot;

    public TransformComponent() {
        this.position = new Vector3f();
        this.rotation = new Vector3f();
        this.scale = new Vector3f(1, 1, 1);
        this.pivot = new Vector3f();
    }

    @Override
    public String getName() {
        return "Transform";
    }

    public void setLocalPosition(Vector3f position) {
        SceneNode node = getNode();
        if (node != null)
            if (node.isChild()) {
                this.position = new Vector3f(position).sub(node.getParent().getTransform().position);
                return;
            }
        this.position = new Vector3f(position);
    }

    public void setLocalRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public void setLocalScale(Vector3f scale) {
        SceneNode node = getNode();
        if (node != null)
            if (node.isChild()) {
                this.scale = new Vector3f(scale).sub(node.getParent().getTransform().scale);
                return;
            }
        this.scale = new Vector3f(scale);
    }

    public Vector3f getWorldPosition() {
        SceneNode node = getNode();
        if (node != null)
            if (node.isChild()) {
                return new Vector3f(node.getParent().getTransform().position).add(position);
            }
        return new Vector3f(position);
    }

    public Vector3f getWorldRotation() {
        return rotation;
    }

    public Vector3f getWorldScale() {
        SceneNode node = getNode();
        if (node != null)
            if (node.isChild()) {
                return new Vector3f(node.getParent().getTransform().scale).add(scale);
            }
        return new Vector3f(scale);
    }

    public Matrix4f getMatrix() {
        /* If the node is a child, calculate relative position to the parent transform
        * TODO: also calculate rotation */
        SceneNode node = getNode();

        if (node != null) {
            if (node.isChild()) {
                TransformComponent parentTransform = node.getParent().getTransform();
                Vector3f relativePosition = new Vector3f(parentTransform.position);
                Vector3f relativeRotation = new Vector3f(parentTransform.rotation);
                Vector3f relativeScale = new Vector3f(parentTransform.scale);

                return new Matrix4f()
                        .scale(relativeScale.mul(this.scale))
                        .rotateXYZ(0, 0, 0)
                        .translate(new Vector3f(this.position).add(relativePosition).add(this.pivot))
                        .rotateXYZ(
                                (float) Math.toRadians(this.rotation.x),
                                (float) Math.toRadians(this.rotation.y),
                                (float) Math.toRadians(this.rotation.z))
                        .translate(new Vector3f(this.pivot).mul(-1));
            }
        }

        return new Matrix4f()
                .scale(scale)
                .rotateXYZ(0, 0, 0)
                .translate(new Vector3f(this.position).add(this.pivot))
                .rotateXYZ(
                        (float) Math.toRadians(this.rotation.x),
                        (float) Math.toRadians(this.rotation.y),
                        (float) Math.toRadians(this.rotation.z))
                .translate(new Vector3f(this.pivot).mul(-1));
    }

    public void copy(TransformComponent transform) {
        this.position = new Vector3f(transform.position);
        this.rotation = new Vector3f(transform.rotation);
        this.scale = new Vector3f(transform.scale);
    }
}

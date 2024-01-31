package dev.artingl.Engine.scene.components.transform;

import dev.artingl.Engine.scene.components.Component;
import dev.artingl.Engine.scene.nodes.SceneNode;
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
//        Matrix4f moveOriginMat = new Matrix4f();
//        Matrix4f modelMatrix = new Matrix4f();
//        Matrix4f scaleMat = new Matrix4f().scale(scale);
//
//        Vector3f centroid = new Vector3f(0.5f);
//        moveOriginMat.translation(-(float)centroid.x * (scaleMat.m00() - 1), -(float)centroid.y * (scaleMat.m11() - 1), -(float)centroid.z * (scaleMat.m22() - 1));
//        modelMatrix.set(moveOriginMat);
//        modelMatrix.mul(scaleMat);
//        modelMatrix.translate(centroid.x, centroid.y, centroid.z);
//        modelMatrix.mul(new Matrix4f().rotateZ(MathUtils.deg2rad(rotation.z)));
//        modelMatrix.mul(new Matrix4f().rotateY(MathUtils.deg2rad(rotation.y)));
//        modelMatrix.mul(new Matrix4f().rotateX(MathUtils.deg2rad(rotation.x)));
//        modelMatrix.translate(-(float)centroid.x, -(float)centroid.y, -(float)centroid.z);
//        modelMatrix.mul(new Matrix4f().translate(position));
//
//        return modelMatrix;

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
}

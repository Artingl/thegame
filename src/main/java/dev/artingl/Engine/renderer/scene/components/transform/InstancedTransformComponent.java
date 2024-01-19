package dev.artingl.Engine.renderer.scene.components.transform;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.renderer.scene.components.ComponentIgnoreField;
import dev.artingl.Engine.renderer.scene.components.ComponentInformation;
import dev.artingl.Engine.renderer.scene.components.ComponentFinalField;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class InstancedTransformComponent extends TransformComponent {

    @ComponentFinalField
    @ComponentInformation(desc="Amount of transform instances")
    public int instances;

    // Ignore fields from basic transform component
    @ComponentIgnoreField
    public Vector3f position;
    @ComponentIgnoreField
    public Vector3f rotation;
    @ComponentIgnoreField
    public Vector3f scale;

    private final List<TransformComponent> transforms = new ArrayList<>();
    private int transformsHash = 0;

    public void addInstanceTransform(Vector3f position, Vector3f rotation, Vector3f scale) {
        TransformComponent transform = new TransformComponent();
        transform.position = position;
        transform.rotation = rotation;
        transform.scale = scale;
        this.transforms.add(transform);
        this.instances = this.transforms.size();
        this.transformsHash += this.instances;
    }

    /**
     * Get collection of all transform instaces.
     * */
    public Collection<TransformComponent> getTransforms() {
        return transforms;
    }

    /**
     * Get hash of the transforms collection, which can help to prove whether the collection was updated.
     * */
    public int getTransformsHash() {
        return transformsHash;
    }

    @Override
    public String getName() {
        return "Instanced Transform";
    }

    @Override
    public Matrix4f getMatrix() {
        Engine.getInstance().getLogger().log(LogLevel.WARNING, "Instanced Transform does not provide matrices");
        return new Matrix4f();
    }
}

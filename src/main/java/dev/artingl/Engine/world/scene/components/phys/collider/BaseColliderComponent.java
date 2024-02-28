package dev.artingl.Engine.world.scene.components.phys.collider;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.GhostControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.scene.Spatial;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.Component;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.jetbrains.annotations.Nullable;

public class BaseColliderComponent extends Component {

    private boolean isColliderBuilt = false;
    private CollisionShape shape;

    @Override
    public void cleanup() {
        super.cleanup();
        SceneNode node = getNode();

        if (node == null)
            return;

        try {
            BaseScene scene = node.getScene();
            PhysicsSpace space = scene.getPhysicsSpace();
            if (this.shape != null)
                space.remove(this.shape);
        } catch (Exception ignored) {}
    }

    /**
     * Build the collider
     */
    protected CollisionShape buildCollider() {
        getEngine().getLogger().log(LogLevel.UNIMPLEMENTED, "Don't use BaseColliderComponent as the collider for objects.");
        return null;
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);
        SceneNode node = getNode();

        if (node == null)
            return;

        if (!isColliderBuilt) {
            BaseScene scene = node.getScene();
            PhysicsSpace space = scene.getPhysicsSpace();

            // Remove old shape from the space if we have any
            if (this.shape != null)
                space.remove(this.shape);

            // Build the collider and save the shape
            this.shape = this.buildCollider();
            if (this.shape != null) {
                this.isColliderBuilt = true;
            }
        }
    }

    @Override
    public String getName() {
        return "Collider";
    }

    /**
     * Get colliders' shape
     * */
    @Nullable
    public CollisionShape getShape() {
        return shape;
    }

}

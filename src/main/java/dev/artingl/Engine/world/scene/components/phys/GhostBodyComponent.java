package dev.artingl.Engine.world.scene.components.phys;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.GhostControl;
import com.jme3.math.Quaternion;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.components.Component;
import dev.artingl.Engine.world.scene.components.phys.collider.BaseColliderComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;

public class GhostBodyComponent extends Component implements BodyComponent {

    private GhostControl body;

    @Override
    public void cleanup() {
        super.cleanup();
        if (body != null) {
            PhysicsSpace space = body.getPhysicsSpace();
            space.removeCollisionObject(body);
        }
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        SceneNode node = getNode();

        if (node == null)
            return;

        // Set the rigidbody's shape if we have collider
        BaseColliderComponent collider = node.getComponent(BaseColliderComponent.class);
        if (collider == null || collider.getShape() == null)
            return;

        if (body == null) {
            this.body = new GhostControl(collider.getShape());
            this.body.setUserObject(this);
        }

        PhysicsSpace space = node.getScene().getPhysicsSpace();
        TransformComponent transform = node.getTransform();

        this.body.setPhysicsSpace(space);
        this.body.setCollisionShape(collider.getShape());
        this.body.setPhysicsLocation(Utils.joml2jme(transform.getWorldPosition()));
        this.body.setPhysicsRotation(new Quaternion().fromAngles(transform.rotation.x, transform.rotation.y, transform.rotation.z));
    }

    @Override
    public String getName() {
        return "Ghost Body";
    }

    @Override
    public boolean overlaps(BodyComponent body) {
        return this.body.getOverlappingObjects().contains(body.getCollisionObject());
    }

    @Override
    public PhysicsCollisionObject getCollisionObject() {
        return this.body;
    }
}

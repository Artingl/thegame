package dev.artingl.Engine.world.scene.components.phys;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.math.Quaternion;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.components.Component;
import dev.artingl.Engine.world.scene.components.phys.collider.BaseColliderComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Vector3f;

public class RigidBodyComponent extends Component implements BodyComponent {
    public boolean enableBody = true;
    public boolean isKinematic = false;
    public float mass;

    private boolean outOfRangeCheck;
    private final RigidBodyControl body;
    private Vector3f lastPos, lastRot;

    public RigidBodyComponent() {
        this(1);
    }

    public RigidBodyComponent(float mass) {
        this.mass = mass;
        this.body = new RigidBodyControl(mass);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        PhysicsSpace space = body.getPhysicsSpace();
        space.removeCollisionObject(body);
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        SceneNode node = getNode();

        if (node == null)
            return;

        // If the rigidbody is too far from the player, disable it so we won't waste time computing collisions with it
        if (this.outOfRangeCheck) {
            CameraNode camera = node.getScene().getMainCamera();
            if (camera != null) {
                float distance = camera.getPosition().distance(node.getTransform().position);
                if (distance > 60) {
                    this.body.setEnabled(false);
                    return;
                }
            }
        }

        // Set the rigidbody's shape if we have collider
        BaseColliderComponent collider = node.getComponent(BaseColliderComponent.class);
        if (collider == null || collider.getShape() == null)
            return;

        if (collider instanceof MeshColliderComponent) {
            this.body.setMass(0);
            this.isKinematic = true;
            this.mass = 0;
        }
        else {
            this.body.setMass(this.mass);
        }

        PhysicsSpace space = node.getScene().getPhysicsSpace();
        this.body.setCollisionShape(collider.getShape());
        this.body.setPhysicsSpace(space);

        this.updateBody();
    }

    private void updateBody() {
        SceneNode node = getNode();
        if (node == null || !enableBody) {
            this.lastRot = null;
            this.lastPos = null;
            this.body.setEnabled(false);
            return;
        }

        this.body.setKinematic(this.isKinematic);
        this.body.setEnabled(true);

        TransformComponent transform = node.getTransform();
        Vector3f position = Utils.jme2joml(body.getPhysicsLocation());
        Vector3f rotation = new Vector3f(
                (float) Math.toDegrees(body.getPhysicsRotation().getX()) + 180,
                (float) Math.toDegrees(body.getPhysicsRotation().getY()) + 180,
                (float) Math.toDegrees(body.getPhysicsRotation().getZ()) + 180);

        if (!transform.position.equals(lastPos))
            body.setPhysicsLocation(Utils.joml2jme(transform.position));
        else transform.position = position;

        if (!transform.rotation.equals(lastRot))
            body.setPhysicsRotation(new Quaternion().fromAngles(transform.rotation.x, transform.rotation.y, transform.rotation.z));
        else transform.rotation = rotation;

        lastPos = transform.position;
        lastRot = transform.rotation;
    }

    public void addVelocity(Vector3f vec) {
        this.body.setLinearVelocity(this.body.getLinearVelocity().add(Utils.joml2jme(vec)));
    }

    public void setVelocity(Vector3f vec) {
        this.body.setLinearVelocity(Utils.joml2jme(vec));
    }

    public Vector3f getVelocity() {
        return Utils.jme2joml(this.body.getLinearVelocity());
    }

    /**
     * If the provided state is true, the rigidbody would be disabled if it's located too far away
     * from the main scene's camera (the distance is higher than 60).
     * <p>
     * By default, the value if false
     *
     * @param state The state to be set
     * */
    public void setOutOfRangeCheck(boolean state) {
        this.outOfRangeCheck = state;
    }

    @Override
    public String getName() {
        return "Rigid Body";
    }

    @Override
    public boolean overlaps(BodyComponent body) {
        Engine.getInstance().getLogger().log(LogLevel.UNIMPLEMENTED, "overlaps method in RigidBodyComponent is not implemented!");
        return false;
    }

    @Override
    public PhysicsCollisionObject getCollisionObject() {
        return this.body;
    }
}

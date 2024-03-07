package dev.artingl.Engine.world.scene.components.phys;

import com.jme3.anim.Joint;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.objects.PhysicsRigidBody;
import com.jme3.export.*;
import com.jme3.math.Quaternion;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.components.Component;
import dev.artingl.Engine.world.scene.components.phys.collider.BaseColliderComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.io.IOException;

public class RigidBodyComponent extends Component implements BodyComponent {
    public boolean enableRotation = true;
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
        this.body.setUserObject(this);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        PhysicsSpace space = body.getPhysicsSpace();
        space.removeCollisionObject(body);
    }

    @Override
    public void init(SceneNode node) throws EngineException {
        super.init(node);
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
                Vector2f v0 = new Vector2f(camera.getPosition().x, camera.getPosition().z);
                Vector2f v1 = new Vector2f(node.getTransform().position.x, node.getTransform().position.z);
                float distance = v0.distance(v1);
                if (distance > 200) {
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

        this.body.setSleepingThresholds(0.1f, 0.1f);

//        BoundingVolume
//        this.body.setSpatial();

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
        Quaternion q = body.getMotionState().getWorldRotationQuat();
        float[] angles = new float[3];
        q.toAngles(angles);
        Vector3f rotation = new Vector3f(
                (float) Math.toDegrees(angles[0]),
                (float) Math.toDegrees(angles[1]),
                (float) Math.toDegrees(angles[2]));

        if (!transform.position.equals(lastPos))
            body.setPhysicsLocation(Utils.joml2jme(transform.getWorldPosition()));
        else transform.setLocalPosition(position);

        if (!transform.rotation.equals(lastRot) || !enableRotation)
            body.setPhysicsRotation(new Quaternion().fromAngles((float) Math.toRadians(transform.rotation.x), (float) Math.toRadians(transform.rotation.y), (float) Math.toRadians(transform.rotation.z)));
        else
            transform.rotation = rotation;

        lastPos = new Vector3f(transform.position);
        lastRot = new Vector3f(transform.rotation);
    }

    public void addLinearVelocity(Vector3f vec) {
        this.body.setLinearVelocity(this.body.getLinearVelocity().add(Utils.joml2jme(vec)));
    }

    public void setLinearVelocity(Vector3f vec) {
        this.body.setLinearVelocity(Utils.joml2jme(vec));
    }

    public Vector3f getLinearVelocity() {
        return Utils.jme2joml(this.body.getLinearVelocity());
    }

    public void addAngularVelocity(Vector3f vec) {
        this.body.setAngularVelocity(this.body.getAngularVelocity().add(Utils.joml2jme(vec)));
    }

    public void setAngularVelocity(Vector3f vec) {
        this.body.setAngularVelocity(Utils.joml2jme(vec));
    }

    public Vector3f getAngularVelocity() {
        return Utils.jme2joml(this.body.getAngularVelocity());
    }

    public float getFriction() {
        return body.getFriction();
    }

    public void setFriction(float v) {
        body.setFriction(v);
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

package dev.artingl.Engine.world.scene.components.phys;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.MeshCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.math.Quaternion;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.components.Component;
import dev.artingl.Engine.world.scene.components.phys.collider.BaseColliderComponent;
import dev.artingl.Engine.world.scene.components.phys.collider.MeshColliderComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Vector3f;

public class RigidBodyComponent extends Component {
    public boolean enableBody = true;
    public boolean isKinematic = false;
    public float mass;

    private final RigidBodyControl rb;
    private Vector3f lastPos, lastRot;

    public RigidBodyComponent() {
        this(1);
    }

    public RigidBodyComponent(float mass) {
        this.mass = mass;
        this.rb = new RigidBodyControl(mass);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        PhysicsSpace space = rb.getPhysicsSpace();
        space.removeCollisionObject(rb);
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

        if (collider instanceof MeshColliderComponent) {
            this.rb.setMass(0);
            this.mass = 0;
        }

        this.rb.setCollisionShape(collider.getShape());

        PhysicsSpace space = node.getScene().getPhysicsSpace();
        this.rb.setPhysicsSpace(space);

        this.updateBody();
    }

    private void updateBody() {
        SceneNode node = getNode();
        if (node == null || !enableBody) {
            this.lastRot = null;
            this.lastPos = null;
            this.rb.setEnabled(false);
            return;
        }

        this.rb.setKinematic(this.isKinematic);
        this.rb.setEnabled(true);

        TransformComponent transform = node.getTransform();
        Vector3f position = Utils.jme2joml(rb.getPhysicsLocation());
        Vector3f rotation = new Vector3f(
                (float) Math.toDegrees(rb.getPhysicsRotation().getX()) + 180,
                (float) Math.toDegrees(rb.getPhysicsRotation().getY()) + 180,
                (float) Math.toDegrees(rb.getPhysicsRotation().getZ()) + 180);

        if (!transform.position.equals(lastPos))
            rb.setPhysicsLocation(Utils.joml2jme(transform.position));
        else transform.position = position;

        if (!transform.rotation.equals(lastRot))
            rb.setPhysicsRotation(new Quaternion().fromAngles(transform.rotation.x, transform.rotation.y, transform.rotation.z));
        else transform.rotation = rotation;

        lastPos = transform.position;
        lastRot = transform.rotation;
    }

    @Override
    public void disable() {
        enableBody = false;
    }

    @Override
    public void enable() {
        enableBody = true;
    }

    public void addVelocity(Vector3f vec) {
        this.rb.setLinearVelocity(this.rb.getLinearVelocity().add(Utils.joml2jme(vec)));
    }

    public void setVelocity(Vector3f vec) {
        this.rb.setLinearVelocity(Utils.joml2jme(vec));
    }

    public Vector3f getVelocity() {
        return Utils.jme2joml(this.rb.getLinearVelocity());
    }

    @Override
    public String getName() {
        return "Dynamic Rigid Body";
    }
}

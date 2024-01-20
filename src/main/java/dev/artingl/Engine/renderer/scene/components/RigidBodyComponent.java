package dev.artingl.Engine.renderer.scene.components;

import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.scene.components.transform.TransformComponent;
import dev.artingl.Engine.renderer.scene.nodes.SceneNode;
import dev.artingl.Engine.timer.Timer;
import org.joml.Vector3f;
import org.ode4j.math.DMatrix3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.DBody;
import org.ode4j.ode.OdeHelper;
import org.ode4j.ode.internal.DxMass;

import static org.ode4j.ode.DRotation.dRFromAxisAndAngle;

public class RigidBodyComponent extends Component {
    public boolean enableBody = true;
    public boolean enableRotation = true;
    public float mass = 0.8f;

    private DBody body;

    private Vector3f offset = new Vector3f();
    private Vector3f lastPosition;
    private Vector3f lastRotation = new Vector3f();

    @Override
    public void cleanup() {
        super.cleanup();
        getNode().getScene().unlockSpace(() -> this.body.destroy());
    }

    @Override
    public void init(SceneNode node) throws EngineException {
        super.init(node);
        if (this.body != null) {
            this.body.destroy();
        }
        this.body = OdeHelper.createBody(node.getScene().getWorld());
        this.body.setMovedCallback(b -> this.updateBody());
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        if (enableBody) body.enable();
        else body.disable();

        ((DxMass) this.body.getMass()).setMass(mass);
        this.updateBody();
    }

    @Override
    public void disable() {
        enableBody = false;
    }

    @Override
    public void enable() {
        enableBody = true;
    }

    private void updateBody() {
        TransformComponent transform = getNode().getTransform();

        if (!transform.position.equals(this.lastPosition) || !enableBody) {
            // We need to update body's position since it was changed somewhere outside rigid body class
            this.body.setPosition(
                    transform.position.x,
                    transform.position.z,
                    transform.position.y
            );
        } else {
            transform.position.set(
                    body.getPosition().get0() + this.offset.x,
                    body.getPosition().get2() + this.offset.y,
                    body.getPosition().get1() + this.offset.z
            );
        }

        if (enableRotation) {
            double r11 = body.getRotation().get00(), r12 = body.getRotation().get01(), r13 = body.getRotation().get02();
            double r21 = body.getRotation().get10(), r22 = body.getRotation().get11(), r23 = body.getRotation().get12();
            double r31 = body.getRotation().get20(), r32 = body.getRotation().get21(), r33 = body.getRotation().get22();
            double yaw = 0, pitch, roll;

            if (r31 != 1 && r31 != -1) {
                double pitch_1 = -1 * Math.asin(r31);
                double pitch_2 = Math.PI - pitch_1;
                double roll_1 = Math.atan2(r32 / Math.cos(pitch_1), r33 / Math.cos(pitch_1));
                double roll_2 = Math.atan2(r32 / Math.cos(pitch_2), r33 / Math.cos(pitch_2));
                double yaw_1 = Math.atan2(r21 / Math.cos(pitch_1), r11 / Math.cos(pitch_1));
                double yaw_2 = Math.atan2(r21 / Math.cos(pitch_2), r11 / Math.cos(pitch_2));

                pitch = pitch_1;
                roll = roll_1;
                yaw = yaw_1;

            }
            else {
                if (r31 == -1) {
                    pitch = Math.PI / 2;
                    roll = yaw + Math.atan2(r12, r13);
                }
                else {
                    pitch = -Math.PI / 2;
                    roll = -1 * yaw + Math.atan2(-1 * r12, -1 * r13);
                }
            }

            transform.rotation.set(
                    Math.toDegrees(roll),
                    Math.toDegrees(pitch),
                    Math.toDegrees(yaw)
            );
            lastRotation = new Vector3f(transform.rotation);
        }
        else {
            DMatrix3 R = new DMatrix3();
            R.setIdentity();
            dRFromAxisAndAngle(R, 1, 0, 0, Math.toRadians(this.lastRotation.x));
            dRFromAxisAndAngle(R, 0, 1, 0, Math.toRadians(this.lastRotation.y));
            dRFromAxisAndAngle(R, 0, 0, 1, Math.toRadians(this.lastRotation.z));
            this.body.setRotation(R);
        }

        this.lastPosition = transform.position;
    }

    public void addVelocity(float x, float y, float z) {
        body.addLinearVel(x, z, y);
    }

    public void addVelocity(Vector3f vec) {
        body.addLinearVel(vec.x, vec.z, vec.y);
    }

    public void addForce(float x, float y, float z) {
        body.addForce(x, z, y);
    }

    public void addForce(Vector3f vec) {
        body.addForce(vec.x, vec.z, vec.y);
    }

    public void setVelocity(Vector3f vec) {
        body.setLinearVel(vec.x, vec.z, vec.y);
    }

    public void setVelocity(float x, float y, float z) {
        body.setLinearVel(x, z, y);
    }

    public Vector3f getVelocity() {
        DVector3C vel = body.getLinearVel();
        return new Vector3f((float) vel.get0(), (float) vel.get2(), (float) vel.get1());
    }

    @Override
    public String getName() {
        return "RigidBody";
    }

    public DBody getBody() {
        return body;
    }

    public void setOffset(Vector3f yOffset) {
        this.offset = yOffset;
    }
}

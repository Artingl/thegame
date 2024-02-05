package dev.artingl.Engine.world.scene.components.phys;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.math.Quaternion;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.Component;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Vector3f;

public class CharacterControlComponent extends Component {

    public boolean enableController = false;
    public float height = 2;

    private final CharacterControl controller;
    private CapsuleCollisionShape shape;
    private Vector3f lastPos;
    private Vector3f offset;
    private float oldHeight;
    private boolean addedToSpace;

    public CharacterControlComponent() {
        this.shape = new CapsuleCollisionShape(1, this.height);
        this.controller = new CharacterControl(shape, 0.01f);
        this.oldHeight = this.height;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        SceneNode node = getNode();

        if (node == null)
            return;

        BaseScene scene = node.getScene();
        PhysicsSpace space = scene.getPhysicsSpace();
        space.remove(this.controller);
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);
        SceneNode node = getNode();

        if (node == null)
            return;

        // Add the character to the space if we haven't yet
        if (!addedToSpace) {
            BaseScene scene = node.getScene();
            PhysicsSpace space = scene.getPhysicsSpace();
            space.add(this.controller);
            this.addedToSpace = true;
        }

        // Update the collision shape if the character height has changed
        if (this.oldHeight != this.height) {
            this.oldHeight = this.height;
            this.shape = new CapsuleCollisionShape(1, this.height);
            this.controller.setCollisionShape(this.shape);
        }

        this.controller.setEnabled(enableController);
        if (!enableController) {
            lastPos = null;
            return;
        }

        // Update transforms
        TransformComponent transform = node.getTransform();
        Vector3f position = Utils.jme2joml(this.controller.getPhysicsLocation());

        if (!transform.position.equals(lastPos)) {
            this.controller.setPhysicsLocation(Utils.joml2jme(transform.position));
        }
        else transform.position = new Vector3f(transform.position).add(position.add(offset).sub(transform.position));
        lastPos = transform.position;
    }

    public boolean onGround() {
        return this.controller.onGround();
    }

    public void jump() {
        this.controller.jump();
    }

    public void setJumpSpeed(float v) {
        this.controller.setJumpSpeed(v);
    }

    public void setGravity(float v) {
        this.controller.setGravity(new com.jme3.math.Vector3f(0, v, 0));
    }

    public void addVelocity(Vector3f vec) {
        com.jme3.math.Vector3f vel = new com.jme3.math.Vector3f();
        this.controller.getLinearVelocity(vel);
        this.controller.setLinearVelocity(vel.add(Utils.joml2jme(vec)));
    }

    public void setVelocity(Vector3f vec) {
        this.controller.setLinearVelocity(Utils.joml2jme(vec));
    }

    public Vector3f getVelocity() {
        com.jme3.math.Vector3f vel = new com.jme3.math.Vector3f();
        this.controller.getLinearVelocity(vel);
        return Utils.jme2joml(vel);
    }

    public void setWalkingDirection(Vector3f v) {
        this.controller.setWalkDirection(Utils.joml2jme(v));
    }

    public Vector3f getWalkingDirection() {
        return Utils.jme2joml(this.controller.getWalkDirection());
    }

    public void setOffset(Vector3f offset) {
        this.offset = offset;
    }

    @Override
    public String getName() {
        return "Character Controller";
    }
}

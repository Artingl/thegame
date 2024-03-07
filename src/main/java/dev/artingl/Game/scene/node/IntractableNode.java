package dev.artingl.Game.scene.node;

import dev.artingl.Engine.resources.Text;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.world.scene.components.phys.RigidBodyComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import org.joml.Vector3f;

public abstract class IntractableNode extends SceneNode {

    private boolean isHeld;
    private Vector3f positionBeforeHeld;

    public abstract Text getTooltipTitle();

    public boolean isStatic() {
        return true;
    }

    public boolean canInteract() {
        return true;
    }

    public void interact(SceneNode node) {
    }

    public void holdObject() {
        if (!isStatic()) {
            this.isHeld = true;
            this.positionBeforeHeld = getTransform().position;
        }
    }

    public void placeHeldObject() {
        if (isStatic())
            return;
        this.isHeld = false;
        RigidBodyComponent rb = getComponent(RigidBodyComponent.class);
        if (rb != null)
            rb.setLinearVelocity(rb.getLinearVelocity().mul(0.3f));
    }

    public void throwHeldObject(float strength) {
        if (isStatic())
            return;
        this.isHeld = false;

        RigidBodyComponent rb = getComponent(RigidBodyComponent.class);
        BaseScene scene = getScene();

        if (rb != null && scene != null) {
            CameraNode cameraNode = scene.getMainCamera();

            if (!(cameraNode instanceof PlayerControllerNode controller))
                return;

            TransformComponent cameraTransform = cameraNode.getTransform();
            Vector3f cameraPosition = new Vector3f(cameraTransform.position);
            Vector3f cameraRotation = new Vector3f(cameraTransform.rotation);
            float m = (float) Math.cos(Math.toRadians(-cameraRotation.x));
            float dx = (float) Math.sin(Math.toRadians(cameraRotation.y)) * m;
            float dz = (float) -Math.cos(Math.toRadians(cameraRotation.y)) * m;
            float dy = (float) Math.sin(Math.toRadians(-cameraRotation.x));

            // Calculate velocity for the objects to move it
            Vector3f newPos = new Vector3f(cameraPosition).add(new Vector3f(controller.handRayDistance + strength / rb.mass).mul(dx, dy, dz));
            rb.setLinearVelocity(newPos.sub(getTransform().position).mul(10 / rb.mass));
        }
    }

    public boolean isObjectHeld() {
        return isHeld;
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        RigidBodyComponent rb = getComponent(RigidBodyComponent.class);
        BaseScene scene = getScene();

        if (rb == null)
            return;
//        rb.enableRotation = !isObjectHeld();

        if (isObjectHeld() && !isStatic() && scene != null) {
            Vector3f pos = new Vector3f(getTransform().position);
            CameraNode cameraNode = scene.getMainCamera();

            if (!(cameraNode instanceof PlayerControllerNode controller))
                return;

            TransformComponent cameraTransform = cameraNode.getTransform();
            Vector3f cameraPosition = new Vector3f(cameraTransform.position);
            Vector3f cameraRotation = new Vector3f(cameraTransform.rotation);
            float m = (float) Math.cos(Math.toRadians(-cameraRotation.x));
            float dx = (float) Math.sin(Math.toRadians(cameraRotation.y)) * m;
            float dz = (float) -Math.cos(Math.toRadians(cameraRotation.y)) * m;
            float dy = (float) Math.sin(Math.toRadians(-cameraRotation.x));

            // Calculate velocity for the objects to move it
            Vector3f newPos = new Vector3f(cameraPosition).add(new Vector3f(controller.handRayDistance).mul(dx, dy, dz));
            Vector3f velocity = newPos.sub(pos).mul(12 / Math.max(1, rb.mass)).min(new Vector3f(32 / Math.max(1, rb.mass)));
            velocity.y /= Math.max(1, rb.mass);
            rb.setLinearVelocity(velocity.add(rb.getLinearVelocity().sub(velocity).mul(0.5f)));
            rb.setAngularVelocity(new Vector3f());
        }
    }
}

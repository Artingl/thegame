package dev.artingl.Game.scene.node;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.input.IInput;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.input.InputKeys;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.misc.noise.PerlinNoise;
import dev.artingl.Engine.renderer.scene.components.CameraComponent;
import dev.artingl.Engine.renderer.scene.components.RigidBodyComponent;
import dev.artingl.Engine.renderer.scene.components.collider.BoxColliderComponent;
import dev.artingl.Engine.renderer.scene.components.transform.TransformComponent;
import dev.artingl.Engine.renderer.scene.nodes.CameraNode;
import dev.artingl.Engine.timer.Timer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class CameraControlNode extends CameraNode implements IInput {

    public float defaultFov = 0;
    public float sprintFov = 0;

    public float jumpStrength = 5;
    public float movementSpeed = 2;
    public float rotationSpeed = 0.7f;
    public boolean captureControl = true;

    // Required for sprinting
    private boolean fovState = false;

    private Vector3f oldPosDelta = new Vector3f();
    private final BoxColliderComponent collider;
    private final RigidBodyComponent rigidBody;
    private boolean onGround;

    public CameraControlNode() {
        super();

        /* Initialize camera collider and rigid body */
        this.rigidBody = new RigidBodyComponent();
        this.collider = new BoxColliderComponent(new Vector3f(1, 2, 1));
        this.rigidBody.enableRotation = false;
        this.rigidBody.enableBody = true;

        this.addComponent(collider);
        this.addComponent(this.rigidBody);

        this.collider.setCollisionHandler(() -> this.onGround = true);
    }

    @Override
    public void init() throws EngineException {
        super.init();

        /* Initialize the camera for the 3D space */
        CameraComponent camera = getComponent(CameraComponent.class);
        camera.type = Type.PERSPECTIVE;
        camera.zFar = 300;
        camera.backgroundColor = Color.BLACK;

        /* Make sure the camera is main on the scene!!! */
        this.getScene().setMainCamera(this);

        Input input = Engine.getInstance().getInput();
        input.subscribe(this);

        this.defaultFov = getFov();
        this.sprintFov = this.defaultFov + 12;
    }

    @Override
    public void cleanup() {
        super.cleanup();

        Input input = Engine.getInstance().getInput();
        input.unsubscribe(this);
    }

    @Override
    public void tick(Timer timer) {
        super.tick(timer);

        this.rigidBody.setYOffset(this.collider.length.y);

        /* Some useful refs */
        TransformComponent cameraTransform = getTransform();
        Engine engine = this.getScene().getEngine();
        Input input = engine.getInput();

        /* Capture cursor if input is captured */
        input.captureCursor(this.captureControl);

        if (this.captureControl) {
            /* Control camera only if input is captured */
            Vector3f posDelta = new Vector3f();
            Vector2f rotDelta = input.getCursorDelta();

            /*
             * Move the camera in the space on keyboard
             * TODO: Don't hardcode input keys
             * */
            if (input.getKeyboardState(InputKeys.KEY_W).isHeld()) { posDelta.z -= movementSpeed; }
            if (input.getKeyboardState(InputKeys.KEY_S).isHeld()) { posDelta.z += movementSpeed; }
            if (input.getKeyboardState(InputKeys.KEY_A).isHeld()) { posDelta.x -= movementSpeed; }
            if (input.getKeyboardState(InputKeys.KEY_D).isHeld()) { posDelta.x += movementSpeed; }

            /* Sprint */
            if (input.getKeyboardState(InputKeys.KEY_LEFT_SHIFT).isHeld() && posDelta.z < 0) {
                if (!fovState) {
                    this.smoothFov(this.sprintFov);
                    this.fovState = !this.fovState;
                }
                this.movementSpeed = 5;
            }
            else {
                if (fovState) {
                    this.smoothFov(this.defaultFov);
                    this.fovState = !this.fovState;
                }
                this.movementSpeed = 2;
            }

            /* Sneak */
            if (input.getKeyboardState(InputKeys.KEY_LEFT_CONTROL).isHeld()) this.collider.length.y = 1.2f;
            else this.collider.length.y = 2f;

            /* Jump */
            if (input.getKeyboardState(InputKeys.KEY_SPACE).isHeld() && this.onGround) posDelta.y += jumpStrength;
            this.onGround = false;

            /* Make new position vector */
            float sin = (float) Math.sin(Math.toRadians(cameraTransform.rotation.y));
            float cos = (float) Math.cos(Math.toRadians(cameraTransform.rotation.y));
            Vector3f newPos = new Vector3f(posDelta.x * cos - posDelta.z * sin, posDelta.y, posDelta.z * cos + posDelta.x * sin);

            /* Update rigid body's velocity to move the camera */
            this.oldPosDelta.set(
                    oldPosDelta.x + (newPos.x - oldPosDelta.x) * 0.1f,
                    0,
                    oldPosDelta.z + (newPos.z - oldPosDelta.z) * 0.1f
            );
            this.rigidBody.addVelocity(0, newPos.y, 0);
            this.rigidBody.setVelocity(
                    oldPosDelta.x,
                    Math.min(this.rigidBody.getVelocity().y, jumpStrength),
                    oldPosDelta.z
            );

            /* Update camera rotation */
            cameraTransform.rotation.add(rotDelta.y * rotationSpeed, rotDelta.x * rotationSpeed, 0);
            cameraTransform.rotation.x = Math.max(-90, Math.min(90, cameraTransform.rotation.x));
        }

        this.rigidBody.enableBody = this.captureControl;
    }

    public CameraComponent getCamera() {
        return (CameraComponent) getComponent(CameraComponent.class);
    }

    @Override
    public void keyboardEvent(Input input, Input.State state, int key) {
        /* Disable/enable capturing on escape */
        if (key == InputKeys.KEY_ESCAPE && state.isPressed()) {
            this.captureControl = !this.captureControl;
        }
    }

    @Override
    public void mouseButtonEvent(Input input, Input.State state, int key) {
    }

    @Override
    public void mouseWheelEvent(Input input, int wheelX, int wheelY) {
    }

    @Override
    public void mouseMoveEvent(Input input, float x, float y) {
    }
}

package dev.artingl.Game.scene.node;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.input.InputListener;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.input.InputKeys;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.world.scene.components.CameraComponent;
import dev.artingl.Engine.world.scene.components.phys.CharacterControlComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.timer.Timer;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class CameraControlNode extends CameraNode implements InputListener {

    public float defaultFov = 0;
    public float sprintFov = 0;

    public float jumpStrength = 5;
    public float movementSpeed = 2;
    public float rotationSpeed = 1.1f;
    public boolean captureControl = true;

    // Required for sprinting
    private boolean fovState = false;
    private boolean isSneaking = false;

    private Vector3f oldPosDelta = new Vector3f();
    private Vector2f oldRotDelta = new Vector2f();
    private final CharacterControlComponent controller;

    public CameraControlNode() {
        super();

        /* Initialize camera collider and rigid body */
        this.controller = new CharacterControlComponent();
        this.addComponent(controller);
    }

    @Override
    public void init() throws EngineException {
        super.init();

        /* Initialize the camera for the 3D space */
        CameraComponent camera = getCamera();
        camera.type = Type.PERSPECTIVE;
        camera.zFar = 400;
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

        this.controller.height = this.isSneaking ? 1.3f : 2f;
        this.controller.setOffset(new Vector3f(0, this.controller.height, 0));

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
            this.oldRotDelta = this.oldRotDelta.add(new Vector2f(rotDelta).sub(this.oldRotDelta)).mul(20 / timer.getTickPerSecond());

            /*
             * Move the camera in the space on keyboard
             * TODO: Don't hardcode input keys
             * */
            if (input.getKeyboardState(InputKeys.KEY_W).isHeld()) { posDelta.z -= movementSpeed; }
            if (input.getKeyboardState(InputKeys.KEY_S).isHeld()) { posDelta.z += movementSpeed; }
            if (input.getKeyboardState(InputKeys.KEY_A).isHeld()) { posDelta.x -= movementSpeed; }
            if (input.getKeyboardState(InputKeys.KEY_D).isHeld()) { posDelta.x += movementSpeed; }

            /* Sprint */
            if (input.getKeyboardState(InputKeys.KEY_LEFT_SHIFT).isHeld() && !this.isSneaking && posDelta.z < 0) {
                if (!fovState) {
                    this.smoothFov(this.sprintFov);
                    this.fovState = !this.fovState;
                }
                this.movementSpeed = 0.4f;
            }
            else {
                if (fovState) {
                    this.smoothFov(this.defaultFov);
                    this.fovState = !this.fovState;
                }
                this.movementSpeed = 0.2f;
            }

            /* Sneak */
            this.isSneaking = input.getKeyboardState(InputKeys.KEY_LEFT_CONTROL).isHeld();

            /* Jump */
            this.controller.setJumpSpeed(this.isSneaking ? 10 : 15);
            if (input.getKeyboardState(InputKeys.KEY_SPACE).isHeld()
                    && this.controller.onGround()) this.controller.jump();

            /* Make new position vector */
            float sin = (float) Math.sin(Math.toRadians(cameraTransform.rotation.y));
            float cos = (float) Math.cos(Math.toRadians(cameraTransform.rotation.y));
            Vector3f newPos = new Vector3f(posDelta.x * cos - posDelta.z * sin, posDelta.y, posDelta.z * cos + posDelta.x * sin);

            /* Update rigid body's velocity to move the camera */
            this.controller.setWalkingDirection(new Vector3f(newPos.x, 0, newPos.z));

            /* Update camera rotation */
            cameraTransform.rotation.add(this.oldRotDelta.y * rotationSpeed, this.oldRotDelta.x * rotationSpeed, 0);
            cameraTransform.rotation.x = Math.max(-90, Math.min(90, cameraTransform.rotation.x));

        }

        this.controller.enableController = this.captureControl;
    }

    public CameraComponent getCamera() {
        return getComponent(CameraComponent.class);
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

package dev.artingl.Game.scene.node;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.input.InputListener;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.input.InputKeys;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.renderer.viewport.IViewport;
import dev.artingl.Engine.world.scene.components.CameraComponent;
import dev.artingl.Engine.world.scene.components.phys.CharacterControlComponent;
import dev.artingl.Engine.world.scene.components.transform.TransformComponent;
import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.timer.Timer;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PlayerControllerNode extends CameraNode implements InputListener {

    public float defaultFov = 0;
    public float sprintFov = 0;
    public float movementSpeed = 0.25f;
    public float cameraLeanAngle = 4f;
    public float rotationSpeed = 0.2f;
    public boolean captureControl = true;

    private final CharacterControlComponent controller;
    private Vector3f oldPosDelta = new Vector3f();
    private boolean fovState = false;
    private boolean isSneaking = false;
    private boolean hasJumped = false;
    private float currentFov = 0;
    private float speedModifier = 1;
    private float zoomModifier = 0;
    private float zoomSpeed = 1;

    public PlayerControllerNode() {
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
        camera.type = IViewport.Type.PERSPECTIVE;
        camera.farPlane = 400;
        camera.postprocessing = true;

        /* Make sure the camera is main on the scene!!! */
        this.getScene().setMainCamera(this);

        Input input = Engine.getInstance().getInput();
        input.subscribe(this);

        this.defaultFov = this.getFov();
        this.currentFov = this.defaultFov;
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
        CameraComponent camera = getCamera();
        Engine engine = this.getScene().getEngine();
        Input input = engine.getInput();

        /* Capture cursor if input is captured */
        input.captureCursor(this.captureControl);

        if (this.captureControl) {
            /* Control camera only if input is captured */
            Vector3f posDelta = new Vector3f();
            Vector2f rotDelta = input.getCursorDelta();
            float speed = this.movementSpeed * this.speedModifier;

            /*
             * Move the camera in the space on keyboard
             * TODO: Don't hardcode input keys
             * */
            if (input.getKeyboardState(InputKeys.KEY_W).isHeld()) { posDelta.z -= speed; }
            if (input.getKeyboardState(InputKeys.KEY_S).isHeld()) { posDelta.z += speed; }
            if (input.getKeyboardState(InputKeys.KEY_A).isHeld()) { posDelta.x -= speed; }
            if (input.getKeyboardState(InputKeys.KEY_D).isHeld()) { posDelta.x += speed; }
            this.oldPosDelta = this.oldPosDelta.add(new Vector3f(posDelta).sub(this.oldPosDelta).mul(0.1f));

            /* Sneak
             * TODO: sneaking crashes the game for some reason.... */
//            this.isSneaking = input.getKeyboardState(InputKeys.KEY_LEFT_CONTROL).isHeld();

            /* Sprint */
            if (input.getKeyboardState(InputKeys.KEY_LEFT_SHIFT).isHeld() && !this.isSneaking && posDelta.z < 0) {
                if (!fovState && this.zoomModifier == 0) {
                    this.smoothFov(this.sprintFov);
                    this.currentFov = this.sprintFov;
                    this.fovState = !this.fovState;
                }
                this.speedModifier = 1.5f;
            }
            else {
                if (fovState && this.zoomModifier == 0) {
                    this.smoothFov(this.defaultFov);
                    this.currentFov = this.defaultFov;
                    this.fovState = !this.fovState;
                }
                this.speedModifier = 1;
            }

            if (isSneaking)
                this.speedModifier = 0.3f;

            /* Jump */
            this.controller.setJumpSpeed(this.isSneaking ? 15 : 21);
            if (input.getKeyboardState(InputKeys.KEY_SPACE).isHeld()
                    && this.controller.onGround() && !this.hasJumped) {
                this.controller.jump();
                this.hasJumped = true;
            }
            else if (!input.getKeyboardState(InputKeys.KEY_SPACE).isHeld()) this.hasJumped = false;

            /* Make new position vector */
            float sin = (float) Math.sin(Math.toRadians(cameraTransform.rotation.y));
            float cos = (float) Math.cos(Math.toRadians(cameraTransform.rotation.y));
            Vector3f newPos = new Vector3f(oldPosDelta.x * cos - oldPosDelta.z * sin, oldPosDelta.y, oldPosDelta.z * cos + oldPosDelta.x * sin);

            /* Update rigid body's velocity to move the camera */
            this.controller.setWalkingDirection(new Vector3f(newPos.x, 0, newPos.z));

            /* Update camera rotation */
            cameraTransform.rotation.add(rotDelta.y * rotationSpeed, rotDelta.x * rotationSpeed, 0);
            cameraTransform.rotation.x = Math.max(-90, Math.min(90, cameraTransform.rotation.x));

            /* Make the viewport "lean" to the side we're moving right now */
            Vector3f projRot = getProjectionRotation();
            float axis = posDelta.x == 0 ? 0 : posDelta.x > 0 ? 1 : -1;
            float targetAngle = this.cameraLeanAngle * axis;
            float leanSpeed = (this.isSneaking ? 4 : 8) / timer.getTickPerSecond();

            setProjectionRotation(new Vector3f(0, 0, projRot.z + (targetAngle - projRot.z) * leanSpeed));

            /* Change camera fov based on current zoom value */
            float zoomSpeed = 6 / timer.getTickPerSecond();
            camera.fov = camera.fov + ((this.currentFov - this.zoomModifier) - camera.fov) * zoomSpeed;
        }

//        cameraTransform.rotation.add(16 / timer.getTickPerSecond(), 16 / timer.getTickPerSecond(), 16 / timer.getTickPerSecond());

        this.controller.setGravity(-48);
        this.zoomSpeed = Math.max(0.5f, Math.min(50, this.zoomSpeed * 1.05f));
        this.zoomModifier = Math.min(Math.max(0, this.zoomModifier - zoomSpeed / timer.getTickPerSecond()), 70);
        this.controller.enableController = this.captureControl;

//        cameraTransform.rotation.x = Utils.randInt(0, 360);
//        cameraTransform.rotation.y = Utils.randInt(0, 360);
//        cameraTransform.rotation.z = Utils.randInt(0, 360);
//
//        cameraTransform.position.x = Utils.randInt(-200, 200);
//        cameraTransform.position.y = 10;
//        cameraTransform.position.z = Utils.randInt(-200, 200);
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
        // Camera zoom effect on wheel
        if (this.captureControl) {
            this.zoomModifier += wheelY * 6;
            this.zoomSpeed = 0;
        }
    }

    @Override
    public void mouseMoveEvent(Input input, float x, float y) {
    }
}

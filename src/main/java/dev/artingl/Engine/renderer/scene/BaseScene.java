package dev.artingl.Engine.renderer.scene;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.input.IInput;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.renderer.scene.nodes.CameraNode;
import dev.artingl.Engine.renderer.scene.nodes.SceneNode;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.timer.ITick;
import dev.artingl.Engine.timer.Timer;
import org.jetbrains.annotations.Nullable;
import org.ode4j.math.DMatrix3;
import org.ode4j.ode.*;
import org.ode4j.ode.internal.DxSpace;
import org.ode4j.ode.internal.DxWorld;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

import static org.ode4j.ode.OdeConstants.*;
import static org.ode4j.ode.OdeHelper.areConnectedExcluding;

public class BaseScene implements ITick, IInput {
    private final Logger logger;
    private final Map<UUID, SceneNode> nodesList;

    private final DxWorld world;
    private final DxSpace space;
    private final DJointGroup contactGroup;

    private final Collection<Runnable> spaceCallbacks;
    private final Collection<SceneNode> lazyNodes;
    private Map<DGeom, Runnable> worldCollisionHandlers;

    private CameraNode mainCamera;
    private boolean isInitialized;

    public BaseScene() {
        this.nodesList = new ConcurrentHashMap<>();
        this.lazyNodes = new ConcurrentLinkedDeque<>();
        this.spaceCallbacks = new ConcurrentLinkedDeque<>();
        this.worldCollisionHandlers = new ConcurrentHashMap<>();
        this.logger = getEngine().getLogger();
        this.isInitialized = false;

        this.world = (DxWorld) OdeHelper.createWorld();
        this.space = (DxSpace) OdeHelper.createHashSpace(null);
        this.contactGroup = OdeHelper.createJointGroup();

        this.world.setGravity(0, 0, -4.8);
//        this.world.setQuickStepNumIterations(16);
        this.world.setContactMaxCorrectingVel(16);
        this.world.setContactSurfaceLayer(0.001);
        this.world.setCFM(1e-5);
        this.world.setAutoDisableFlag(true);
    }

    /**
     * Set the main camera node for the scene.
     *
     * @param node The node to be set as camera
     */
    public void setMainCamera(SceneNode node) {
        if (node instanceof CameraNode cam)
            this.mainCamera = cam;
    }

    @Nullable
    public CameraNode getMainCamera() {
        return mainCamera;
    }

    /**
     * Add node to the scene
     *
     * @param node Target node
     */
    public void addNode(SceneNode node) {
        if (node == null) {
            this.logger.log(LogLevel.WARNING, "Cannot add NULL node to the scene.");
            return;
        }

        node.attach(makeNametag(node), this);
        this.nodesList.put(node.getUUID(), node);
        this.lazyNodes.add(node);

        // Try to set the node as main camera if we don't have
        if (mainCamera == null) {
            this.setMainCamera(node);
        }
    }

    /**
     * Add a child to the node.
     *
     * @param parent The parent's UUID
     * @param child  The target child
     * @return If no nodes exist under the target UUID, false is returned. Otherwise true
     */
    public boolean addChild(UUID parent, SceneNode child) {
        SceneNode node = this.nodesList.get(parent);

        if (node != null) {
            child.attach(makeNametag(child), this, node);
            node.addChild(child.getUUID());
            this.nodesList.put(child.getUUID(), child);
            this.lazyNodes.add(child);

            // Try to set the node as main camera if we don't have
            if (mainCamera == null) {
                this.setMainCamera(node);
            }
            return true;
        }

        return false;
    }

    /**
     * Add a child to the node.
     *
     * @param parent The target parent
     * @param child  The target child
     */
    public boolean addChild(SceneNode parent, SceneNode child) {
        return addChild(parent.getUUID(), child);
    }

    /**
     * Removes node from the scene.
     *
     * @param node The target node.
     * @return True if node exists on the scene and was removed successfully, otherwise false.
     */
    public boolean removeNode(SceneNode node) {
        // Check that we have the node on the scene
        if (this.nodesList.containsKey(node.getUUID())) {
            // Remove all children firstly
            for (UUID child : node.getChildren()) {
                this.removeNode(child);
            }

            // Cleanup, detach the node and remove from the node's list
            node.cleanup();
            node.detach();
            this.nodesList.remove(node.getUUID());

            /* If we have camera on the scene and the node we want to
             * remove has the same ID as the main camera, set the main camera to null
             */
            if (mainCamera != null && this.mainCamera.getUUID() == node.getUUID()) {
                this.mainCamera = null;
            }
            return true;
        }

        return false;
    }

    /**
     * Removes node by the UUID from the scene.
     *
     * @param uuid The target node's UUID.
     * @return True if node exists on the scene and was removed successfully, otherwise false.
     */
    public boolean removeNode(UUID uuid) {
        SceneNode node = this.nodesList.get(uuid);

        if (node != null) {
            return this.removeNode(node);
        }

        return false;
    }

    /**
     * Get node by its UUID
     *
     * @param uuid Node's uuid
     */
    public SceneNode getNode(UUID uuid) {
        return this.nodesList.get(uuid);
    }

    /**
     * Get all nodes assigned to the scene
     */
    public Collection<SceneNode> getNodes() {
        return this.nodesList.values();
    }

    /**
     * Gets called only once to initialize the scene
     */
    public void init() throws EngineException {
    }

    /**
     * Gets called before scene becomes active
     */
    public void active() {
    }

    /**
     * Gets called before scene becomes inactive
     */
    public void inactive() {
    }

    /**
     * Get the engine instance
     */
    public Engine getEngine() {
        return Engine.getInstance();
    }

    /**
     * Called every frame
     *
     * @param context Current renderer context
     */
    public void render(RenderContext context) {
    }

    /**
     * Called every frame to prepare the render environment.
     * You should not overload this function if you just want to do
     * your own rendering on top of the scene (overload {@link BaseScene#render(RenderContext)} then)
     *
     * @param context Current renderer context
     */
    public void prepareRender(RenderContext context) throws EngineException {
        // Initialize the scene if we haven't yet
        if (!this.isInitialized) {
            this.isInitialized = true;
            this.init();
        }

        // Initialize all lazy nodes
        synchronized (this.lazyNodes) {
            for (SceneNode node : this.lazyNodes) {
                node.init();
            }

            this.lazyNodes.clear();
        }

        // Update the viewport with the main camera if it exists
        if (mainCamera != null) {
            Viewport viewport = getEngine().getRenderer().getViewport();
            viewport.setViewport(mainCamera);
        } else {
            // Print warning that we don't have any camera on the scene
            this.logger.log(LogLevel.WARNING, "No camera is set for the scene!");
        }

        // Render nodes
        for (SceneNode node : nodesList.values()) {
            node.render(context);
        }

        this.render(context);
    }

    /**
     * Make unique nametag for the node
     *
     * @param target Target node
     */
    protected String makeNametag(SceneNode target) {
        return target.getClass().getSimpleName() + "(" + target.getUUID() + ")";
//        String className = target.getClass().getSimpleName();
//        int idx = 0;
//
//        do {
//            boolean foundDuplicate = false;
//
//            for (SceneNode node: nodesList.values()) {
//                if (node.getNametag().equals(className + idx)) {
//                    idx++;
//                    foundDuplicate = true;
//                    break;
//                }
//            }
//
//            if (!foundDuplicate)
//                return className + idx;
//        }
//        while (true);
    }

    /**
     * Get collision space
     */
    public DSpace getSpace() {
        return space;
    }

    /**
     * Get collision world
     */
    public DWorld getWorld() {
        return world;
    }

    @Override
    public void tick(Timer timer) {
        // Call all callbacks which expect the space not to be locked
        synchronized (this.spaceCallbacks) {
            for (Runnable callback : this.spaceCallbacks) {
                callback.run();
            }
            this.spaceCallbacks.clear();
        }

        float steps = 3 / timer.getTickPerSecond();

        long start = System.currentTimeMillis();
        this.space.collide(null, this::collideCallback);
//        System.out.println(System.currentTimeMillis() - start);

        this.world.quickStep(steps);
        this.contactGroup.empty();
    }

    private void collideCallback(Object data, DGeom o1, DGeom o2) {
        int ncontacts = 32;
        int i;

        DBody b1 = o1.getBody();
        DBody b2 = o2.getBody();
        if (b1 != null && b2 != null && areConnectedExcluding(b1, b2, DContactJoint.class)) {
            return;
        }

        DContactBuffer contacts = new DContactBuffer(ncontacts);
        for (i = 0; i < ncontacts; i++) {
            DContact contact = contacts.get(i);
            contact.surface.mode = dContactBounce | dContactSoftCFM;
            contact.surface.mu = dInfinity;
            contact.surface.mu2 = 0;
            contact.surface.bounce = 0.1;
            contact.surface.bounce_vel = 0.1;
            contact.surface.soft_cfm = 0.01;
        }
        int numc = OdeHelper.collide(o1, o2, ncontacts, contacts.getGeomBuffer());
        boolean eventSent = false;

        if (numc != 0) {
            for (i = 0; i < numc; i++) {
                DJoint c = OdeHelper.createContactJoint(world, contactGroup, contacts.get(i));
                c.attach(b1, b2);
                if (c.getNumBodies() != 0 && !eventSent) {
                    eventSent = true;
                    Runnable collisionHandler1 = this.worldCollisionHandlers.get(o1);
                    Runnable collisionHandler2 = this.worldCollisionHandlers.get(o2);
                    if (collisionHandler1 != null) collisionHandler1.run();
                    if (collisionHandler2 != null) collisionHandler2.run();
                }
            }
        }
    }

    /**
     * Should be used when there is a need to make a modification on a physics body.
     * Note: If you'd try to modify the body directly, you'll get the error that the space is locked.
     *
     * @param callback Callback that will be called when space is unlocked
     */
    public void unlockSpace(Runnable callback) {
        this.spaceCallbacks.add(callback);
    }

    /**
     * Get a map of collision handlers for all geometries on the scene
     */
    public Map<DGeom, Runnable> getCollisionHandlers() {
        return this.worldCollisionHandlers;
    }

    @Override
    public void keyboardEvent(Input input, Input.State state, int key) {
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

package dev.artingl.Engine.world.scene;

import com.jme3.bullet.PhysicsSpace;
import com.jme3.math.Vector3f;
import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.input.InputListener;
import dev.artingl.Engine.input.Input;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.renderer.Renderer;
import dev.artingl.Engine.renderer.viewport.Viewport;
import dev.artingl.Engine.world.Dimension;
import dev.artingl.Engine.world.scene.components.CameraComponent;
import dev.artingl.Engine.world.scene.nodes.CameraNode;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Engine.renderer.viewport.ViewportManager;
import dev.artingl.Engine.timer.TickListener;
import dev.artingl.Engine.timer.Timer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

public class BaseScene implements TickListener, InputListener {
    private final Logger logger;
    private final Map<UUID, SceneNode> nodesList;
    private final Map<Layer, List<SceneNode>> renderNodes;

    private final Collection<Runnable> spaceCallbacks;
    private final Collection<SceneNode> lazyNodes;
    private final Dimension dimension;
    private final PhysicsSpace physicsSpace;
    private final CameraNode uiCamera;
    private CameraNode mainCamera;
    private boolean isInitialized;

    public BaseScene() {
        this.nodesList = new ConcurrentHashMap<>();
        this.lazyNodes = new ConcurrentLinkedDeque<>();
        this.spaceCallbacks = new ConcurrentLinkedDeque<>();
        this.logger = getEngine().getLogger();
        this.isInitialized = false;
        this.dimension = new Dimension();
        this.renderNodes = new ConcurrentHashMap<>();

        for (Layer layer: Layer.values())
            this.renderNodes.put(layer, new ArrayList<>());

        this.physicsSpace = new PhysicsSpace();
        this.physicsSpace.setGravity(new Vector3f(0, -32f, 0));

        this.uiCamera = new CameraNode();
        CameraComponent camera = this.uiCamera.getComponent(CameraComponent.class);
        camera.viewType = Viewport.ViewType.ORTHOGRAPHIC;
        camera.renderType = Viewport.RenderType.UI;
        camera.farPlane = 1000;
        camera.backgroundColor = Color.BLACK;
    }

    /**
     * Get scene's physics space
     * */
    public PhysicsSpace getPhysicsSpace() {
        return physicsSpace;
    }

    /**
     * Get scene's dimension.
     * */
    public Dimension getDimension() {
        return dimension;
    }

    /**
     * Set the main camera node for the scene.
     *
     * @param cam The node to be set as camera
     */
    public void setMainCamera(CameraNode cam) {
        this.mainCamera = cam;
    }

    @Nullable
    public CameraNode getMainCamera() {
        return mainCamera;
    }

    public CameraNode getUiCamera() {
        return uiCamera;
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
        this.renderNodes.get(node.getLayer()).add(node);
        this.nodesList.put(node.getUUID(), node);
        this.lazyNodes.add(node);

        // Try to set the node as main camera if we don't have any
        if (mainCamera == null && node instanceof CameraNode cam) {
            this.setMainCamera(cam);
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
            child.setLayer(node.getLayer());
            this.renderNodes.get(child.getLayer()).add(child);
            this.nodesList.put(child.getUUID(), child);
            this.lazyNodes.add(child);

            // Try to set the node as main camera if we don't have any
            if (mainCamera == null && node instanceof CameraNode cam) {
                this.setMainCamera(cam);
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
            getEngine().glContext(node::cleanup);
            node.detach();
            this.nodesList.remove(node.getUUID());

            for (List<SceneNode> nodes: this.renderNodes.values())
                nodes.remove(node);

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
     * Renders a frame of the scene
     *
     * @param renderer The renderer
     * @param layer Target layer
     * @param viewport The viewport used in the rendering
     */
    public void render(Renderer renderer, Layer layer, Viewport viewport) {
        ViewportManager viewportManager = renderer.getViewport();

        synchronized (this.renderNodes) {
            List<SceneNode> nodes = this.renderNodes.get(layer);
    //        nodes.sort(new SceneNodesSorter(mainCamera));

            viewportManager.setViewport(viewport);
            viewportManager.update();
            for (SceneNode node: nodes) {
                if (!node.getLayer().equals(layer)) {
                    // Move node to a different layer
                    nodes.remove(node);
                    if (!this.renderNodes.get(node.getLayer()).contains(node))
                        this.renderNodes.get(node.getLayer()).add(node);
                    continue;
                }

                // Make sure the child node has the same layer as the parent
                if (node.getParent() != null)
                    node.setLayer(node.getParent().getLayer());
                node.render(renderer);
            }
        }
    }

    /**
     * Called every frame to prepare the render environment.
     *
     * @param renderer The renderer
     */
    public void prepareFrame(Renderer renderer) throws EngineException {
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
        if (mainCamera == null) {
            // Print warning that we don't have any camera on the scene
            this.logger.log(LogLevel.WARNING, "No camera is set for the scene!");
        }
    }

    /**
     * Make unique nametag for the node
     *
     * @param target Target node
     */
    protected String makeNametag(SceneNode target) {
        return target.getClass().getSimpleName() + "(" + target.getUUID() + ")";
    }

    @Override
    public void tick(Timer timer) {
        this.physicsSpace.update(1 / timer.getTickPerSecond());
        this.physicsSpace.distributeEvents();

        // Call all callbacks which expect the space not to be locked
        synchronized (this.spaceCallbacks) {
            for (Runnable callback : this.spaceCallbacks) {
                callback.run();
            }
            this.spaceCallbacks.clear();
        }

        // Call tick for each node
        for (SceneNode node: nodesList.values())
            node.tick(timer);
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

    public enum Layer {
        MAIN, UI
    }
}

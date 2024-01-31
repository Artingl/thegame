package dev.artingl.Engine.scene.nodes;


import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.scene.BaseScene;
import dev.artingl.Engine.scene.components.Component;
import dev.artingl.Engine.scene.components.transform.TransformComponent;
import dev.artingl.Engine.timer.ITick;
import dev.artingl.Engine.timer.Timer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class SceneNode implements ITick {

    private final UUID uuid;
    private final List<UUID> children;
    private final List<Component> components;

    private boolean renderingAborted;
    private BaseScene scene;
    private SceneNode parent;
    private String nametag;

    public boolean isEnabled;

    public SceneNode() {
        // Generate unique id for the node
        this(UUID.randomUUID());
    }

    public SceneNode(UUID uuid) {
        this.uuid = uuid;
        this.children = new ArrayList<>();
        this.components = new ArrayList<>();
        this.isEnabled = true;

        // The node should always have transform component
        this.addComponent(new TransformComponent());
    }

    /**
     * Get collection of node's children
     * */
    public Collection<SceneNode> getChildrenNodes() {
        Collection<SceneNode> children = new ArrayList<>();

        for (UUID child: this.children) {
            children.add(getScene().getNode(child));
        }

        return children;
    }

    /**
     * Get node's transform
     * */
    public TransformComponent getTransform() {
        TransformComponent transform = this.getComponent(TransformComponent.class);

        if (transform == null) {
            // Something terrible happened... Create a new transform component and return it
            transform = new TransformComponent();
            this.addComponent(transform);
        }

        return transform;
    }

    /**
     * Change node's transform instance.
     * */
    public void changeTransformInstance(TransformComponent newTransform) {
        // Remove current transform
        for (Component component: components) {
            if (component instanceof TransformComponent)
            {
                this.components.remove(component);
                break;
            }
        }

        // Add new one
        this.addComponent(newTransform);
    }

    /**
     * Replace node's transform component with custom one
     * */
    public void replaceTransform(TransformComponent transform) {
        // Remove current transform
        components.removeIf(component -> component.getClass().isInstance(new TransformComponent()));
        components.add(0, transform);
    }

    /**
     * Attach component to a node
     * Note: Node cannot have duplicates of components.
     *
     * @param target Component to be attached
     * */
    public void addComponent(Component target) {
        // Check that we don't have the same component in the node
        for (Component component: components) {
            if (component == target)
                return;
        }

        this.components.add(target);
    }

    /**
     * Remove component from a node
     *
     * @param target Component name
     * */
    public boolean removeComponent(String target) {
        // We cannot remove transform component
        if (new TransformComponent().getName().equals(target))
            return false;

        // Check that we don't have the same component in the node
        for (Component component: components) {
            if (component.getName().equals(target))
            {
                this.components.remove(component);
                return true;
            }
        }

        return false;
    }

    /**
     * Get component instance from the node
     *
     * @param id Component class to find
     * */
    @Nullable
    public <T> T getComponent(Class<? extends Component> id) {
        for (Component component: components) {
            if (component.getClass().getName().equals(id.getName()) || id.isAssignableFrom(component.getClass())) {
                return (T) component;
            }
        }

        return null;
    }

    /**
     * Get node's all components
     * */
    public Collection<Component> getComponents() {
        return this.components;
    }

    /**
     * Gets called when node is attached to a scene.
     * Note: this must be called only once and from the scene class itself.
     *
     * @param scene The scene the entity is attached to
     * */
    public void attach(String nametag, BaseScene scene) {
        this.attach(nametag, scene, null);
    }

    /**
     * Gets called when the node is attached to a scene and this node has a parent.
     * Note: this must be called only once and from the scene class itself.
     *
     * @param scene The scene the entity is attached to
     * @param parent The node's parent (can be null)
     * */
    public void attach(String nametag, BaseScene scene, @Nullable SceneNode parent) {
        this.scene = scene;
        this.parent = parent;
        this.nametag = nametag;
    }

    /**
     * Abort next rendering of the children until next parent render is executed.
     * */
    public void abortRender() {
        this.renderingAborted = true;
    }

    /**
     * Gets called when the node is detached (removed) from the scene.
     * Note: this must be called only once and from the scene class itself.
     * */
    public void detach() {
        this.scene = null;
        this.parent = null;;
        this.nametag = null;
        this.children.clear();
    }

    /**
     * Node's unique ID
     * */
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Node's scene
     * */
    public BaseScene getScene() {
        return scene;
    }

    /**
     * Returns node's parent if any
     * */
    @Nullable
    public SceneNode getParent() {
        return parent;
    }

    /**
     * Identifies the node that it has child attached to it
     * Note: this must only be called by the scene class from {@link BaseScene#addChild(UUID, SceneNode)} method.
     *
     * @param child The child's UUID
     * */
    public void addChild(UUID child) {
        this.children.add(child);
    }

    /**
     * Get all node's children UUIDs
     * */
    public Collection<UUID> getChildren() {
        return children;
    }

    /**
     * Called every frame to render the node.
     *
     * @param context The current render context
     */
    public void render(RenderContext context) {
        this.renderingAborted = false;
        if (!this.isEnabled)
            return;
        if (parent != null && (!parent.isEnabled || parent.renderingAborted))
            return;

        for (Component component : components) {
            component.render(this, context);
        }
    }

    /**
     * Called on when node is added to the scene and should be initialized
     * */
    public void init() throws EngineException {
        for (Component component: getComponents()) {
            component.init(this);
        }
    }

    /**
     * Called when node is removed from the scene and should be cleaned up
     * */
    public void cleanup() {
        for (Component component: getComponents()) {
            component.cleanup();
        }
    }

    /**
     * Tells is the node is a child of any other node.
     * */
    public boolean isChild() {
        return parent != null;
    }

    @Override
    public void tick(Timer timer) {
        if (!this.isEnabled)
            return;
        if (parent != null && !parent.isEnabled)
            return;

        for (Component component : components) {
            // Check if node is initialized
            if (component.getNode() != null)
                component.tick(timer);
        }
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SceneNode))
            return false;
        return ((SceneNode) obj).uuid == uuid;
    }

    public Engine getEngine() {
        return Engine.getInstance();
    }

    public String getNametag() {
        return nametag;
    }

    public void setNametag(String nametag) {
        this.nametag = nametag + "(" + uuid + ")";
    }

}

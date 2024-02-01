package dev.artingl.Engine.world.scene.components;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.renderer.RenderContext;
import dev.artingl.Engine.world.scene.nodes.SceneNode;
import dev.artingl.Engine.timer.TickListener;
import dev.artingl.Engine.timer.Timer;

public class Component implements TickListener {

    private SceneNode node;

    public SceneNode getNode() {
        return node;
    }

    public void render(SceneNode node, RenderContext context) {}

    public void disable() {}

    public void enable() {}

    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Component))
            return false;
        return ((Component) obj).getName().equals(getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    public Engine getEngine() {
        return Engine.getInstance();
    }

    public void init(SceneNode node) throws EngineException {
        this.node = node;
    }

    public void cleanup() {
    }

    @Override
    public void tick(Timer timer) {
    }
}

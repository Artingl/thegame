package dev.artingl.Engine.renderer.visual.postprocessing;

import dev.artingl.Engine.renderer.shader.Shader;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public abstract class PostprocessEffect {

    private final Map<String, Object> properties;
    private boolean isEnabled = true;

    public void setEnabled(boolean enabled) {
        this.isEnabled = enabled;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public PostprocessEffect() {
        this.properties = new ConcurrentHashMap<>();
    }

    public Set<Map.Entry<String, Object>> getProperties() {
        return this.properties.entrySet();
    }

    public void setProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    /**
     * Initialize and return effects which would be computed before rendering this effect.
     * */
    @Nullable
    public PostprocessEffect[] initPreEffects() {
        return null;
    }

    /**
     * Return uniform texture names which would be allocated for the effect
     */
    public String[] getTexUniforms() {
        return null;
    }

    /**
     * Get shaders used in the effect
     * */
    public abstract Shader[] getShaders();

    public void prepareRender() {}

}

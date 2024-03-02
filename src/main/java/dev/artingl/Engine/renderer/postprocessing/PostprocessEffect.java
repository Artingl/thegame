package dev.artingl.Engine.renderer.postprocessing;

import dev.artingl.Engine.EngineException;
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
     * Get effects which would be computed before rendering this one and the result will be give in the 'ppTex' sampler uniform
     * */
    @Nullable
    public abstract PostprocessEffect[] getPreEffects();

    /**
     * Return uniform texture names which would be allocated for the effect
     */
    public abstract String[] getTexUniforms();

    /**
     * Get shaders used in the effect
     * */
    public abstract Shader[] getShaders();

}

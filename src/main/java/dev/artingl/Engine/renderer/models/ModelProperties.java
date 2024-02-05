package dev.artingl.Engine.renderer.models;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ModelProperties {

    private final IModel model;

    private final Map<String, MaterialProperty> materialProperties;

    public ModelProperties(IModel parent) {
        Engine engine = Engine.getInstance();

        this.model = parent;
        this.materialProperties = new HashMap<>();

        // Parse the json file of the model if exists
        Resource resource = this.model.getResource().relative("properties.json");
        try {
            if (resource.exists()) {
                engine.getLogger().log(LogLevel.INFO, "Parsing properties for " + parent.getResource());

                String jsonString = resource.readAsString();
                JSONObject json = new JSONObject(jsonString);

                // Parse all properties
                for (String key: json.keySet()) {
                    Object obj = json.get(key);

                    // Parsing material
                    if (key.startsWith("material.")) {
                        JSONObject materialJson = (JSONObject) obj;

                        boolean isTiled = false;
                        float opacity = 1;
                        String texture = null;

                        if (materialJson.has("texture"))
                            texture = materialJson.getString("texture");

                        if (materialJson.has("texture_tile"))
                            isTiled = materialJson.getBoolean("texture_tile");

                        if (materialJson.has("opacity"))
                            opacity = materialJson.getFloat("opacity");

                        String materialName = key.substring(9);
                        MaterialProperty prop = new MaterialProperty(materialName, texture == null ? null : new Resource(texture), opacity, isTiled);
                        this.materialProperties.put(materialName, prop);
                    }
                }
            }
        } catch (Exception e) {
            engine.getLogger().exception(e, "Unable to parse properties for " + parent.getResource());
        }
    }

    /**
     * Get property of a material in the model
     *
     * @param name Material name
     * */
    @Nullable
    public MaterialProperty getMaterialProperty(String name) {
        return this.materialProperties.get(name);
    }

    public static class MaterialProperty {

        private final String name;
        private final Resource customTexture;
        private final boolean textureTile;
        private final float opacity;

        public MaterialProperty(String name, Resource customTexture, float opacity, boolean textureTile) {
            this.name = name;
            this.opacity = opacity;
            this.textureTile = textureTile;
            this.customTexture = customTexture;
        }

        public float getOpacity() {
            return opacity;
        }

        public Resource getCustomTexture() {
            return customTexture;
        }

        public boolean isTextureTiled() {
            return textureTile;
        }

        public String getName() {
            return name;
        }
    }

}

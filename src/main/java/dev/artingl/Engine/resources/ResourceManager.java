package dev.artingl.Engine.resources;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineEventListener;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.resources.lang.LanguageManager;
import dev.artingl.Engine.resources.texture.TextureManager;

import java.io.IOException;
import java.util.Collection;

public class ResourceManager implements EngineEventListener {

    private final Engine engine;
    private final Logger logger;

    private final TextureManager textureManager;
    private final LanguageManager languageManager;

    public ResourceManager(Engine engine, Logger logger) {
        this.engine = engine;
        this.logger = logger;

        this.textureManager = new TextureManager(this.logger);
        this.languageManager = new LanguageManager(this.logger);
    }

    public void init() throws EngineException, IOException {
        this.textureManager.init();
        this.engine.subscribeEngineEvents(this);

        // Load textures and languages from all namespaces
        Collection<String> namespaces = engine.getNamespaces();
        for (String namespace: namespaces) {
            this.textureManager.load(new Resource(namespace, "textures"));
            this.languageManager.load(new Resource(namespace, "lang"));
        }
    }

    public void cleanup() throws EngineException {
        this.textureManager.cleanup();
        this.engine.unsubscribeEngineEvents(this);
    }

    public TextureManager getTextureManager() {
        return textureManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }

    @Override
    public void onReload() throws Exception {
        Engine.getInstance().getLogger().log(LogLevel.INFO, "Reloading all resources");
        this.textureManager.reload();
        this.languageManager.reload();
    }
}

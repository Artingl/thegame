package dev.artingl.Engine.resources.lang;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.EngineException;
import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;
import dev.artingl.Engine.resources.Resource;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class LanguageManager {

    public static final String[] LANGUAGES = { "en", "ru" };

    // languagesList[namespace:lang/en][item.laptop_title]
    private final ConcurrentHashMap<Resource, ConcurrentHashMap<String, String>> languagesList;
    private final Logger logger;

    public LanguageManager(Logger logger) {
        this.languagesList = new ConcurrentHashMap<>();
        this.logger = logger;
    }

    /**
     * Load all languages from a resource
     *
     * @param resource Target resource
     */
    public void load(Resource resource) throws IOException, EngineException {
        // Load all languages within the resource
        for (String lang: LANGUAGES) {
            Resource jsonPath = resource.relative(lang + ".json");
            if (jsonPath.exists()) {
                logger.log(LogLevel.INFO, "Loading language '" + lang + "' at '" + resource.getNamespace() + "'");
                parseLanguage(lang, jsonPath);
            }
        }
    }

    /**
     * Get a string from language file. If the language and/or location are invalid, null is returned.
     *
     * @param textLocation The location of the text inside languages in form namespace:en/item.laptop_title
     * */
    @Nullable
    public String getLanguageString(Resource textLocation) {
        String[] delims = textLocation.getPath().split("/");
        if (delims.length != 2)
            // Malformed location
            return null;

        Resource language = new Resource(textLocation.getNamespace(), "lang/" + delims[0]);
        ConcurrentHashMap<String, String> languageMap = languagesList.get(language);
        if (languageMap != null)
            return languageMap.get(delims[1]);
        return null;
    }

    private void parseLanguage(String language, Resource jsonPath) throws IOException {
        String jsonString = jsonPath.readAsString();
        JSONObject json = new JSONObject(jsonString);

        // Parse all properties
        for (String key: json.keySet()) {
            String value = json.getString(key);
            addLanguageString(new Resource(jsonPath.getNamespace(), "lang/" + language), key, value);
        }
    }

    private void addLanguageString(Resource language, String location, String value) {
        if (this.languagesList.containsKey(language))
            this.languagesList.get(language).put(location, value);
        else {
            ConcurrentHashMap<String, String> languageMap = new ConcurrentHashMap<>();
            languageMap.put(location, value);
            this.languagesList.put(language, languageMap);
        }
    }

    public void cleanup() {
        this.languagesList.clear();
    }

    public void reload() throws EngineException, IOException {
        this.cleanup();

        for (String namespace: Engine.getInstance().getNamespaces())
            this.load(new Resource(namespace, "lang"));
    }
}

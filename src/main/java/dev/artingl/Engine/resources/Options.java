package dev.artingl.Engine.resources;

import dev.artingl.Engine.debug.Logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Options {

    private final Logger logger;
    private final Map<Values, Object> options;

    public Options(Logger logger) {
        this.logger = logger;
        this.options = new ConcurrentHashMap<>();

        this.load();
    }

    /**
     * Load options from the save file (if exists)
     * */
    public void load() {
        this.options.put(Values.DEBUG, false);
        this.options.put(Values.RENDER_DISTANCE, 0.5f);
    }

    /**
     * Save options to the save file (or create a new one)
     * */
    public void save() {}

    public void set(Values option, Object value) {
        this.options.put(option, value);
    }

    public void set(Values option, String value) {
        this.set(option, (Object) value);
    }

    public void set(Values option, int value) {
        this.set(option, (Object) value);
    }

    public void set(Values option, boolean value) {
        this.set(option, (Object) value);
    }

    public void set(Values option, float value) {
        this.set(option, (Object) value);
    }

    public Object get(Values option) {
        return this.options.get(option);
    }

    public String getString(Values option) {
        return (String) this.get(option);
    }

    public int getInt(Values option) {
        return (int) this.get(option);
    }

    public float getFloat(Values option) {
        return (Float) this.get(option);
    }

    public boolean getBoolean(Values option) {
        return (boolean) this.get(option);
    }

    public enum Values {
        DEBUG, RENDER_DISTANCE
    }

}

package dev.artingl.Engine.resources;

import dev.artingl.Engine.Engine;

public class Text {

    public static final Text EMPTY = new Text("");
    private final String text;


    public Text(Resource textLocation) {
        this(Engine.getInstance().getResourceManager().getLanguageManager().getLanguageString(textLocation));
    }

    public Text(String text) {
        this.text = text;
    }

    /**
     * Get string of the text
     * */
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Text{" + text + "}";
    }

    @Override
    public int hashCode() {
        return text.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Text))
            return false;
        return text.equals(((Text) obj).text);
    }

    /**
     * Concatenate two texts into one single string
     *
     * @param text The text to concatenate with
     * */
    public Text concatenate(Text text) {
        if (text == null)
            return this;
        return new Text(this.text.trim() + " " + text.text.trim());
    }

    /**
     * Concatenate two texts into one single string
     *
     * @param text The text to concatenate with
     * */
    public Text concatenate(String text) {
        return concatenate(new Text(text));
    }
}

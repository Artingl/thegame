package dev.artingl.Engine.renderer;

public enum Quality {
    HIGH, MEDIUM, LOW, POTATO, NOT_RENDERED

    ;

    /**
     * Clamps the target quality value, so it would not be higher than the possible highest value.
     *
     * @param highest The possible highest quality
     * @param target Target quality
     * */
    public static Quality clamp(Quality highest, Quality target) {
        if (target.ordinal() < highest.ordinal()) {
            return highest;
        }
        return target;
    }
}

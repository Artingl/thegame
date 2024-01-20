package dev.artingl.Game.level.ambient;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Game.level.Level;

public class Sky {

    private final SkyMesh mesh;
    private final Level level;

    public Sky(Level level) {
        this.level = level;
        this.mesh = new SkyMesh(this, level);
    }

    /**
     * Change sky's color
     * */
    public void setColor(Color color) {
        this.mesh.setColor(color);
    }

    /**
     * Get current sky's color
     * */
    public Color getColor() {
        return mesh.getColor();
    }

    /**
     * Get sky's mesh instance
     * */
    public SkyMesh getMesh() {
        return mesh;
    }

    /**
     * Get sun's position on the sky (in degrees)
     * */
    public float getSunRotation() {
        float time = this.level.getLevelTime();

        if (time < Level.SUN_CYCLE_TICKS) {
            return 180 * time / Level.SUN_CYCLE_TICKS;
        }

        return 180 + 180 * ((time - Level.SUN_CYCLE_TICKS) / Level.MOON_CYCLE_TICKS);
    }

}

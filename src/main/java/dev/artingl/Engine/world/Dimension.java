package dev.artingl.Engine.world;

import java.util.UUID;

public class Dimension {

    private final UUID uuid;

    public Dimension() {
        this.uuid = UUID.randomUUID();
    }

    /**
     * Get UUID of this dimension.
     * */
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Dimension))
            return false;
        return ((Dimension) obj).uuid == uuid;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}

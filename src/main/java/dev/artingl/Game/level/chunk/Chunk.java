package dev.artingl.Game.level.chunk;

import dev.artingl.Game.level.Level;
import dev.artingl.Game.level.chunk.environment.EnvironmentObjects;
import org.joml.Vector2i;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Chunk {

    public static final int CHUNK_SIZE = 64;
    public static final int VOLUME = CHUNK_SIZE * CHUNK_SIZE;

    private final Vector2i position;
    private final Level level;
    private final ChunkMesh mesh;
    private final ConcurrentLinkedDeque<Pair<EnvironmentObjects, Vector3f>> envObjectsList;

    public Chunk(Level level, Vector2i chunkPosition) {
        this.position = chunkPosition;
        this.level = level;
        this.mesh = new ChunkMesh(this, level);
        this.envObjectsList = new ConcurrentLinkedDeque<>();
        this.level.getGenerator().generateChunk(this);
    }

    /**
     * Get chunk's level
     * */
    public Level getLevel() {
        return level;
    }

    /**
     * Get list of all environment objects in the chunk.
     *
     * @return Collection of pairs, which represents each environment object and it's co-ordinates
     * */
    public Collection<Pair<EnvironmentObjects, Vector3f>> getEnvObjectsList() {
        return envObjectsList;
    }

    /**
     * Get chunk's position
     * */
    public Vector2i getPosition() {
        return new Vector2i(position);
    }

    /**
     * Get chunk's position in the level
     * */
    public Vector2i getPositionLevel() {
        return new Vector2i(position).mul(CHUNK_SIZE);
    }

    /**
     * Get chunk's mesh
     * */
    public ChunkMesh getMesh() {
        return mesh;
    }
}

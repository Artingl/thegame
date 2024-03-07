package dev.artingl.Game.level;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.misc.noise.PerlinNoise;
import dev.artingl.Engine.renderer.Quality;
import dev.artingl.Engine.renderer.mesh.VerticesBuffer;
import dev.artingl.Game.level.chunk.Chunk;
import dev.artingl.Game.level.chunk.environment.EnvironmentObjects;
import dev.artingl.Game.level.chunk.terrain.Terrain;
import dev.artingl.Game.level.chunk.terrain.TerrainType;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.joml.Vector3f;
import oshi.util.tuples.Pair;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class LevelTerrainGenerator {

    public static final float SPAWN_RADIUS = 100;
    public static final float RIVER_SIZE = 2;

    private final Map<Vector2i, Terrain.TerrainMeta> cachedTerrain;
    private final PerlinNoise noise;
    private final PerlinNoise terrainNoise;
    private final Random random;
    private final int seed;
    private final Level level;

    public LevelTerrainGenerator(Level level, int seed) {
        this.seed = seed;
        this.level = level;
        this.cachedTerrain = new ConcurrentHashMap<>();
        this.random = new Random(seed);
        this.noise = new PerlinNoise(1, seed);
        this.terrainNoise = new PerlinNoise(3, seed*seed);
    }

    /**
     * Generate the terrain for a chunk.
     *
     * @param chunk Chunk which is needed to be generated
     */
    public void generateChunk(Chunk chunk) {
        // Generate the highest quality first, and all others using other threads to speed up the loading
        Engine engine = Engine.getInstance();
        chunk.getMesh().setVerticesQuality(
                Quality.HIGH,
                generateQualityBuffer(Quality.HIGH, chunk));

        for (Quality quality: Quality.values()) {
            if (quality == Quality.NOT_RENDERED || quality == Quality.HIGH)
                continue;
            engine.getThreadsManager().execute(() -> {
                chunk.getMesh().setVerticesQuality(
                        quality,
                        generateQualityBuffer(quality, chunk));
            });
        }
    }

    private VerticesBuffer generateQualityBuffer(Quality quality, Chunk chunk) {
        Vector2i chunkPosition = chunk.getPositionLevel();
        int idx = (quality == Quality.POTATO ? Quality.LOW : quality).ordinal();
        float step = 3;

        VerticesBuffer buffer = new VerticesBuffer(
                // Position
                VerticesBuffer.Attribute.VEC3F,

                // Normal
                VerticesBuffer.Attribute.VEC3F,

                // UV
                VerticesBuffer.Attribute.VEC2F,

                // Color
                VerticesBuffer.Attribute.VEC3F);

        for (float x = 0; x < Chunk.CHUNK_SIZE; x += step)
            for (float z = 0; z < Chunk.CHUNK_SIZE; z += step) {
                Terrain.TerrainMeta[] corners = calculateCorners(chunk, chunkPosition.x + x, chunkPosition.y + z, step);

                // First triangle vectors and normal
                Vector3f fv0 = new Vector3f(0.0f, 0.0f, 0.0f).add(x, corners[0].getHeight(), z);
                Vector3f fv1 = new Vector3f(step, 0.0f, 0.0f).add(x, corners[1].getHeight(), z);
                Vector3f fv2 = new Vector3f(0.0f, 0.0f, step).add(x, corners[2].getHeight(), z);

                // Second triangle vectors and normal
                Vector3f sv0 = new Vector3f(step, 0.0f, step).add(x, corners[3].getHeight(), z);
                Vector3f sv1 = new Vector3f(0.0f, 0.0f, step).add(x, corners[5].getHeight(), z);
                Vector3f sv2 = new Vector3f(step, 0.0f, 0.0f).add(x, corners[4].getHeight(), z);

                Vector3f normal0 = new Vector3f(fv1).sub(fv0).cross(new Vector3f(fv2).sub(fv0)).normalize();
                Vector3f normal1 = new Vector3f(sv1).sub(sv0).cross(new Vector3f(sv2).sub(sv0)).normalize();

                float u0 = x / Chunk.CHUNK_SIZE;
                float v0 = z / Chunk.CHUNK_SIZE;
                float u1 = (x + step) / Chunk.CHUNK_SIZE;
                float v1 = (z + step) / Chunk.CHUNK_SIZE;

                buffer
                        .addAttribute(fv0).addAttribute(normal0).addAttribute(new Vector2f(u0, v0)).addColor3f(corners[0].getColor())
                        .addAttribute(fv1).addAttribute(normal0).addAttribute(new Vector2f(u1, v0)).addColor3f(corners[1].getColor())
                        .addAttribute(fv2).addAttribute(normal0).addAttribute(new Vector2f(u0, v1)).addColor3f(corners[2].getColor());

                buffer
                        .addAttribute(sv0).addAttribute(normal1).addAttribute(new Vector2f(u1, v1)).addColor3f(corners[3].getColor())
                        .addAttribute(sv1).addAttribute(normal1).addAttribute(new Vector2f(u0, v1)).addColor3f(corners[4].getColor())
                        .addAttribute(sv2).addAttribute(normal1).addAttribute(new Vector2f(u1, v0)).addColor3f(corners[5].getColor());
            }

        return buffer;
    }

    private Terrain.TerrainMeta[] calculateCorners(Chunk chunk, float x, float z, float step) {
        Terrain.TerrainMeta m0 = this.generateTerrain(chunk, x + 0, z + 0);
        Terrain.TerrainMeta m1 = this.generateTerrain(chunk, x + step, z + 0);
        Terrain.TerrainMeta m2 = this.generateTerrain(chunk, x + 0, z + step);
        Terrain.TerrainMeta m3 = this.generateTerrain(chunk, x + step, z + step);

        return new Terrain.TerrainMeta[]{m0, m1, m2, m3, m1, m2};
    }

    /**
     * Get perlin noise generator of the chunk.
     */
    public PerlinNoise getNoise() {
        return noise;
    }

    /**
     * Get seed of the chunk.
     */
    public int getSeed() {
        return seed;
    }

    /**
     * Get a terrain info that was cached at X and Z.
     *
     * @param x Target X co-ordinate.
     * @param z Target Z co-ordinate.
     * @return null if there is no cached value at X and Z, or the height value
     */
    public Terrain.TerrainMeta getCachedTerrain(int x, int z) {
        return this.cachedTerrain.get(new Vector2i(x, z));
    }

    /**
     * Get amount of cached values.
     */
    public int getCacheSize() {
        return this.cachedTerrain.size();
    }

    /**
     * Get normalized normal at point position
     * */
    public Vector3f getNormal(Vector3f position) {
        Chunk chunk = level.getChunkWorld(new Vector2i((int) position.x, (int) position.z));
        if (chunk == null)
            return new Vector3f();

        Terrain.TerrainMeta m0 = this.generateTerrain(chunk, position.x + 0, position.z + 0);
        Terrain.TerrainMeta m1 = this.generateTerrain(chunk, position.x + 2, position.z + 0);
        Terrain.TerrainMeta m2 = this.generateTerrain(chunk, position.x + 0, position.z + 2);

        Vector3f fv0 = new Vector3f(position.x, m0.getHeight(), position.z);
        Vector3f fv1 = new Vector3f(position.x + 2, m2.getHeight(), position.z);
        Vector3f fv2 = new Vector3f(position.x, m1.getHeight(), position.z + 2);

        return new Vector3f(fv1).sub(fv0).cross(new Vector3f(fv2).sub(fv0)).normalize();
    }

    public Terrain.TerrainMeta generateTerrain(Chunk chunk, float x, float z) {
        // Check if we already calculated terrain for this X and Z
        Terrain.TerrainMeta cached = getCachedTerrain((int) x, (int) z);
        if (cached != null) {
            return cached;
        }

        // Calculate terrain for provided co-ordinates
        Color color = Color.from(0, 150, 0);
        EnvironmentObjects envObj = null;
        int objsCounter = 0;
        float height = 0;
        int divider = 0;
        int radius = 6, step = 1;

//        for (int xOffset = -radius; xOffset <= radius; xOffset += step)
//            for (int zOffset = -radius; zOffset <= radius; zOffset += step) {
//                float newX = x + xOffset, newZ = z + zOffset;
//                Terrain terrain = Terrain.as(getTerrainTypeAt(newX, newZ));
//                Terrain.TerrainMeta meta = terrain.generateTerrain(this, newX, newZ);
//                height += meta.getHeight();
//                color.add(meta.getColor());
//                divider++;
//                if (!terrain.environmentOverlap())
//                    objsCounter = -1;
//                if (meta.getEnvObject() != null && objsCounter != -1) {
//                    envObj = meta.getEnvObject();
//                    objsCounter++;
//                }
//            }
//        color.div3(divider);
//        height /= divider;
//        Terrain.TerrainMeta result = new Terrain.TerrainMeta(color, envObj, height);
//
//        if (objsCounter > 0)//> radius*2)
//            chunk.getEnvObjectsList().add(new Pair<>(envObj, new Vector3f(x, height - 0.1f, z)));

        Terrain.TerrainMeta result = new Terrain.TerrainMeta(Color.from(125, 199, 79), null, -2);

        // Cache value to be used later
        this.cachedTerrain.put(new Vector2i((int) x, (int) z), result);

        return result;
    }

    public TerrainType getTerrainTypeAt(float x, float z) {
        // Under the shelter on the spawn, we have basement under the ground.
        // We must generate hole at coordinates of this basement, so it'd be possible to get underground
        // -27 -34 -18 -26
        if (x > -23 && x < 10 && z > -30 && z < 10)
            return TerrainType.HOLE;

        PerlinNoise.Settings spawnSettings = new PerlinNoise.Settings(1, 0.5f, 0.035f, -5, 5);
        float spawnRoughness = terrainNoise.getValue(spawnSettings, x, z);

        // Check if we're inside the spawn radius
        if (new Vector2f(0, 0).distance(x, z) + spawnRoughness <= SPAWN_RADIUS) {
            return TerrainType.SPAWN;
        }

        PerlinNoise.Settings settings = new PerlinNoise.Settings(3, 1f, 0.035f, 0, 32 + RIVER_SIZE * 2);
        float value = terrainNoise.getValue(settings, x, z);

        // We outside the spawn radius, generate the world normally
        if (value < 2) {
            return TerrainType.FLAT;
//        } else if (value < 2 + RIVER_SIZE) {
//            return TerrainType.WATER;
        } else if (value < 7 + RIVER_SIZE) {
            return TerrainType.FOREST;
//        } else if (value < 25 + RIVER_SIZE) {
//            return TerrainType.WATER;
        }

        return TerrainType.MOUNTAINS;
    }

    public int getRandomInt(int min, int max) {
        return Utils.randInt(random, min, max);
    }

}

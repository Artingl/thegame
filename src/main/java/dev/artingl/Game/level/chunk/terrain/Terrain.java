package dev.artingl.Game.level.chunk.terrain;

import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.misc.noise.PerlinNoise;
import dev.artingl.Game.GameDirector;
import dev.artingl.Game.level.LevelTerrainGenerator;
import dev.artingl.Game.level.chunk.environment.EnvironmentObjects;

import java.util.Random;

public interface Terrain {

    /**
     * Get terrain class based on its type
     */
    static Terrain as(TerrainType type) {
        switch (type) {
            case FLAT -> {
                return new FlatTerrain();
            }

            case MOUNTAINS -> {
                return new MountainsTerrain();
            }

            case FOREST -> {
                return new ForestTerrain();
            }

            case WATER -> {
                return new WaterTerrain();
            }

            case SPAWN -> {
                return new SpawnTerrain();
            }

            case HOLE -> {
                return new HoleTerrain();
            }
        }

        GameDirector.getInstance().getLogger().log(LogLevel.ERROR, "Invalid terrain type " + type);
        return null;
    }

    /**
     * Generates environment object at co-ordinates.
     * */
    default EnvironmentObjects calculateEnvObject(LevelTerrainGenerator generator, float x, float height, float z) {
        int per = Utils.randInt(new Random(generator.getSeed() + ((int)x) * 31L + ((int)z)), -4096, 4096);
        if (getType() == TerrainType.MOUNTAINS)
            return per >= 0 && per < 4 && height > 20 ? EnvironmentObjects.TREE :
                    per > 0 && per < 8 ? EnvironmentObjects.ROCK :
                    per > 40 && per < 70 && height > 20 ? EnvironmentObjects.GRASS : null;
        return per >= 0 && per < 4 ? EnvironmentObjects.TREE :
                per > 10 && per < 18 ? EnvironmentObjects.ROCK :
                per > 40 && per < 70 ? EnvironmentObjects.GRASS : null;
    }

    /**
     * Generate terrain at given co-ordinates.
     *
     * @param generator Chunk generator for which we're making terrain.
     * @param x         X co-ordinate of the terrain
     * @param z         Z co-ordinate of the terrain
     * @return Info about the terrain at given co-ordinates, such as the height, plants, etc.
     */
    TerrainMeta generateTerrain(LevelTerrainGenerator generator, float x, float z);

    /**
     * Returns general noise settings for the terrain.
     * */
    PerlinNoise.Settings getSettings();

    /**
     * Get terrain type.
     */
    TerrainType getType();

    /**
     * Can environment objects for other terrains overlap this terrain
     * */
    boolean environmentOverlap();

    class TerrainMeta {

        private final float height;
        private final Color color;
        private EnvironmentObjects envObject;

        public TerrainMeta(Color color, EnvironmentObjects envObject, float height) {
            this.height = height;
            this.envObject = envObject;
            this.color = color;
        }

        public EnvironmentObjects getEnvObject() {
            return envObject;
        }

        public Color getColor() {
            return color;
        }

        public float getHeight() {
            return height;
        }
    }

}

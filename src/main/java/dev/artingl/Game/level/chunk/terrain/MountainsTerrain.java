package dev.artingl.Game.level.chunk.terrain;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.misc.noise.PerlinNoise;
import dev.artingl.Game.level.LevelTerrainGenerator;
import dev.artingl.Game.level.chunk.environment.EnvironmentObjects;

import java.util.Random;

public class MountainsTerrain implements Terrain {

    @Override
    public TerrainMeta generateTerrain(LevelTerrainGenerator generator, float x, float z) {
        float height = generator.getNoise().getValue(getSettings(), x, z);
        EnvironmentObjects envObj = calculateEnvObject(generator, x, height, z);
        return new TerrainMeta(Color.from(125, 199, 79), envObj, height);
    }

    @Override
    public PerlinNoise.Settings getSettings() {
        return new PerlinNoise.Settings(3, 0.2f, 0.025f, 4, 32);
    }

    @Override
    public TerrainType getType() {
        return TerrainType.MOUNTAINS;
    }

    @Override
    public boolean environmentOverlap() {
        return true;
    }
}

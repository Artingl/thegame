package dev.artingl.Game.level.chunk.terrain;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.noise.PerlinNoise;
import dev.artingl.Game.level.LevelTerrainGenerator;
import dev.artingl.Game.level.chunk.environment.EnvironmentObjects;

public class FlatTerrain implements Terrain {

    @Override
    public TerrainMeta generateTerrain(LevelTerrainGenerator generator, float x, float z) {
        float height = generator.getNoise().getValue(getSettings(), x, z);
        EnvironmentObjects envObj = calculateEnvObject(generator, x, height, z);
        return new TerrainMeta(Color.from(227, 196, 155), envObj, height);
    }

    @Override
    public PerlinNoise.Settings getSettings() {
        return new PerlinNoise.Settings(3, 0.5f, 0.035f, 0, 6);
    }

    @Override
    public TerrainType getType() {
        return TerrainType.FLAT;
    }

    @Override
    public boolean environmentOverlap() {
        return true;
    }

}

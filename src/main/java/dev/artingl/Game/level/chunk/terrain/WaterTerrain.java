package dev.artingl.Game.level.chunk.terrain;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.noise.PerlinNoise;
import dev.artingl.Game.level.LevelTerrainGenerator;

public class WaterTerrain implements Terrain {

    @Override
    public TerrainMeta generateTerrain(LevelTerrainGenerator generator, float x, float z) {
        return new TerrainMeta(Color.from(109, 110, 104), null, generator.getNoise().getValue(getSettings(), x, z));
    }

    @Override
    public PerlinNoise.Settings getSettings() {
        return new PerlinNoise.Settings(1, 0.2f, 0.025f, -15, -10);
    }

    @Override
    public TerrainType getType() {
        return TerrainType.MOUNTAINS;
    }

    @Override
    public boolean environmentOverlap() {
        return false;
    }
}

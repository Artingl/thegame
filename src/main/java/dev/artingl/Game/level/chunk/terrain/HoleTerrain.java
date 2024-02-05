package dev.artingl.Game.level.chunk.terrain;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.noise.PerlinNoise;
import dev.artingl.Game.level.LevelTerrainGenerator;

public class HoleTerrain implements Terrain {
    @Override
    public TerrainMeta generateTerrain(LevelTerrainGenerator generator, float x, float z) {
        return new TerrainMeta(Color.TRANSPARENT, null, -10000);
    }

    @Override
    public PerlinNoise.Settings getSettings() {
        return null;
    }

    @Override
    public TerrainType getType() {
        return TerrainType.HOLE;
    }

    @Override
    public boolean environmentOverlap() {
        return true;
    }

}

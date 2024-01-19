package dev.artingl.Game.level.chunk.terrain;

import dev.artingl.Engine.misc.Color;
import dev.artingl.Engine.misc.noise.PerlinNoise;
import dev.artingl.Game.level.LevelTerrainGenerator;

public class SpawnTerrain implements Terrain {

    @Override
    public TerrainMeta generateTerrain(LevelTerrainGenerator generator, float x, float z) {
        return new TerrainMeta(Color.from(227, 196, 155), null, generator.getNoise().getValue(getSettings(), x, z));
    }

    @Override
    public PerlinNoise.Settings getSettings() {
        return new PerlinNoise.Settings(1, 0.5f, 0.035f, -2, 2);
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

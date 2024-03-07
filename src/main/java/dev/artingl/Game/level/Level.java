package dev.artingl.Game.level;

import dev.artingl.Engine.Engine;
import dev.artingl.Engine.misc.Utils;
import dev.artingl.Engine.world.scene.BaseScene;
import dev.artingl.Engine.timer.TickListener;
import dev.artingl.Engine.timer.Timer;
import dev.artingl.Game.level.ambient.Sky;
import dev.artingl.Game.level.chunk.Chunk;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2i;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Level implements TickListener {

    public static final int SUN_CYCLE_TICKS = 128 * 12 * 60;
    public static final int MOON_CYCLE_TICKS = 128 * 25 * 60;
    public static final int DAY_CYCLE_TICKS = MOON_CYCLE_TICKS + SUN_CYCLE_TICKS;

    // Chunks each direction
    public static final int LEVEL_SIZE = 8;//16;

    private final Map<Vector2i, Chunk> chunks;
    private final LevelTerrainGenerator generator;
    private float levelTime;
    private float timeSpeed = 0;
    private float levelLight;

    // Level's ambient
    private final Sky sky;

    public Level() {
        this.sky = new Sky(this);
        this.chunks = new ConcurrentHashMap<>();
        this.generator = new LevelTerrainGenerator(this, Utils.randInt(-0xfffff, 0xfffff));

        this.levelTime = SUN_CYCLE_TICKS;
        this.levelLight = 1;

        // Fill the level with chunks
        for (int x = -LEVEL_SIZE; x <= LEVEL_SIZE; x++)
            for (int z = -LEVEL_SIZE; z <= LEVEL_SIZE; z++)
            {
                this.chunks.put(new Vector2i(x, z), new Chunk(this, new Vector2i(x, z)));
            }
//        int x = 0, z = 0;
//        this.chunks.put(new Vector2i(x, z), new Chunk(this, new Vector2i(x, z)));
    }

    /**
     * Get the chunk in the level
     *
     * @param position Chunk's position
     * */
    @Nullable
    public Chunk getChunk(Vector2i position) {
        return this.chunks.get(position);
    }

    /**
     * Get the chunk in the level by world position
     *
     * @param position Chunk's position in the world
     * */
    @Nullable
    public Chunk getChunkWorld(Vector2i position) {
        return this.chunks.get(new Vector2i(position).div(Chunk.CHUNK_SIZE));
    }

    @Override
    public void tick(Timer timer) {
        levelTime += timeSpeed;

        if (levelTime > DAY_CYCLE_TICKS) {
            this.levelTime = 0;
            this.levelLight = 1;
        }

        // Calculate light level for current time
        float daySwitchSpeed = 128 * 100;
        float sunStartTicks = SUN_CYCLE_TICKS - daySwitchSpeed + 512;
        float moonStartTicks = DAY_CYCLE_TICKS - daySwitchSpeed;
        if (levelTime > moonStartTicks) {
            this.levelLight = Math.min(1, (levelTime - moonStartTicks) / daySwitchSpeed);
        }
        else if (levelTime > sunStartTicks) {
            this.levelLight = Math.max(0, 1 - ((levelTime - sunStartTicks) / daySwitchSpeed));
        }
    }

    /**
     * Get collection of all chunks in the level
     * */
    public Collection<Chunk> getChunks() {
        return this.chunks.values();
    }

    /**
     * Get level's local time in ticks (one day is 6 minutes => 46080 ticks)
     * */
    public float getLevelTime() {
        return levelTime;
    }

    /**
     * Get current light level based on the time, where 1 is the noon, and 0 is the night
     * */
    public float getLightLevel() {
        return levelLight;
    }

    /**
     * Get level's sky
     * */
    public Sky getSky() {
        return sky;
    }

    /**
     * TODO: make player class so the level wont need to serve this kind of information
     * */
    public Vector3f getPlayerPosition() {
        BaseScene scene = Engine.getInstance().getSceneManager().getCurrentScene();
        if (scene == null)
            return new Vector3f();

        return scene.getMainCamera().getPosition();
    }

    /**
     * Sets value that will be added to the level time very tick
     *
     * @param speed The level time speed
     * */
    public void setTimeSpeed(int speed) {
        this.timeSpeed = speed;
    }

    /**
     * Get chunk terrain generator.
     * */
    public LevelTerrainGenerator getGenerator() {
        return generator;
    }

}

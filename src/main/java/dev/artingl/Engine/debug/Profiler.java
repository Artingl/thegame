package dev.artingl.Engine.debug;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Profiler {

    private final Map<Task, Integer> counter;
    private long lastTime;
    private long newTime;

    private float fps = -1;
    private float frameTime = -1;
    private float calculatedGpuTime = -1;
    private float gpuTime = 0;
    private int gpuTimeDiv = 0;
    private long timeSinceUpdate = 0;


    public Profiler() {
        this.counter = new ConcurrentHashMap<>();
        this.counter.put(Task.DRAW_CALLS, 0);
        this.counter.put(Task.VERTICES_DRAWN, 0);
        this.counter.put(Task.FRAMEBUFFER_BINDS, 0);
    }

    /**
     * Gets called every frame so the profiler can make its calculations
     * */
    public void frame() {
        gpuTimeDiv++;
        lastTime = newTime;
        newTime = System.nanoTime();

        counter.replaceAll((t, v) -> 0);

        if (fps == -1 || timeSinceUpdate + 50 < System.currentTimeMillis()) {
            this.timeSinceUpdate = System.currentTimeMillis();
            this.fps = 1000 / getFrameTime();
            this.frameTime = (newTime - lastTime) / 1000000f;

            this.calculatedGpuTime = this.gpuTime / gpuTimeDiv;
            this.gpuTime = 0;
            this.gpuTimeDiv = 0;
        }
    }

    public float getFPS() {
        return this.fps;
    }

    public float getFrameTime() {
        return this.frameTime;
    }

    public float getGpuTime() {
        return calculatedGpuTime;
    }

    /**
     * Increment counter value for task
     *
     * @param task Target task
     * */
    public void incCounter(Task task) {
        addCounter(task, 1);
    }

    /**
     * Add value to the task counter
     *
     * @param task Target task
     * @param value Value to be added
     * */
    public void addCounter(Task task, int value) {
        this.counter.put(task, this.counter.get(task) + value);
    }

    /**
     * Get task counter value
     *
     * @param task Target task
     * */
    public int getCounter(Task task) {
        return this.counter.get(task);
    }

    public void addGpuTime(float value) {
        this.gpuTime += value;
    }

    public enum Task {
        DRAW_CALLS,
        VERTICES_DRAWN, FRAMEBUFFER_BINDS,

    }
}

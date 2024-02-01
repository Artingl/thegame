package dev.artingl.Engine.timer;

public interface TickListener {

    /**
     * Called every tick (set by the timer)
     * */
    void tick(Timer timer);

}

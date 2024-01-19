package dev.artingl.Engine.timer;

public interface ITick {

    /**
     * Called every tick (set by the timer)
     * */
    void tick(Timer timer);

}

package dev.artingl.Engine.misc;

import java.util.concurrent.atomic.AtomicBoolean;

public class Spinlock {

    private final AtomicBoolean locked = new AtomicBoolean(false);

    public void lock() {
        while (!locked.compareAndSet(false, true));
    }

    public void unlock() {
        locked.set(false);
    }
}

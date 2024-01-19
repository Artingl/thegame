package dev.artingl.Engine.threading;

import dev.artingl.Engine.debug.LogLevel;
import dev.artingl.Engine.debug.Logger;

import java.util.concurrent.*;

public class ThreadsManager {

    private final Logger logger;

    private final ThreadPoolExecutor executor;
    private final int logicalCores;

    public ThreadsManager() {
        this.logicalCores = (int) (Runtime.getRuntime().availableProcessors() * 0.8f);
        this.executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(this.logicalCores);

        this.logger = Logger.create("ThreadsManager");
        this.logger.log(LogLevel.INFO, "Using ThreadPoolExecutor with %d threads.", this.getAvailableCores());
    }

    public int getAvailableCores() {
        return this.logicalCores;
    }

    public Future<?> submit(Runnable handler) {
        return this.executor.submit(handler);
    }

    public <T> Future<T> submit(Callable<T> handler) {
        return this.executor.submit(handler);
    }

    public void execute(Runnable handler) {
        this.executor.execute(handler);
    }

    public <T> Future<T> submit(Callable<T> handler, int timeout) {
        this.logger.log(LogLevel.UNIMPLEMENTED, "Threading: submit(Callable<T> handler, int timeout)");
        return null;
    }
}

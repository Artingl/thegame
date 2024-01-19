/*
 * The MIT License (MIT)
 *
 * Copyright © 2014-2015, Heiko Brumme
 * Copyright © 2023, Artyom Troiazykov
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.artingl.Engine.timer;

import dev.artingl.Engine.Engine;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Timer {

    private final float tickPerSecond;
    private long lastTime;
    private int ticks;
    private float timeScale = 1.0F;
    private float passedTime = 0.0F;
    private boolean isRunning;
    private boolean isTimerStopped = true;
    private final Collection<ITick> subscribers;

    public Timer(float tickPerSecond) {
        this.tickPerSecond = tickPerSecond;
        this.subscribers = new ConcurrentLinkedDeque<>();
    }

    public float getTickPerSecond() {
        return tickPerSecond;
    }

    public void subscribe(ITick tick) {
        this.subscribers.add(tick);
    }

    public void unsubscribe(ITick tick) {
        this.subscribers.remove(tick);
    }

    /**
     * Stops the main timer loop running in different thread
     * */
    public void terminate() {
        this.isRunning = false;

        // Wait until the timer is stopped
        while (!this.isTimerStopped) {}
    }

    /**
     * Enter the main timer loop in different thread, that will call tick subscribers
     * */
    public void enterLoop() {
        this.isRunning = true;

        Engine engine = Engine.getInstance();
        engine.getThreadsManager().execute(() -> {
            while (this.isRunning) {
                this.isTimerStopped = false;
                long now = System.nanoTime();
                long passedNs = now - this.lastTime;
                this.lastTime = now;
                if (passedNs < 0L) {
                    passedNs = 0L;
                }

                if (passedNs > 1000000000L) {
                    passedNs = 1000000000L;
                }

                this.passedTime += (float)passedNs * this.timeScale * this.tickPerSecond / 1.0E9F;
                this.ticks = (int)this.passedTime;
                if (this.ticks > 100) {
                    this.ticks = 100;
                }

                this.passedTime -= (float)this.ticks;
                this.callSubscribers();
            }
            this.isTimerStopped = true;
        });
    }

    public int getTicks() {
        return ticks;
    }

    public float getPassedTime() {
        return passedTime;
    }

    private void callSubscribers() {
        Engine engine = Engine.getInstance();

        for (int i = 0; i < ticks; i++)
            for (ITick tick : subscribers) {
                try {
                    tick.tick(this);
                } catch (Exception e) {
                    engine.getLogger().exception(e, "Got error while serving ticks");
                    engine.panic("Unable to call timer subscriber");
                }
            }
    }
}

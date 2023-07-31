package com.vartan.abc.model;


import java.time.Duration;
import java.time.Instant;

public class TickCounter {
    private static final int GAME_TICK_DURATION_MS = 600;
    private int ticks = 0;
    private int fullValue = 0;
    private Instant lastTick = Instant.now();

    public TickCounter() {

    }

    public TickCounter(int ticks) {
        set(ticks);
    }

    public boolean isRunning() {
        return ticks > 0;
    }

    public int getTicksRemaining() {
        return Math.max(ticks, 0);
    }
    public int getTicksElapsed() {
        return fullValue - getTicksRemaining();
    }

    public boolean justElapsed() {
        return ticks == 0;
    }

    public void reset() {
        ticks = fullValue;
    }

    public void set(int value) {
        fullValue = value;
        ticks = value;
    }

    public void tick() {
        lastTick = Instant.now();
        if (ticks >= 0) {
            ticks--;
        }
    }

    public double getPercentDone() {
        int wholeTicksRemaining = getTicksRemaining();
        if (wholeTicksRemaining <= 0) {
            return 1;
        }
        double msSinceLastTick = (double) Duration.between(this.lastTick, Instant.now()).toMillis();

        double fractionalTickPercent = Math.min(
                msSinceLastTick / GAME_TICK_DURATION_MS,
                // Avoid showing close to 100% done on the final tick. The final tick should only be estimated 75% of its
                // duration.
                wholeTicksRemaining == 1 ? 0.75 : 1
        );

        double ticksRemaining = wholeTicksRemaining - fractionalTickPercent;
        double ticksPercent = 1.0 - ticksRemaining / (double) fullValue;
        return ticksPercent;
    }

    /** Returns the full value of the timer when it was set. */
    public int getFullValue() {
        return fullValue;
    }
}

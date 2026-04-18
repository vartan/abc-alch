package com.vartan.abc.model;

public enum Spell {
    LOW_LEVEL_ALCHEMY(31, 3),
    HIGH_LEVEL_ALCHEMY(65, 5);

    public final int xpGained;
    /**
     * The number of game ticks after casting until the spell cools down.
     */
    public final int cooldown;

    Spell(int xpGained, int cooldown) {
        this.xpGained = xpGained;
        this.cooldown = cooldown;
    }
}

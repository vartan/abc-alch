package com.vartan.abc.model;

public enum Spell {
    LOW_LEVEL_ALCHEMY(31, 3, 19),
    HIGH_LEVEL_ALCHEMY(65, 5, 40);

    public final int xpGained;
    /**
     * The number of game ticks afer casting until the spell cools down.
     */
    public final int cooldown;
    public final int widgetChildId;

    Spell(int xpGained, int cooldown, int widgetChildId) {
        this.xpGained = xpGained;
        this.cooldown = cooldown;
        this.widgetChildId = widgetChildId;
    }
}

package com.vartan.abc.model;

import com.google.common.collect.ImmutableMap;
import net.runelite.api.Client;
import net.runelite.api.Skill;
import net.runelite.api.events.StatChanged;

import java.util.List;

public class XpTracker {
    private final ImmutableMap<Skill, AmountDiffer> xpDiffs;

    public XpTracker(List<Skill> skills) {
        ImmutableMap.Builder<Skill, AmountDiffer> xpDiffsBuilder = ImmutableMap.builder();
        for (Skill skill : skills) {
            xpDiffsBuilder.put(skill, new AmountDiffer());
        }
        xpDiffs = xpDiffsBuilder.build();
    }

    public void update(Client client) {
        for (Skill skill : this.xpDiffs.keySet()) {
            AmountDiffer xpDiff = this.xpDiffs.get(skill);
            xpDiff.put(client.getSkillExperience(skill));
        }
    }

    /**
     * Returns the xp change since this function was last called. If this skill is not being tracked, it returns -1.
     */
    public int onStatChanged(StatChanged statChanged) {
        Skill skill = statChanged.getSkill();
        if (!xpDiffs.containsKey(skill)) {
            // Exit immediately if this is not a skill we are tracking.
            return -1;
        }

        int currentXp = statChanged.getXp();
        int xpDiff = xpDiffs.get(skill).put(currentXp).getDiff();

        return xpDiff;
    }
}

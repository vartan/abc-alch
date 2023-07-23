package com.vartan.abc;

import com.vartan.abc.model.SoundEffect;
import net.runelite.api.SoundEffectVolume;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("AbcAlchemy")
public interface AbcAlchConfig extends Config {
    @ConfigItem(
            keyName = "showAlchIntersection",
            name = "Show alch intersection",
            description = "Whether to draw an indicator in the 'sweet spot' where the alchemy spell overlaps most over an item slot."
    )
    default boolean showAlchIntersection() {
        return true;
    }

    @ConfigItem(
            keyName = "showAlchBounds",
            name = "Show alch bounds",
            description = "Whether to draw a box around the alchemy spell."
    )
    default boolean showAlchBounds() {
        return false;
    }

    @ConfigItem(
            keyName = "showItemBounds",
            name = "Show item bounds",
            description = "Whether to draw a box around the item slot that intersects most with the alchemy spell."
    )
    default boolean showItemBounds() {
        return false;
    }

    @ConfigItem(
            keyName = "audioHintVolume",
            name = "Audio hint volume",
            description = "The volume of the alch ready audio hint, from 0-127. 0 fully disables audio hints."
    )
    @Range(max = SoundEffectVolume.HIGH)
    default int audioHintVolume() {
        return 0;
    }

    @ConfigItem(
            keyName = "audioHintSoundEffect",
            name = "Audio hint sound effect",
            description = "Which sound effect plays when alch is ready. Disabled when audio hint volume is 0."
    )
    default SoundEffect audioHintSoundEffect() {
        return SoundEffect.UI_BOOP;
    }
}

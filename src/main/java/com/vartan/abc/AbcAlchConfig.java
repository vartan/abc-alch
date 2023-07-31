package com.vartan.abc;

import com.vartan.abc.model.SoundEffect;
import net.runelite.api.SoundEffectVolume;
import net.runelite.client.config.*;

import java.awt.*;

@ConfigGroup("AbcAlchemy")
public interface AbcAlchConfig extends Config {

    @ConfigSection(
            name = "Alch Intersection",
            description = "Alch Intersection",
            position = 0)
    String alchIntersectionSection = "Alch Intersection";

    @ConfigItem(
            keyName = "showAlchIntersection",
            name = "Show alch intersection",
            description = "Whether to draw an indicator in the 'sweet spot' where the alchemy spell overlaps most " +
                    "over an item slot.",
            section = alchIntersectionSection,
            position = 0
    )
    default boolean showAlchIntersection() {
        return true;
    }

    @ConfigItem(
            keyName = "readyColor",
            name = "Ready color",
            description = "Shown on the spellbook when alchemy is ready, and in the inventory when the mouse is over " +
                    "the alch intersection area.",
            section = alchIntersectionSection,
            position = 1
    )
    default Color readyColor() {
        return Color.GREEN;
    }

    @ConfigItem(
            keyName = "pendingColor",
            name = "Pending color",
            description = "Shown while the player is busy casting alchemy.",
            section = alchIntersectionSection,
            position = 2
    )
    default Color pendingColor() {
        return Color.YELLOW;
    }

    @ConfigItem(
            keyName = "misclickColor",
            name = "Misclick color",
            description = "Shown when the next click will perform alchemy and the mouse isn't in the alch " +
                    "intersection area, or otherwise when clicking in the intersection would not be productive.",
            section = alchIntersectionSection,
            position = 3
    )
    default Color misclickColor() {
        return Color.RED;
    }

    @ConfigItem(
            keyName = "intersectionFillOpacity",
            name = "Intersection fill opacity",
            description = "From 0-1, how opaque should the intersection be filled?",
            section = alchIntersectionSection,
            position = 4
    )
    default double intersectionFillOpacity() {
        return 0.5;
    }

    @ConfigSection(
            name = "Misc",
            description = "Miscellaneous Settings",
            position = 1)
    String miscSection = "Misc";


    @ConfigItem(
            keyName = "showAlchBounds",
            name = "Show alch bounds",
            description = "Whether to draw a box around the alchemy spell.",
            section = miscSection,
            position = 0
    )
    default boolean showAlchBounds() {
        return false;
    }

    @ConfigItem(
            keyName = "Alch bounds color",
            name = "Alch bounds color",
            description = "What color to draw around the alchemy spell, when enabled.",
            section = miscSection,
            position = 1
    )
    default Color alchBoundsColor() {
        return Color.PINK;
    }

    @ConfigItem(
            keyName = "showItemBounds",
            name = "Show item bounds",
            description = "Whether to draw a box around the optimal item slot that intersects most with the alchemy " +
                    "spell.",
            section = miscSection,
            position = 2
    )
    default boolean showItemBounds() {
        return false;
    }

    @ConfigItem(
            keyName = "Alch item color",
            name = "Item bounds color",
            description = "What color to draw around the optimal item, when enabled.",
            section = miscSection,
            position = 3
    )
    default Color itemBoundsColor() {
        return Color.ORANGE;
    }

    @ConfigItem(
            keyName = "audioHintVolume",
            name = "Audio hint volume",
            description = "The volume of the alch ready audio hint, from 0-127. 0 fully disables audio hints.",
            section = miscSection,
            position = 4
    )
    @Range(max = SoundEffectVolume.HIGH)
    default int audioHintVolume() {
        return 0;
    }

    @ConfigItem(
            keyName = "audioHintSoundEffect",
            name = "Audio hint sound effect",
            description = "Which sound effect plays when alch is ready. Disabled when audio hint volume is 0.",
            section = miscSection,
            position = 5
    )
    default SoundEffect audioHintSoundEffect() {
        return SoundEffect.UI_BOOP;
    }

    @ConfigItem(
            keyName = "spellbookClickHint",
            name = "Spellbook click hint",
            description = "Whether to draw a box around the spellbook tab when clicking it is necessary for the next " +
                    "alch.",
            section = miscSection,
            position = 5
    )
    default boolean spellbookClickHint() {
        return true;
    }

    @ConfigItem(
            keyName = "spellbookClickHintColor",
            name = "Spellbook click hint Color",
            description = "What color to draw around the spellbook when spellbook click hint is enabled.",
            section = miscSection,
            position = 6
    )
    default Color spellbookClickHintColor() {
        return Color.GREEN;
    }
}

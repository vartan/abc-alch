package com.vartan.abc;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.inject.Provides;
import com.vartan.abc.model.AlchItem;
import com.vartan.abc.model.Spell;
import com.vartan.abc.model.TickCounter;
import com.vartan.abc.model.XpTracker;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.http.api.item.ItemPrice;
import net.runelite.http.api.item.ItemStats;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Slf4j
@PluginDescriptor(
        name = "ABC Alchemy"
)
public class AbcAlchPlugin extends Plugin {
    private static final int TICKS_PER_MINUTE = 100;
    public final TickCounter magicTicker = new TickCounter();
    /**
     * When this timer is running, the alch overlay will be visible.
     * <p>
     * It is set to 1 minute whenever alchemy is being cast.
     */
    public final TickCounter alchOverlayTimer = new TickCounter();
    /**
     * Whether to update the price list at the next available time, at which point this will be reset to false.
     */
    public boolean readyForPriceUpdate = true;
    /**
     * Whether to use wiki prices.
     */
    public boolean useWikiPrices = true;
    @Inject
    private Client client;
    @Inject
    private AbcAlchConfig config;
    @Inject
    private AbcAlchOverlay abcAlchOverlay;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private ItemManager itemManager;
    @Inject
    private ClientToolbar clientToolbar;
    /**
     * List of all items, sorted by alch profit.
     */
    private List<AlchItem> alchItems;
    /**
     * A list of tick counters for the application. These are ticked in onGameTick.
     */
    private final ArrayList<TickCounter> tickCounters = new ArrayList<>();
    /**
     * The RuneLite sidebar navigation button.
     */
    private NavigationButton navButton;
    /**
     * Plugin sidebar panel.
     */
    private AbcAlchPanel panel;
    /**
     * Keeps track of XP differences.
     */
    private final XpTracker xpTracker = new XpTracker(ImmutableList.of(Skill.MAGIC));

    @Override
    protected void startUp() throws Exception {
        if (client.getGameState() == GameState.LOGGED_IN) {
            onLoginOrActivated();
        }
        panel = new AbcAlchPanel(this, client, itemManager);
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "/icon.png");

        navButton = NavigationButton.builder()
                .tooltip("ABC Alchemy")
                .icon(icon)
                .priority(2)
                .panel(panel)
                .build();

        clientToolbar.addNavigation(navButton);

        tickCounters.add(magicTicker);
        tickCounters.add(alchOverlayTimer);
        overlayManager.add(abcAlchOverlay);

    }

    public void updatePriceList() {
        // TODO: Inquire about making a PR which introduces a function that returns all GE tradeable ItemPrices.
        List<ItemPrice> itemPrices = this.itemManager.search("");
        int natureRunePrice = this.itemManager.getItemPrice(ItemID.NATURE_RUNE);

        ArrayList<AlchItem> tempAlchItems = new ArrayList<>();
        for (ItemPrice price : itemPrices) {
            int itemId = price.getId();
            ItemComposition itemComposition = this.itemManager.getItemComposition(itemId);
            ItemStats itemStats = itemManager.getItemStats(itemId, false);
            String name = price.getName();
            // Filter out any useless items.
            if (itemStats == null || itemComposition == null || name.length() == 0) {
                continue;
            }

            int gePrice = this.useWikiPrices ? itemManager.getWikiPrice(price) : price.getPrice();
            int highAlchPrice = itemComposition.getHaPrice();
            int highAlchProfit = highAlchPrice - gePrice - natureRunePrice;
            int geLimit = itemStats.getGeLimit();
            if (highAlchProfit < 0) {
                // Avoid creating entries for items that will never be displayed.
                continue;
            }

            BufferedImage image = itemManager.getImage(itemId, geLimit, false);
            tempAlchItems.add(new AlchItem(name, gePrice, highAlchPrice, highAlchProfit, geLimit, image));
        }
        // Sort by high alchemy profit,
        this.alchItems = Ordering.from(Comparator.comparing(AlchItem::getHighAlchProfit)).reverse().immutableSortedCopy(tempAlchItems);

        SwingUtilities.invokeLater(() -> panel.updateItemList());
        readyForPriceUpdate = false;
    }


    @Override
    protected void shutDown() throws Exception {
        clientToolbar.removeNavigation(navButton);
        overlayManager.remove(abcAlchOverlay);
        // Remove counters since they will be reinstalled on startup.
        for(TickCounter counter : tickCounters) {
            tickCounters.remove(counter);
        }
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            onLoginOrActivated();
        }
    }

    @Subscribe
    public void onMenuOptionClicked(MenuOptionClicked menuOptionClicked) {
        // TODO: check positive alch click on item.
    }

    @Subscribe
    public void onAnimationChanged(AnimationChanged event) {
        final Actor actor = event.getActor();
        final String actorName = actor.getName();

        if (actor != client.getLocalPlayer()) {
            // We only care about the player
        }
        // TODO: track animations for actions which prevent alching, such as agility.
    }

    @Provides
    AbcAlchConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(AbcAlchConfig.class);
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        tickCounters.forEach(TickCounter::tick);

        if (magicTicker.justElapsed()) {
            maybePlayAudioHint();
        }

        if (readyForPriceUpdate && client.getGameState() == GameState.LOGGED_IN) {
            updatePriceList();
        }

        if (menuIsCast()) {
            alchOverlayTimer.set(TICKS_PER_MINUTE);
        }
    }

    private void maybePlayAudioHint() {
        int volume = config.audioHintVolume();
        if (volume != 0) {
            client.playSoundEffect(config.audioHintSoundEffect().id, volume);
        }
    }

    @Subscribe
    public void onStatChanged(StatChanged statChanged) {
        Skill skill = statChanged.getSkill();
        int xpDiff = xpTracker.onStatChanged(statChanged);
        if (xpDiff < 0) {
            // Exit early if this skill isn't tracked in XpTracker.
            return;
        }

        if (Objects.requireNonNull(skill) == Skill.MAGIC) {
            for (Spell spell : Spell.values()) {
                if (xpDiff == spell.xpGained) {
                    onMagicXpGained(spell);
                    break;
                }
            }
        }
    }

    private void onMagicXpGained(Spell spell) {
        magicTicker.set(spell.cooldown);
    }


    public List<AlchItem> getAlchItems() {
        return this.alchItems;
    }

    /**
     * Whether the user is currently casting alchemy.
     */
    public boolean menuIsCast() {
        // TODO: Support Low Alch.
        Widget selectedWidget = client.getSelectedWidget();
        Widget alchWidget = client.getWidget(WidgetID.SPELLBOOK_GROUP_ID, Spell.HIGH_LEVEL_ALCHEMY.widgetChildId);
        return selectedWidget != null && selectedWidget == alchWidget;
    }

    /**
     * Run when the user logs in, or when they enable the plugin while logged in.
     */
    private void onLoginOrActivated() {
        readyForPriceUpdate = true;
        xpTracker.update(client);
    }
}

package com.vartan.abc;

import com.vartan.abc.model.Spell;
import com.vartan.abc.util.PointUtil;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

public class AbcAlchOverlay extends Overlay {

    private static WidgetInfo[] SPELLBOOK_ICON_IDS = {
            WidgetInfo.RESIZABLE_VIEWPORT_MAGIC_ICON,
            WidgetInfo.RESIZABLE_VIEWPORT_BOTTOM_LINE_MAGIC_ICON,
            WidgetInfo.FIXED_VIEWPORT_MAGIC_ICON
    };

    private final Client client;
    private final AbcAlchConfig config;
    private final AbcAlchPlugin plugin;

    private Rectangle alchBounds = null;
    private Rectangle optimalItemBounds = null;
    private Rectangle alchIntersection = null;

    @Inject
    private AbcAlchOverlay(Client client, AbcAlchPlugin plugin, AbcAlchConfig config) {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ALWAYS_ON_TOP);
        this.client = client;
        this.config = config;
        this.plugin = plugin;
    }

    public Dimension render(Graphics2D graphics) {
        // TODO: Update bounds less often, this doesn't need to be recalculated every frame.
        Widget magicWidget = client.getWidget(WidgetID.SPELLBOOK_GROUP_ID, Spell.HIGH_LEVEL_ALCHEMY.widgetChildId);
        if (magicWidget != null) {
            Rectangle maybeBounds = magicWidget.getBounds();
            if (maybeBounds != null) {
                alchBounds = maybeBounds;
            }
        }
        updateInventoryBounds(graphics);


        if (!plugin.alchOverlayTimer.isRunning()) {
            // Don't draw the overlay when the user isn't alching.
            return null;
        }

        if (alchBounds != null && config.showAlchBounds()) {
            graphics.setColor(config.alchBoundsColor());
            graphics.draw(alchBounds);
        }

        if (optimalItemBounds != null && config.showItemBounds()) {
            graphics.setColor(config.itemBoundsColor());
            graphics.draw(optimalItemBounds);
        }
        renderAlchIntersection(graphics, magicWidget);
        renderSpellbookClickHint(graphics, magicWidget);
        return null;
    }

    private void renderAlchIntersection(Graphics2D graphics, Widget magicWidget) {
        if (alchIntersection == null || !config.showAlchIntersection()) {
            return;
        }
        double magicReadyPercent = plugin.magicTicker.getPercentDone();
        boolean isReady = magicReadyPercent == 1.0;
        boolean mouseIntersects = alchIntersectionContainsMouse();

        Rectangle fillRect = getAlchFillRect(magicReadyPercent);
        // Draw an opaque border around the progress bar to make its position more obvious.

        Color statusColor = isReady ? config.readyColor() : config.pendingColor();
        graphics.setColor(statusColor);
        graphics.draw(fillRect);

        // Fill translucent progress bar inside the intersection box.
        graphics.setColor(getAlchIntersectionColor((float) magicReadyPercent));
        graphics.fill(fillRect);

        boolean menuIsCast = plugin.menuIsCast();
        if (magicWidget != null && !magicWidget.isHidden() || (menuIsCast && mouseIntersects)) {
            graphics.setColor(statusColor);
        } else {
            // Make it obvious to the user that clicking the intersection box will not alch.
            graphics.setColor(config.misclickColor());
        }
        graphics.draw(alchIntersection);

        graphics.setColor(new Color(1, 1, 1, .5f));
    }

    private boolean alchIntersectionContainsMouse() {
        return alchIntersection.contains(PointUtil.toAwtPoint(client.getMouseCanvasPosition()));
    }

    public void renderSpellbookClickHint(Graphics2D graphics, Widget magicWidget) {
        if (!config.spellbookClickHint() || plugin.hideSpellbookHintTimer.isRunning() || (magicWidget != null && !magicWidget.isHidden())) {
            // Don't render the spellbook hint if the spellbook is open and the alch spell is visible.
            return;
        }
        if (plugin.alchOverlayTimer.getTicksElapsed() == 0) {
            // Don't render the spellbook hint during the first tick before the client returns to the
            // spellbook tab.
            return;
        }
        if(plugin.magicTicker.isRunning() && plugin.alchOverlayTimer.getTicksElapsed() > plugin.magicTicker.getFullValue()) {
            // The user may have queued the next alch, avoid showing spellbook hint until the current
            // is complete.
            return;
        }
        Widget spellbookTab = getVisibleSpellbookTabWidget();
        if (spellbookTab == null) {
            // Don't render the spellbook hint if the spellbook tab is hidden.
            return;
        }
        boolean menuIsCast = plugin.menuIsCast();
        if (menuIsCast) {
            // Don't render the spellbook hint if the alch spell is selected.
            return;
        }
        graphics.setColor(config.spellbookClickHintColor());
        graphics.draw(spellbookTab.getBounds());
    }

    private Widget getVisibleSpellbookTabWidget() {
        Widget tabWidget = null;
        for (int i = 0; i < SPELLBOOK_ICON_IDS.length && tabWidget == null; i++) {
            tabWidget = client.getWidget(SPELLBOOK_ICON_IDS[i]);
            if(tabWidget != null && tabWidget.isHidden()) {
                tabWidget = null;
            }
        }
        return tabWidget;
    }

    public Color getAlchIntersectionColor(float magicReadyPercent) {
        Color readyColor = config.readyColor();
        Color pendingColor = config.pendingColor();
        float magicRemainingPercent = 1f - magicReadyPercent;

        return new Color(
                Math.min(1f, readyColor.getRed() / 255f * magicReadyPercent + pendingColor.getRed() / 255f * magicRemainingPercent),
                Math.min(1f, readyColor.getGreen() / 255f * magicReadyPercent + pendingColor.getGreen() / 255f * magicRemainingPercent),
                Math.min(1f, readyColor.getBlue() / 255f * magicReadyPercent + pendingColor.getBlue() / 255f * magicRemainingPercent),
                (float) config.intersectionFillOpacity() * (readyColor.getAlpha() / 255f * magicReadyPercent + pendingColor.getAlpha() / 255f * magicRemainingPercent)
        );
    }


    public void updateInventoryBounds(Graphics2D graphics) {
        Widget inventoryWidget = client.getWidget(WidgetInfo.INVENTORY);
        if (inventoryWidget == null || alchBounds == null || inventoryWidget.isHidden()) {
            return;
        }
        Widget[] dynamicChildren = inventoryWidget.getDynamicChildren();
        if (dynamicChildren.length == 0) {
            return;
        }

        double largestIntersectionArea = 1;
        Rectangle largestOverlapItemBounds = null;
        Rectangle largestOverlapIntersection = null;
        // Find the inventory slot with the largest overlap with the alchemy spell.
        for (Widget item : dynamicChildren) {
            if (item == null || item.isHidden()) {
                continue;
            }

            Rectangle itemBounds = item.getBounds();
            if (itemBounds == null) {
                continue;
            }
            Rectangle intersection = alchBounds.intersection(itemBounds);
            if (intersection.isEmpty()) {
                // An empty rectangle does not necessarily have a width/height of 0.
                // Negative values were observed, which results in a misleading positive area calculation.
                continue;
            }

            double intersectionArea = intersection.getWidth() * intersection.getHeight();
            if (intersectionArea > largestIntersectionArea) {
                largestIntersectionArea = intersectionArea;
                largestOverlapItemBounds = itemBounds;
                largestOverlapIntersection = intersection;
            }
        }
        if (largestOverlapItemBounds != null) {
            this.optimalItemBounds = largestOverlapItemBounds;
            this.alchIntersection = largestOverlapIntersection;
        }
    }

    /**
     * Generates the progress bar for the alchemy timer.
     * <p>
     * The Rectangle grow from the bottom upwards if the y-axis is larger, otherwise it will grow from the left
     * rightwards.
     *
     * @param magicReadyPercent % of the timer has completed.
     * @return Progress bar rectangle
     */
    private Rectangle getAlchFillRect(double magicReadyPercent) {
        if (alchIntersection.getHeight() > alchIntersection.getWidth()) {
            int indicatorY = (int) Math.round(alchIntersection.getY() + alchIntersection.getHeight() * (1.0 - magicReadyPercent));
            int indicatorHeight = (int) Math.round(alchIntersection.getHeight() * magicReadyPercent);
            return new Rectangle((int) alchIntersection.getX(), indicatorY, (int) alchIntersection.getWidth(), indicatorHeight);
        } else {
            return new Rectangle((int) alchIntersection.getX(), (int) alchIntersection.getY(),
                    (int) Math.round(alchIntersection.getWidth() * magicReadyPercent), (int) Math.round(alchIntersection.getHeight()));
        }
    }
}


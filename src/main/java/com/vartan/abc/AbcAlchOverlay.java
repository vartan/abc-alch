package com.vartan.abc;

import com.vartan.abc.model.Spell;
import com.vartan.abc.util.PointUtil;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetID;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

import javax.inject.Inject;
import java.awt.*;

@Slf4j
public class AbcAlchOverlay extends Overlay {
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
            alchBounds = magicWidget.getBounds();
        }
        updateInventoryBounds(graphics);


        if (!plugin.alchOverlayTimer.isRunning()) {
            // Don't draw the overlay when the user isn't alching.
            return null;
        }

        if (alchBounds != null && config.showAlchBounds()) {
            graphics.setColor(Color.PINK);
            graphics.draw(alchBounds);
        }

        if (optimalItemBounds != null && config.showItemBounds()) {
            graphics.setColor(Color.YELLOW);
            graphics.draw(optimalItemBounds);
        }

        renderAlchIntersection(graphics, magicWidget);
        return null;
    }

    private void renderAlchIntersection(Graphics2D graphics, Widget magicWidget) {
        if (alchIntersection == null || !config.showAlchIntersection()) {
            return;
        }
        double magicReadyPercent = plugin.magicTicker.getPercentDone();
        boolean isReady = magicReadyPercent == 1.0;
        Rectangle fillRect = getAlchFillRect(magicReadyPercent);
        boolean mouseIntersects = alchIntersection.contains(PointUtil.toAwtPoint(client.getMouseCanvasPosition()));

        // Draw an opaque border around the progress bar to make its position more obvious.
        graphics.setColor(new Color(isReady ? 0f : 0.5f, 1f, 0f, 1));
        graphics.draw(fillRect);

        // Fill translucent progress bar inside the intersection box.
        graphics.setColor(new Color(isReady ? 0f : 0.5f, 1f, 0f, 0.5f * (float) magicReadyPercent));
        graphics.fill(fillRect);

        // Draw border around the alch intersection area.
        graphics.setStroke(new BasicStroke(2));

        boolean menuIsCast = plugin.menuIsCast();
        if (!magicWidget.isHidden() || (menuIsCast && mouseIntersects)) {
            graphics.setColor(isReady ? Color.GREEN : Color.YELLOW);
        } else {
            Widget spellbookTab = client.getWidget(WidgetInfo.RESIZABLE_VIEWPORT_MAGIC_ICON);
            // If the user is in the wrong tab, point them to the right tab.
            // TODO: This isn't part of the alch intersection, should be moved to its own function with its own config.
            if (!spellbookTab.isHidden() && !menuIsCast) {
                graphics.setColor(Color.GREEN);
                graphics.draw(spellbookTab.getBounds());
            }
            // Make it obvious to the user that clicking the intersection box will not alch.
            graphics.setColor(Color.RED);
        }
        graphics.draw(alchIntersection);
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
        graphics.setStroke(new BasicStroke(1));

        double largestIntersectionArea = 1;
        Rectangle largestOverlapItemBounds = null;
        Rectangle largestOverlapIntersection = null;
        // Find the inventory slot with the largest overlap with the alchemy spell.
        for (Widget item : dynamicChildren) {
            if (item == null || item.isHidden()) {
                continue;
            }

            Rectangle itemBounds = item.getBounds();
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
            int indicatorY = (int) (alchIntersection.getY() + alchIntersection.getHeight() * (1.0 - magicReadyPercent));
            int indicatorHeight = (int) (alchIntersection.getHeight() * magicReadyPercent);
            return new Rectangle((int) alchIntersection.getX(), indicatorY, (int) alchIntersection.getWidth(), indicatorHeight);
        } else {
            return new Rectangle((int) alchIntersection.getX(), (int) alchIntersection.getY(),
                    (int) (alchIntersection.getWidth() * magicReadyPercent), (int) alchIntersection.getHeight());
        }
    }
}


package com.vartan.abc.model;

import com.vartan.abc.util.IntegerUtil;
import net.runelite.client.ui.FontManager;

import java.awt.*;
import java.awt.image.BufferedImage;

public class AlchItem {
    private final String name;
    private final int gePrice;
    private final int geLimit;
    private final BufferedImage image;
    private final int highAlchPrice;
    private final int highAlchProfit;

    public AlchItem(String name, int gePrice, int highAlchPrice, int highAlchProfit, int geLimit, BufferedImage image) {
        this.name = name;
        this.gePrice = gePrice;
        this.highAlchPrice = highAlchPrice;
        this.highAlchProfit = highAlchProfit;
        this.geLimit = geLimit;
        this.image = createAlchImage(image, geLimit);
    }

    private static BufferedImage createAlchImage(BufferedImage bufferedImage, int geLimit) {
        Font smallFont = FontManager.getRunescapeSmallFont();
        Graphics2D imageIconGraphics = bufferedImage.createGraphics();
        String geLimitString = IntegerUtil.toShorthand(geLimit);
        imageIconGraphics.setFont(smallFont);
        imageIconGraphics.setColor(Color.BLACK);
        imageIconGraphics.drawString(geLimitString, 6, 11);
        imageIconGraphics.setColor(Color.YELLOW);
        imageIconGraphics.drawString(geLimitString, 5, 10);
        imageIconGraphics.dispose();
        return bufferedImage;
    }

    public int getGePrice() {
        return gePrice;
    }

    public int getGeLimit() {
        return geLimit;
    }

    public int getHighAlchProfit() {
        return highAlchProfit;
    }

    public String getName() {
        return name;
    }

    public int getHighAlchPrice() {
        return highAlchPrice;
    }

    public BufferedImage getImage() {
        return image;
    }
}

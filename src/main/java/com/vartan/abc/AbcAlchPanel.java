package com.vartan.abc;

import com.vartan.abc.model.AlchItem;
import com.vartan.abc.util.IntegerUtil;
import net.runelite.api.Client;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

public class AbcAlchPanel extends PluginPanel {

    private static final String[] PRICE_SOURCE_OPTIONS = {"Wiki Prices", "Standard Prices"};
    private static final int WIKI_PRICES_INDEX = 0;
    private final Client client;
    private final ItemManager itemManager;
    JTextField minimumTradeLimitField;
    JTextField maxPriceField;
    JToggleButton includeMemberItems;
    JComboBox priceSourceBox;
    AbcAlchPlugin plugin;
    JPanel alchList;
    JButton searchButton;

    public AbcAlchPanel(AbcAlchPlugin plugin, Client client, ItemManager itemManager) {
        super();
        this.plugin = plugin;
        this.client = client;
        this.itemManager = itemManager;
        this.searchButton = new JButton("Search");
        DocumentListener onInputChanged = new DocumentListener() {
            void update(DocumentEvent e) {
                searchButton.setText("Search");
                searchButton.revalidate();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                update(e);
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                update(e);
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                update(e);
            }
        };

        ItemListener itemListener = new ItemListener() {
            void update(ItemEvent e) {
                searchButton.setText("Search");
                searchButton.revalidate();
            }

            @Override
            public void itemStateChanged(ItemEvent e) {
                update(e);
            }
        };

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        final JPanel layoutPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
        layoutPanel.setLayout(boxLayout);
        add(layoutPanel, BorderLayout.NORTH);
        maxPriceField = new JTextField();
        JPanel maxPriceRow = createLabeledRow("Max Price:", maxPriceField);
        maxPriceRow.setToolTipText("Filters out items with a GE price more than the given value. 0 to disable.");
        layoutPanel.add(maxPriceRow);
        maxPriceField.getDocument().addDocumentListener(onInputChanged);

        minimumTradeLimitField = new JTextField();
        JPanel minimumTradeLimitRow = createLabeledRow("Min Trade Limit:", minimumTradeLimitField);
        minimumTradeLimitRow.setToolTipText("Filters out items with a trade limit less than the given value. " + "0 to disable.");
        layoutPanel.add(minimumTradeLimitRow);
        minimumTradeLimitField.getDocument().addDocumentListener(onInputChanged);

        priceSourceBox = new JComboBox(PRICE_SOURCE_OPTIONS);
        priceSourceBox.setSelectedIndex(0);
        priceSourceBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plugin.useWikiPrices = priceSourceBox.getSelectedIndex() == WIKI_PRICES_INDEX;
                plugin.readyForPriceUpdate = true;
            }
        });
        layoutPanel.add(createLabeledRow("Price Source:", priceSourceBox));

        includeMemberItems = new JCheckBox("", true);
        JPanel includeMemberItemsRow = createLabeledRow("Members' Items:", includeMemberItems);
        includeMemberItemsRow.setToolTipText("Include items that are members only.");
        layoutPanel.add(includeMemberItemsRow);
        includeMemberItems.addItemListener(itemListener);

        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                plugin.readyForPriceUpdate = true;
            }
        });
        layoutPanel.add(searchButton);

        alchList = new JPanel();
        layoutPanel.add(alchList);
        BoxLayout alchListBoxLayout = new BoxLayout(alchList, BoxLayout.Y_AXIS);
        alchList.setLayout(alchListBoxLayout);
        alchList.add(new JLabel("Prices will appear once an account is logged in."));

        updateItemList();
        add(alchList);
    }

    private static int readNumericTextField(JTextField field) {
        try {
            return Integer.parseInt(field.getText());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static JPanel createLabeledRow(String label, Component item) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new GridLayout(1, 2));
        rowPanel.add(new JLabel(label));
        rowPanel.add(item);

        return rowPanel;
    }

    /**
     * Clears and regenerates the item list panel using the list of alch items.
     */
    public void updateItemList() {
        searchButton.setText("Refresh");
        searchButton.revalidate();
        if (plugin.getAlchItems() == null) {
            return;
        }
        alchList.removeAll();
        for (AlchItem item : plugin.getAlchItems()) {
            int geLimit = item.getGeLimit();
            int minimumTradeLimit = readNumericTextField(this.minimumTradeLimitField);
            int maxPrice = readNumericTextField(this.maxPriceField);
            boolean isMembers = item.getIsMembers();
            if (!this.includeMemberItems.isSelected() && isMembers) {
                continue;
            }
            boolean filterGeLimit = geLimit != 0 && minimumTradeLimit != 0 && geLimit < minimumTradeLimit;
            boolean filterPrice = maxPrice != 0 && item.getGePrice() >= maxPrice;
            if (filterGeLimit || filterPrice) {
                continue;
            }
            if (item.getHighAlchProfit() <= 0) {
                break;
            }

            JPanel alchItemContainer = generateItemContainer(item);
            alchList.add(alchItemContainer);
        }
        alchList.revalidate();
    }

    private JPanel generateItemContainer(AlchItem item) {
        // Create the container for the entire item.
        JPanel alchItemContainer = new JPanel();
        alchItemContainer.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        alchItemContainer.setLayout(new BoxLayout(alchItemContainer, BoxLayout.X_AXIS));
        alchList.add(alchItemContainer);

        // Add the image to the left.
        BufferedImage itemImage = item.getImage();
        JLabel iconLabel = new JLabel(new ImageIcon(itemImage));
        iconLabel.setToolTipText(item.getName() + " has a Grand Exchange buy limit of " +
                item.getGeLimit() + " every 4 hours");
        alchItemContainer.add(iconLabel);

        // Create a panel for the content on the right
        JPanel alchItemContent = new JPanel();
        alchItemContent.setLayout(new BoxLayout(alchItemContent, BoxLayout.Y_AXIS));
        alchItemContainer.add(alchItemContent);

        // Item name goes on top
        JLabel itemNameLabel = new JLabel(item.getName(), SwingConstants.LEFT);
        itemNameLabel.setForeground(Color.WHITE);
        alchItemContent.setAlignmentX(Component.LEFT_ALIGNMENT);
        alchItemContent.add(itemNameLabel);

        // Add subtext under item name.
        JPanel subtextContainer = createSubtextContainer(item);
        alchItemContent.add(subtextContainer);
        return alchItemContainer;
    }

    /**
     * Creates a grid showing alch price/profit and ge price.
     */
    private JPanel createSubtextContainer(AlchItem item) {
        // Create subtext grid to distribute metadata evenly
        JPanel subtextContainer = new JPanel();
        subtextContainer.setLayout(new GridLayout(2, 2));

        Font smallFont = FontManager.getRunescapeSmallFont();

        // High Alch Price
        JLabel alchPriceLabel = new JLabel("Alch: " + IntegerUtil.toShorthand(item.getHighAlchPrice()));
        alchPriceLabel.setToolTipText("High Alch Price: " + item.getHighAlchPrice());
        alchPriceLabel.setForeground(Color.YELLOW);
        alchPriceLabel.setFont(smallFont);
        subtextContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtextContainer.add(alchPriceLabel);

        // Profit
        JLabel alchProfitLabel = new JLabel("Profit: " + IntegerUtil.toShorthand(item.getHighAlchProfit()));
        alchProfitLabel.setToolTipText("High Alch Profit: " + item.getHighAlchProfit());
        alchProfitLabel.setForeground(Color.GREEN);
        alchProfitLabel.setFont(smallFont);
        subtextContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtextContainer.add(alchProfitLabel);

        // GE Price
        JLabel gePriceLabel = new JLabel("GE: " + IntegerUtil.toShorthand(item.getGePrice()));
        gePriceLabel.setToolTipText("Grand Exchange Price: " + item.getGePrice());
        gePriceLabel.setForeground(Color.ORANGE);
        gePriceLabel.setFont(smallFont);
        subtextContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtextContainer.add(gePriceLabel);
        return subtextContainer;
    }
}

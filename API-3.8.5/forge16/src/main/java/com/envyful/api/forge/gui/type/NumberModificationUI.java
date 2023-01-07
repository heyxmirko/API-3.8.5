package com.envyful.api.forge.gui.type;

import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.gui.item.PositionableItem;
import com.envyful.api.forge.items.ItemBuilder;
import com.envyful.api.gui.Transformer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.PlayerManager;
import com.envyful.api.type.Pair;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class NumberModificationUI {

    private static void open(Builder config) {
        Pane pane = GuiFactory.paneBuilder()
                .topLeftX(0)
                .topLeftY(0)
                .width(9)
                .height(config.config.guiSettings.getHeight())
                .build();

        for (ConfigItem fillerItem : config.config.guiSettings.getFillerItems()) {
            pane.add(GuiFactory.displayable(UtilConfigItem.fromConfigItem(fillerItem, config.transformers)));
        }

        for (EditValueButton button : config.config.getButtons()) {
            UtilConfigItem.addConfigItem(pane, button.getConfigItem(), config.transformers, (envyPlayer, clickType) -> {
                 int newValue = config.currentValue + button.getAmountModifier();

                 if (newValue >= config.config.maxValue) {
                     config.currentValue(config.config.maxValue);
                 } else if (newValue <= config.config.minValue) {
                     config.currentValue(config.config.minValue);
                 } else {
                     config.currentValue(newValue);
                 }

                open(config);
            });
        }

        UtilConfigItem.addConfigItem(pane, config.config.backButton, config.transformers, config.returnHandler);
        UtilConfigItem.addConfigItem(pane, config.config.confirmItem, config.transformers, (envyPlayer, clickType) -> {
            config.confirm.descriptionItem(config.displayItems.get(config.displayItems.size() - 1).getItemStack());
            config.confirm.returnHandler((envyPlayer1, clickType1) -> open(config));
            config.confirm.confirmHandler((clicker, clickerType) -> config.acceptHandler.accept(clicker, clickerType, config.currentValue));
            config.confirm.playerManager(config.playerManager);
            config.confirm.player(envyPlayer);
            config.confirm.transformers(config.transformers);
            config.confirm.open();
        });

        for (Pair<Integer, Integer> position : config.config.currentValue.getPositions()) {
            if (config.config.currentValue.isEnabled()) {
                pane.set(position.getX(), position.getY(),
                         GuiFactory.displayable(new ItemBuilder(UtilConfigItem.fromConfigItem(config.config.currentValue, config.transformers))
                                                        .name(UtilChatColour.translateColourCodes(
                                                                '&',
                                                                config.config.currentValue.getName()
                                                                        .replace("%value%", config.currentValue + "")
                                                        ))
                                                        .build())
                );
            }
        }

        for (ExtendedConfigItem displayItem : config.displayConfigItems) {
            UtilConfigItem.addConfigItem(pane, config.transformers, displayItem);
        }

        for (PositionableItem displayItem : config.displayItems) {
            pane.set(displayItem.getPosX(), displayItem.getPosY(), GuiFactory.displayable(displayItem.getItemStack()));
        }

        GuiFactory.guiBuilder()
                .setPlayerManager(config.playerManager)
                .addPane(pane)
                .setCloseConsumer(envyPlayer -> {})
                .height(config.config.guiSettings.getHeight())
                .title(UtilChatColour.translateColourCodes('&', config.config.guiSettings.getTitle()))
                .build().open(config.player);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private EnvyPlayer<?> player = null;
        private PlayerManager<?, ?> playerManager = null;
        private NumberModificationConfig config = null;
        private BiConsumer<EnvyPlayer<?>, Displayable.ClickType> returnHandler = null;
        private TriConsumer<EnvyPlayer<?>, Displayable.ClickType, Integer> acceptHandler = null;
        private ConfirmationUI.Builder confirm = null;
        private List<ExtendedConfigItem> displayConfigItems = Lists.newArrayList();
        private List<PositionableItem> displayItems = Lists.newArrayList();
        private int currentValue;
        private String key;
        private List<Transformer> transformers = Lists.newArrayList();

        protected Builder() {}

        public Builder player(EnvyPlayer<?> player) {
            this.player = player;
            return this;
        }

        public Builder playerManager(PlayerManager<?, ?> playerManager) {
            this.playerManager = playerManager;
            return this;
        }

        public Builder config(NumberModificationConfig config) {
            this.config = config;
            return this;
        }

        public Builder returnHandler(BiConsumer<EnvyPlayer<?>, Displayable.ClickType> returnHandler) {
            this.returnHandler = returnHandler;
            return this;
        }

        public Builder acceptHandler(TriConsumer<EnvyPlayer<?>, Displayable.ClickType, Integer> acceptHandler) {
            this.acceptHandler = acceptHandler;
            return this;
        }

        public Builder confirm(ConfirmationUI.Builder confirm) {
            this.confirm = confirm;
            return this;
        }

        public Builder displayConfigItems(List<ExtendedConfigItem> displayConfigItems) {
            this.displayConfigItems.addAll(displayConfigItems);
            return this;
        }

        public Builder displayConfigItem(ExtendedConfigItem displayConfigItem) {
            this.displayConfigItems.add(displayConfigItem);
            return this;
        }

        public Builder displayConfigItems(ExtendedConfigItem... displayConfigItems) {
            this.displayConfigItems.addAll(Arrays.asList(displayConfigItems));
            return this;
        }

        public Builder displayItems(List<PositionableItem> displayItems) {
            this.displayItems.addAll(displayItems);
            return this;
        }

        public Builder displayItem(PositionableItem displayItem) {
            this.displayItems.add(displayItem);
            return this;
        }

        public Builder displayItems(PositionableItem... displayItems) {
            this.displayItems.addAll(Arrays.asList(displayItems));
            return this;
        }

        public Builder currentValue(int currentValue) {
            this.currentValue = currentValue;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder transformers(List<Transformer> transformers) {
            this.transformers.addAll(transformers);
            return this;
        }

        public Builder transformer(Transformer transformer) {
            this.transformers.add(transformer);
            return this;
        }

        public Builder transformers(Transformer... transformers) {
            this.transformers.addAll(Arrays.asList(transformers));
            return this;
        }

        public void open() {
            if (this.player == null || this.playerManager == null || this.config == null ||
                    this.returnHandler == null || this.confirm == null || this.acceptHandler == null) {
                return;
            }

            NumberModificationUI.open(this);
        }
    }

    @ConfigSerializable
    public static class NumberModificationConfig {

        private ConfigInterface guiSettings;

        private Map<String, EditValueButton> editValueButtons;

        private ExtendedConfigItem backButton = new ExtendedConfigItem(
                "pixelmon:eject_button", 1, (byte) 0, "&cBack",
                Lists.newArrayList(), 0, 0, Maps.newHashMap()
        );

        private ExtendedConfigItem confirmItem = new ExtendedConfigItem(
                "pixelmon:poke_ball", 1, (byte) 0, "&a&lCONFIRM",
                Lists.newArrayList(), 2, 2, Maps.newHashMap()
        );

        private ExtendedConfigItem currentValue;
        private int maxValue;
        private int minValue;

        public NumberModificationConfig(String title, int height, int maxValue, int minValue,
                                        ExtendedConfigItem currentValue, Map<String, EditValueButton> editValueButtons) {
            this.editValueButtons = editValueButtons;
            this.currentValue = currentValue;
            this.maxValue = maxValue;
            this.minValue = minValue;
            this.guiSettings = new ConfigInterface(
                    title, height, "BLOCK",
                    ImmutableMap.of("one", new ConfigItem(
                            "minecraft:black_stained_glass_pane", 1, (byte) 15, " ", Lists.newArrayList(), Maps.newHashMap()
                    ))
            );
        }

        public NumberModificationConfig() {}

        public List<EditValueButton> getButtons() {
            return Lists.newArrayList(this.editValueButtons.values());
        }

        public ExtendedConfigItem getConfirmItem() {
            return this.confirmItem;
        }

        public ConfigInterface getGuiSettings() {
            return this.guiSettings;
        }

        public ExtendedConfigItem getBackButton() {
            return this.backButton;
        }

        public int getMinValue() {
            return this.minValue;
        }

        public int getMaxValue() {
            return this.maxValue;
        }
    }

    @ConfigSerializable
    public static class EditValueButton {

        private ExtendedConfigItem configItem;
        private int amountModifier;

        public EditValueButton(ExtendedConfigItem configItem, int amountModifier) {
            this.configItem = configItem;
            this.amountModifier = amountModifier;
        }

        public EditValueButton() {
        }

        public ExtendedConfigItem getConfigItem() {
            return this.configItem;
        }

        public int getAmountModifier() {
            return this.amountModifier;
        }
    }
}

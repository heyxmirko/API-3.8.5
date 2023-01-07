package com.envyful.api.forge.gui.type;

import com.envyful.api.config.type.ConfigInterface;
import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.config.UtilConfigItem;
import com.envyful.api.forge.gui.item.PositionableItem;
import com.envyful.api.gui.Transformer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.PlayerManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.util.TriConsumer;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MultiSelectionUI {

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

        int optionPositionsSize = config.config.optionPositions.size();
        List<Map.Entry<String, ConfigItem>> items = new ArrayList<>(config.config.options.entrySet());
        items.sort(Map.Entry.comparingByKey());

        for (int i = (config.page * optionPositionsSize); i < ((config.page + 1) * optionPositionsSize); ++i) {
            int posX = config.config.optionPositions.get(i % optionPositionsSize) % 9;
            int posY = config.config.optionPositions.get(i % optionPositionsSize) / 9;

            if (i >= items.size()) {
                break;
            }

            Map.Entry<String, ConfigItem> item = items.get(i);
            ItemStack itemStack = UtilConfigItem.fromConfigItem(item.getValue(), config.transformers);

            pane.set(posX, posY,
                     GuiFactory.displayableBuilder(itemStack)
                             .clickHandler((envyPlayer, clickType) -> {
                                 if (config.confirm != null) {
                                     config.confirm.descriptionItem(itemStack);
                                     config.confirm.returnHandler((envyPlayer1, clickType1) -> open(config));
                                     config.confirm.confirmHandler((clicker, clickerType) -> config.acceptHandler.accept(clicker, clickerType, item.getKey()));
                                     config.confirm.playerManager(config.playerManager);
                                     config.confirm.player(envyPlayer);
                                     config.confirm.transformers(config.transformers);
                                     config.confirm.open();
                                 } else {
                                     config.selectHandler.accept(envyPlayer, clickType, item.getKey());
                                 }
                             })
                             .build()
            );
        }

        UtilConfigItem.addConfigItem(pane, config.config.backButton, config.transformers, config.returnHandler);

        if (items.size() > optionPositionsSize) {
            UtilConfigItem.addConfigItem(pane, config.config.previousPageButton, config.transformers, (envyPlayer, clickType) -> {
                if (config.page == 0) {
                    config.page = (config.config.options.size() / config.config.optionPositions.size());
                } else {
                    config.page -= 1;
                }

                open(config);
            });

            UtilConfigItem.addConfigItem(pane, config.config.nextPageButton, config.transformers, (envyPlayer, clickType) -> {
                if (config.page == (config.config.options.size() / config.config.optionPositions.size())) {
                    config.page = 0;
                } else {
                    config.page += 1;
                }

                open(config);
            });
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
        private MultiSelectionConfig config = null;
        private BiConsumer<EnvyPlayer<?>, Displayable.ClickType> returnHandler = null;
        private TriConsumer<EnvyPlayer<?>, Displayable.ClickType, String> acceptHandler = null;
        private TriConsumer<EnvyPlayer<?>, Displayable.ClickType, String> selectHandler = null;
        private ConfirmationUI.Builder confirm = null;
        private List<ExtendedConfigItem> displayConfigItems = Lists.newArrayList();
        private List<PositionableItem> displayItems = Lists.newArrayList();
        private int page = 0;
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

        public Builder config(MultiSelectionConfig config) {
            this.config = config;
            return this;
        }

        public Builder returnHandler(BiConsumer<EnvyPlayer<?>, Displayable.ClickType> returnHandler) {
            this.returnHandler = returnHandler;
            return this;
        }

        public Builder acceptHandler(TriConsumer<EnvyPlayer<?>, Displayable.ClickType, String> acceptHandler) {
            this.acceptHandler = acceptHandler;
            return this;
        }

        public Builder selectHandler(TriConsumer<EnvyPlayer<?>, Displayable.ClickType, String> selectHandler) {
            this.selectHandler = selectHandler;
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

        public Builder page(int page) {
            this.page = page;
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
            if (this.player == null || this.playerManager == null || this.config == null || this.returnHandler == null) {
                return;
            }

            MultiSelectionUI.open(this);
        }
    }

    @ConfigSerializable
    public static class MultiSelectionConfig {

        private ConfigInterface guiSettings;

        private Map<String, ConfigItem> options;
        private List<Integer> optionPositions;

        private ExtendedConfigItem backButton = new ExtendedConfigItem(
                "pixelmon:eject_button", 1, (byte) 0, "&cBack",
                Lists.newArrayList(), 0, 0, Maps.newHashMap()
        );

        private ExtendedConfigItem nextPageButton = new ExtendedConfigItem(
                "pixelmon:trade_holder_right", 1, (byte) 0, "&aNext Page",
                Lists.newArrayList(), 8, 5, Maps.newHashMap()
        );

        private ExtendedConfigItem previousPageButton = new ExtendedConfigItem(
                "pixelmon:trade_holder_left", 1, (byte) 0, "&aPrevious Page",
                Lists.newArrayList(), 0, 5, Maps.newHashMap()
        );

        public MultiSelectionConfig(String title, int height, Map<String, ConfigItem> options,
                                    List<Integer> optionPositions) {
            this.options = options;
            this.optionPositions = optionPositions;
            this.guiSettings = new ConfigInterface(
                    title, height, "BLOCK",
                    ImmutableMap.of("one", new ConfigItem(
                            "minecraft:black_stained_glass_pane", 1," ", Lists.newArrayList()
                    ))
            );
        }

        public MultiSelectionConfig() {}

        public ConfigInterface getGuiSettings() {
            return this.guiSettings;
        }

        public Map<String, ConfigItem> getOptions() {
            return this.options;
        }

        public List<Integer> getOptionPositions() {
            return this.optionPositions;
        }

        public ExtendedConfigItem getBackButton() {
            return this.backButton;
        }

        public ExtendedConfigItem getNextPageButton() {
            return this.nextPageButton;
        }

        public ExtendedConfigItem getPreviousPageButton() {
            return this.previousPageButton;
        }
    }
}

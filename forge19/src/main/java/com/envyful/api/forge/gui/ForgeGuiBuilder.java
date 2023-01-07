package com.envyful.api.forge.gui;

import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.gui.close.ForgeCloseConsumer;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.gui.Gui;
import com.envyful.api.gui.close.CloseConsumer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.PlayerManager;
import com.google.common.collect.Lists;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.function.Consumer;

/**
 *
 * Builder implementation for the ForgeGui
 *
 */
public class ForgeGuiBuilder implements Gui.Builder {

    private Component title;
    private int height = 5;
    private List<Pane> panes = Lists.newArrayList();
    private PlayerManager<ForgeEnvyPlayer, ServerPlayer> playerManager;
    private ForgeCloseConsumer closeConsumer = (ForgeCloseConsumer) GuiFactory.closeConsumerBuilder().build();

    @Override
    public Gui.Builder title(Object title) {
        if (title instanceof Component) {
            this.title = (Component) title;
        } else if (title instanceof String) {
            this.title = UtilChatColour.colour((String) title);
        } else {
            throw new RuntimeException("Unsupported title type given");
        }

        return this;
    }

    @Override
    public Gui.Builder height(int height) {
        this.height = height;
        return this;
    }

    @Override
    public Gui.Builder addPane(Pane pane) {
        this.panes.add(pane);
        return this;
    }

    @Override
    public Gui.Builder setPlayerManager(PlayerManager<?, ?> playerManager) {
        this.playerManager = (PlayerManager<ForgeEnvyPlayer, ServerPlayer>) playerManager;
        return this;
    }

    @Override
    public Gui.Builder setCloseConsumer(Consumer<EnvyPlayer<?>> consumer) {
        return this.closeConsumer(consumer);
    }

    @Override
    public Gui.Builder closeConsumer(Consumer<EnvyPlayer<?>> consumer) {
        return this.closeConsumer(GuiFactory.closeConsumerBuilder().handler(consumer::accept).build());
    }

    @Override
    public Gui.Builder closeConsumer(CloseConsumer<?, ?> closeConsumer) {
        this.closeConsumer = (ForgeCloseConsumer)closeConsumer;
        return this;
    }

    @Override
    public Gui build() {
        if (this.playerManager == null) {
            throw new RuntimeException("Cannot build GUI without PlayerManager being set");
        }

        return new ForgeGui(this.title, this.height, this.playerManager, this.closeConsumer, this.panes.toArray(new Pane[0]));
    }
}

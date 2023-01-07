package com.envyful.api.spigot.gui;

import com.envyful.api.gui.Gui;
import com.envyful.api.gui.close.CloseConsumer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.PlayerManager;
import com.envyful.api.spigot.gui.close.SpigotCloseConsumer;
import com.envyful.api.spigot.player.SpigotPlayerManager;
import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 *
 * Builder implementation for the SpigotGui
 *
 */
public class SpigotGuiBuilder implements Gui.Builder {

    private Component title;
    private int height = 5;
    private List<Pane> panes = Lists.newArrayList();
    private SpigotPlayerManager playerManager;
    private SpigotCloseConsumer closeConsumer = (SpigotCloseConsumer) GuiFactory.closeConsumerBuilder().build();

    @Override
    public Gui.Builder title(Object title) {
        if (title instanceof Component) {
            this.title = (Component) title;
        } else if (title instanceof String) {
            //TODO:
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
        this.playerManager = (SpigotPlayerManager) playerManager;
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
        this.closeConsumer = (SpigotCloseConsumer) closeConsumer;
        return this;
    }

    @Override
    public Gui build() {
        if (this.playerManager == null) {
            throw new RuntimeException("Cannot build GUI without PlayerManager being set");
        }

        return new SpigotGui(this.title, this.height, this.playerManager, this.closeConsumer, this.panes.toArray(new Pane[0]));
    }
}

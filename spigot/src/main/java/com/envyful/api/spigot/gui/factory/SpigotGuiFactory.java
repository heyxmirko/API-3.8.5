package com.envyful.api.spigot.gui.factory;

import com.envyful.api.gui.Gui;
import com.envyful.api.gui.close.CloseConsumer;
import com.envyful.api.gui.factory.PlatformGuiFactory;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.gui.pane.TickHandler;
import com.envyful.api.gui.pane.type.PagedPane;
import com.envyful.api.spigot.gui.SpigotGui;
import com.envyful.api.spigot.gui.SpigotGuiBuilder;
import com.envyful.api.spigot.gui.close.SpigotCloseConsumer;
import com.envyful.api.spigot.gui.item.SpigotSimpleDisplayable;
import com.envyful.api.spigot.gui.pane.SpigotSimplePane;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 *
 * Spigot implementation of the {@link PlatformGuiFactory} interface
 *
 */
public class SpigotGuiFactory implements PlatformGuiFactory<ItemStack> {

    private final Plugin plugin;

    public SpigotGuiFactory(Plugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(new SpigotGui.Listener(), plugin);
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

    @Override
    public Displayable.Builder<ItemStack> displayableBuilder() {
        return new SpigotSimpleDisplayable.Builder();
    }

    @Override
    public Pane.Builder paneBuilder() {
        return new SpigotSimplePane.Builder();
    }

    @Override
    public PagedPane.Builder pagedPaneBuilder() {
        return null; //TODO: not made yet
    }

    @Override
    public Gui.Builder guiBuilder() {
        return new SpigotGuiBuilder();
    }

    @Override
    public TickHandler.Builder tickBuilder() {
        return null;
    }

    @Override
    public CloseConsumer.Builder<?, ?> closeConsumerBuilder() {
        return new SpigotCloseConsumer.Builder();
    }
}

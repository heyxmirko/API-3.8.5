package com.envyful.api.spigot.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class LazyListener<T extends Plugin> implements Listener {

    protected final T plugin;

    public LazyListener(T plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, this.plugin);
    }
}

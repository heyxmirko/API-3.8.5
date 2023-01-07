package com.envyful.api.spigot.player;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.player.PlayerManager;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.player.attribute.data.PlayerAttributeData;
import com.envyful.api.player.save.SaveManager;
import com.envyful.api.player.save.impl.EmptySaveManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * Spigot implementation of the {@link PlayerManager} interface.
 * Registers the {@link PlayerListener} class as a listener with Spigot on instantiation so that it can
 * automatically update the cache when player log in and out of the server.
 *
 * Simple instantiation as not enough arguments to warrant a builder class and
 */
public class SpigotPlayerManager implements PlayerManager<SpigotEnvyPlayer, Player> {

    private final Map<UUID, SpigotEnvyPlayer> cachedPlayers = Maps.newHashMap();
    private final List<PlayerAttributeData> attributeData = Lists.newArrayList();

    private SaveManager<Player> saveManager = new EmptySaveManager<>();

    public SpigotPlayerManager(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), plugin);
    }

    @Override
    public SpigotEnvyPlayer getPlayer(Player player) {
        return this.getPlayer(player.getUniqueId());
    }

    @Override
    public SpigotEnvyPlayer getPlayer(UUID uuid) {
        return this.cachedPlayers.get(uuid);
    }

    @Override
    public SpigotEnvyPlayer getOnlinePlayer(String username) {
        for (SpigotEnvyPlayer online : this.cachedPlayers.values()) {
            if (online.getParent().getName().equals(username)) {
                return online;
            }
        }

        return null;
    }

    @Override
    public SpigotEnvyPlayer getOnlinePlayerCaseInsensitive(String username) {
        for (SpigotEnvyPlayer online : this.cachedPlayers.values()) {
            if (online.getParent().getName().equalsIgnoreCase(username)) {
                return online;
            }
        }

        return null;
    }

    @Override
    public List<SpigotEnvyPlayer> getOnlinePlayers() {
        return Collections.unmodifiableList(Lists.newArrayList(this.cachedPlayers.values()));
    }

    @Override
    public List<PlayerAttribute<?>> getOfflineAttributes(UUID uuid) {
        return this.saveManager.loadData(uuid);
    }

    @Override
    public void registerAttribute(Object manager, Class<? extends PlayerAttribute<?>> attribute) {
        this.attributeData.add(new PlayerAttributeData(manager, attribute));

        if (this.saveManager != null) {
            this.saveManager.registerAttribute(manager, attribute);
        }
    }

    @Override
    public void setSaveManager(SaveManager<Player> saveManager) {
        this.saveManager = saveManager;
    }

    private final class PlayerListener implements Listener {

        private final SpigotPlayerManager manager;

        private PlayerListener(SpigotPlayerManager manager) {
            this.manager = manager;
        }

        @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
        public void onAsyncPrePlayerLogin(AsyncPlayerPreLoginEvent event) {
            SpigotEnvyPlayer player = new SpigotEnvyPlayer(event.getUniqueId());
            this.manager.cachedPlayers.put(event.getUniqueId(), player);

            UtilConcurrency.runAsync(() -> {
                for (PlayerAttributeData attributeDatum : this.manager.attributeData) {
                    PlayerAttribute<?> instance = attributeDatum.getInstance(player);

                    if (instance == null) {
                        continue;
                    }

                    instance.load();
                    attributeDatum.addToMap(player.attributes, instance);
                }
            });
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerLoginEvent event) {
            this.manager.cachedPlayers.get(event.getPlayer().getUniqueId()).setPlayer(event.getPlayer());
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerQuit(PlayerQuitEvent event) {
            SpigotEnvyPlayer player = this.manager.cachedPlayers.remove(event.getPlayer().getUniqueId());

            if (player == null) {
                return;
            }

            UtilConcurrency.runAsync(() -> {
                for (PlayerAttribute<?> value : player.attributes.values()) {
                    if (value != null) {
                        value.save();
                    }
                }
            });
        }

        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerRespawn(PlayerPostRespawnEvent event) {
            UtilConcurrency.runLater(() -> {
                SpigotEnvyPlayer player = this.manager.cachedPlayers.get(event.getPlayer().getUniqueId());

                player.setPlayer(event.getPlayer());
            }, 5L);
        }
    }
}

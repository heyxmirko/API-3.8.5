package com.envyful.api.velocity.player;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.player.PlayerManager;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.player.attribute.data.PlayerAttributeData;
import com.envyful.api.player.save.SaveManager;
import com.envyful.api.player.save.impl.EmptySaveManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * Velocity implementation of the {@link PlayerManager} interface.
 * Registers the {@link PlayerListener} class as a listener with Velocity on instantiation so that it can
 * automatically update the cache when player log in and out of the server.
 *
 * Simple instantiation as not enough arguments to warrant a builder class and
 */
public class VelocityPlayerManager implements PlayerManager<VelocityEnvyPlayer, Player> {

    private final Map<UUID, VelocityEnvyPlayer> cachedPlayers = Maps.newHashMap();
    private final List<PlayerAttributeData> attributeData = Lists.newArrayList();

    private SaveManager<Player> saveManager = new EmptySaveManager<>();
    private ProxyServer proxyServer;

    public VelocityPlayerManager(Object plugin, ProxyServer proxy) {
        this.proxyServer = proxy;
        proxy.getEventManager().register(plugin, new PlayerListener(this));
    }

    @Override
    public VelocityEnvyPlayer getPlayer(Player player) {
        return this.getPlayer(player.getUniqueId());
    }

    @Override
    public VelocityEnvyPlayer getPlayer(UUID uuid) {
        return this.cachedPlayers.get(uuid);
    }

    @Override
    public VelocityEnvyPlayer getOnlinePlayer(String username) {
        for (VelocityEnvyPlayer online : this.cachedPlayers.values()) {
            if (online.getParent().getUsername().equals(username)) {
                return online;
            }
        }

        return null;
    }

    @Override
    public VelocityEnvyPlayer getOnlinePlayerCaseInsensitive(String username) {
        for (VelocityEnvyPlayer online : this.cachedPlayers.values()) {
            if (online.getParent().getUsername().equalsIgnoreCase(username)) {
                return online;
            }
        }

        return null;
    }

    @Override
    public List<VelocityEnvyPlayer> getOnlinePlayers() {
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

    public ProxyServer getProxyServer() {
        return this.proxyServer;
    }

    @Override
    public void setSaveManager(SaveManager<Player> saveManager) {
        this.saveManager = saveManager;
    }

    private final class PlayerListener {

        private final VelocityPlayerManager manager;

        private PlayerListener(VelocityPlayerManager manager) {
            this.manager = manager;
        }

        @Subscribe(order = PostOrder.LAST)
        public void onAsyncPrePlayerLogin(LoginEvent event) {
            VelocityEnvyPlayer player = new VelocityEnvyPlayer(this.manager.proxyServer, event.getPlayer().getUniqueId());
            player.setPlayer(event.getPlayer());
            this.manager.cachedPlayers.put(event.getPlayer().getUniqueId(), player);

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

        @Subscribe(order = PostOrder.LAST)
        public void onPlayerQuit(DisconnectEvent event) {
            VelocityEnvyPlayer player = this.manager.cachedPlayers.remove(event.getPlayer().getUniqueId());

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
    }
}

package com.envyful.api.forge.player;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.player.PlayerManager;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.player.attribute.data.PlayerAttributeData;
import com.envyful.api.player.save.SaveManager;
import com.envyful.api.player.save.impl.EmptySaveManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 *
 * Forge implementation of the {@link PlayerManager} interface.
 * Registers the {@link PlayerListener} class as a listener with forge on instantiation so that it can
 * automatically update the cache when player log in and out of the server.
 *
 * Simple instantiation as not enough arguments to warrant a builder class and
 */
public class ForgePlayerManager implements PlayerManager<ForgeEnvyPlayer, ServerPlayer> {

    private final Map<UUID, ForgeEnvyPlayer> cachedPlayers = Maps.newHashMap();
    private final List<PlayerAttributeData> attributeData = Lists.newArrayList();

    private SaveManager<ServerPlayer> saveManager = new EmptySaveManager<>();

    public ForgePlayerManager() {
        MinecraftForge.EVENT_BUS.register(new PlayerListener(this));
        UsernameFactory.init();
    }

    @Override
    public ForgeEnvyPlayer getPlayer(ServerPlayer player) {
        return this.getPlayer(player.getUUID());
    }

    @Override
    public ForgeEnvyPlayer getPlayer(UUID uuid) {
        return this.cachedPlayers.get(uuid);
    }

    @Override
    public ForgeEnvyPlayer getOnlinePlayer(String username) {
        for (ForgeEnvyPlayer online : this.cachedPlayers.values()) {
            if (online.getParent().getName().getString().equals(username)) {
                return online;
            }
        }

        return null;
    }

    @Override
    public ForgeEnvyPlayer getOnlinePlayerCaseInsensitive(String username) {
        for (ForgeEnvyPlayer online : this.cachedPlayers.values()) {
            if (online.getParent().getName().getString().equalsIgnoreCase(username)) {
                return online;
            }
        }

        return null;
    }

    @Override
    public List<ForgeEnvyPlayer> getOnlinePlayers() {
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
    public void setSaveManager(SaveManager<ServerPlayer> saveManager) {
        this.saveManager = saveManager;
    }

    private final class PlayerListener {

        private final ForgePlayerManager manager;
        private long lastSave = -1L;

        private PlayerListener(ForgePlayerManager manager) {
            this.manager = manager;
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            ForgeEnvyPlayer player = new ForgeEnvyPlayer((ServerPlayer) event.getEntity());
            this.manager.cachedPlayers.put(event.getEntity().getUUID(), player);

            UtilConcurrency.runAsync(() -> {
                List<PlayerAttribute<?>> playerAttributes = this.manager.saveManager.loadData(player);
                for (PlayerAttributeData attributeDatum : this.manager.attributeData) {
                    PlayerAttribute<?> attribute = this.findAttribute(attributeDatum, playerAttributes);

                    if (attribute == null) {
                        continue;
                    }

                    attributeDatum.addToMap(player.attributes, attribute);
                }
            });
        }

        private PlayerAttribute<?> findAttribute(PlayerAttributeData attributeDatum,
                                                 List<PlayerAttribute<?>> playerAttributes) {
            for (PlayerAttribute<?> playerAttribute : playerAttributes) {
                if (Objects.equals(attributeDatum.getAttributeClass(), playerAttribute.getClass())) {
                    return playerAttribute;
                }
            }

            return null;
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPlayerQuit(PlayerEvent.PlayerLoggedOutEvent event) {
            ForgeEnvyPlayer player = this.manager.cachedPlayers.remove(event.getEntity().getUUID());

            if (player == null) {
                return;
            }

            UtilConcurrency.runAsync(() -> {
                for (PlayerAttribute<?> value : player.attributes.values()) {
                    if (value != null) {
                        this.manager.saveManager.saveData(player, value);
                    }
                }
            });
        }

        @SubscribeEvent
        public void onWorldSave(LevelEvent.Save event) {
            if (!this.shouldSave()) {
                return;
            }

            this.lastSave = System.currentTimeMillis();

            UtilConcurrency.runAsync(() -> {
                for (ForgeEnvyPlayer onlinePlayer : this.manager.getOnlinePlayers()) {
                    for (PlayerAttribute<?> value : onlinePlayer.attributes.values()) {
                        UtilConcurrency.runAsync(value::save);
                    }
                }
            });
        }

        private boolean shouldSave() {
            return this.lastSave == -1 || (System.currentTimeMillis() - this.lastSave) >= TimeUnit.MINUTES.toMillis(2);
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
            UtilConcurrency.runLater(() -> {
                ForgeEnvyPlayer player = this.manager.cachedPlayers.get(event.getEntity().getUUID());

                player.setPlayer((ServerPlayer) event.getEntity());
            }, 5L);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void onPreServerShutdown(ServerStoppingEvent event) {
            UtilConcurrency.runAsync(() -> {
                for (ForgeEnvyPlayer player : this.manager.cachedPlayers.values()) {
                    for (PlayerAttribute<?> value : player.attributes.values()) {
                        if (value != null) {
                            this.manager.saveManager.saveData(player, value);
                        }
                    }
                }
            });
        }
    }
}

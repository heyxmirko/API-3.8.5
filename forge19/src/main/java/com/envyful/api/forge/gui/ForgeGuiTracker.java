package com.envyful.api.forge.gui;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.forge.listener.LazyListener;
import com.envyful.api.player.EnvyPlayer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * A class to track all open {@link ForgeGui}s and update them every tick (to update any changed items after player clicks)
 *
 */
public class ForgeGuiTracker {

    private static final Map<UUID, ForgeGui> OPEN_GUIS = Maps.newHashMap();
    private static final Set<UUID> REQUIRED_UPDATE = Sets.newHashSet();

    static {
        new ForgeGuiTickListener();
    }

    public static void addGui(EnvyPlayer<?> player, ForgeGui gui) {
        if (player == null) {
            return;
        }

        OPEN_GUIS.put(player.getUuid(), gui);
    }

    public static boolean inGui(EnvyPlayer<?> player) {
        return OPEN_GUIS.containsKey(player.getUuid());
    }

    public static void removePlayer(EnvyPlayer<?> player) {
        if (player == null) {
            return;
        }

        OPEN_GUIS.remove(player.getUuid());
    }

    public static void enqueueUpdate(EnvyPlayer<?> player) {
        if (player == null) {
            return;
        }

        REQUIRED_UPDATE.add(player.getUuid());
    }

    public static boolean requiresUpdate(ServerPlayer player) {
        return REQUIRED_UPDATE.contains(player.getUUID());
    }

    public static void dequeueUpdate(ServerPlayer player) {
        REQUIRED_UPDATE.remove(player.getUUID());
    }

    private static final class ForgeGuiTickListener extends LazyListener {

        private ForgeGuiTickListener() {
            super();
        }

        @SubscribeEvent
        public void onServerTick(TickEvent.ServerTickEvent event) {
            UtilConcurrency.runAsync(() -> {
                for (ForgeGui value : OPEN_GUIS.values()) {
                    value.update();
                }
            });
        }
    }
}

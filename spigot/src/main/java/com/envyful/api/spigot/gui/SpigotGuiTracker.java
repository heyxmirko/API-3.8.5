package com.envyful.api.spigot.gui;

import com.envyful.api.player.EnvyPlayer;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * A class to track all open {@link SpigotGui}s and update them every tick (to update any changed items after player clicks)
 *
 */
public class SpigotGuiTracker {

    private static final Map<UUID, InventoryDetails> OPEN_GUIS = Maps.newHashMap();
    private static final Set<UUID> REQUIRED_UPDATE = Sets.newHashSet();

    public static void addGui(EnvyPlayer<?> player, SpigotGui gui, Inventory inventory) {
        if (player == null) {
            return;
        }

        OPEN_GUIS.put(player.getUuid(), new InventoryDetails(player.getUuid(), inventory, gui));
    }

    public static InventoryDetails getDetails(Player player) {
        return getDetails(player.getUniqueId());
    }

    public static InventoryDetails getDetails(UUID uuid) {
        return OPEN_GUIS.get(uuid);
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

    public static boolean requiresUpdate(Player player) {
        return REQUIRED_UPDATE.contains(player.getUniqueId());
    }

    public static void dequeueUpdate(Player player) {
        REQUIRED_UPDATE.remove(player.getUniqueId());
    }

    public static class InventoryDetails {

        private final UUID player;
        private final Inventory inventory;
        private final SpigotGui gui;

        public InventoryDetails(UUID player, Inventory inventory, SpigotGui gui) {
            this.player = player;
            this.inventory = inventory;
            this.gui = gui;
        }

        public UUID getPlayer() {
            return this.player;
        }

        public SpigotGui getGui() {
            return this.gui;
        }

        public Inventory getInventory() {
            return this.inventory;
        }
    }
}

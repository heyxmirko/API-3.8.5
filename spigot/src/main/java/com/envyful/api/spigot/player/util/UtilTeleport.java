package com.envyful.api.spigot.player.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 *
 * Static utility class for teleporting players
 *
 */
public class UtilTeleport {

    /**
     *
     * Teleports the player to the given position in the world
     *
     * @param player The player
     * @param pos The location in a world
     */
    public static void teleportPlayer(Player player, Location pos) {
        player.teleportAsync(pos, PlayerTeleportEvent.TeleportCause.PLUGIN);
    }
}

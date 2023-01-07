package com.envyful.api.spigot.player.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 *
 * Static utility class for handling getting online & offline players
 *
 */
public class UtilPlayer {

    public static String getName(CommandSender source) {
        return source.getName();
    }

    public static boolean hasPermission(CommandSender player, String permission) {
        return player.hasPermission(permission);
    }

    public static boolean isOP(CommandSender player) {
        return player.isOp();
    }

    /**
     *
     * Forces the player to run a command
     *
     * @param player The player running the command
     * @param command The command
     */
    public static void runCommand(CommandSender player, String command) {
        Bukkit.getServer().dispatchCommand(player, command);
    }

    /**
     *
     * Gets the online player with the given name.
     * Returns null if not online
     *
     * @param name The name of the player
     * @return The online player
     */
    public static Player findByName(String name) {
        return Bukkit.getPlayer(name);
    }

    /**
     *
     * Gets the online player with the given {@link UUID}.
     * Returns null if not online
     *
     * @param uuid The uuid of the player
     * @return The online player
     */
    public static Player getOnlinePlayer(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }
}

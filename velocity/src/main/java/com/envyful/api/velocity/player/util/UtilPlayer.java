package com.envyful.api.velocity.player.util;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

/**
 *
 * Static utility class for handling getting online & offline players
 *
 */
public class UtilPlayer {

    public static String getName(CommandSource source) {
        if (!(source instanceof Player)) {
            return "CONSOLE";
        }

        return ((Player) source).getUsername();
    }

    public static boolean hasPermission(CommandSource player, String permission) {
        return player.hasPermission(permission);
    }
}

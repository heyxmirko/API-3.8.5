package com.envyful.api.forge.player.util;

import net.minecraft.commands.CommandSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.ServerOpListEntry;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.UUID;

/**
 *
 * Static utility class for handling getting online & offline players
 *
 */
public class UtilPlayer {

    public static String getName(CommandSource source) {
        if (source instanceof ServerPlayer) {
            return ((ServerPlayer) source).getName().getString();
        }

        return "CONSOLE";
    }

    public static boolean hasPermission(ServerPlayer player, String permission) {
        return (/*PermissionAPI.getPermission(player, permission) || */player.hasPermissions(4) || isOP(player)); //TODO:
    }

    public static boolean isOP(ServerPlayer player) {
        ServerOpListEntry entry = ServerLifecycleHooks.getCurrentServer().getPlayerList().getOps().get(player.getGameProfile());
        return entry != null && entry.getLevel() >= ServerLifecycleHooks.getCurrentServer().getOperatorUserPermissionLevel();
    }

    /**
     *
     * Forces the player to run a command
     *
     * @param player The player running the command
     * @param command The command
     */
    public static void runCommand(ServerPlayer player, String command) {
        ServerLifecycleHooks.getCurrentServer().getCommands().performCommand(player.createCommandSourceStack(), command);
    }

    /**
     *
     * Gets the online player with the given name.
     * Returns null if not online
     *
     * @param name The name of the player
     * @return The online player
     */
    public static ServerPlayer findByName(String name) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(name);
    }

    /**
     *
     * Gets the online player with the given {@link UUID}.
     * Returns null if not online
     *
     * @param uuid The uuid of the player
     * @return The online player
     */
    public static ServerPlayer getOnlinePlayer(UUID uuid) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
    }
}

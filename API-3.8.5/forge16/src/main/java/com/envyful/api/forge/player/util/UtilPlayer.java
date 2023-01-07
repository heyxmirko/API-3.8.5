package com.envyful.api.forge.player.util;

import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.management.OpEntry;
import net.minecraftforge.fml.server.ServerLifecycleHooks;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.UUID;

/**
 *
 * Static utility class for handling getting online & offline players
 *
 */
public class UtilPlayer {

    public static String getName(ICommandSource source) {
        if (source instanceof ServerPlayerEntity) {
            return ((ServerPlayerEntity) source).getName().getString();
        }

        return "CONSOLE";
    }

    public static boolean hasPermission(ServerPlayerEntity player, String permission) {
        return (PermissionAPI.hasPermission(player, permission) || player.hasPermissions(4) || isOP(player));
    }

    public static boolean isOP(ServerPlayerEntity player) {
        OpEntry entry = ServerLifecycleHooks.getCurrentServer().getPlayerList().getOps().get(player.getGameProfile());
        return entry != null;
    }

    /**
     *
     * Forces the player to run a command
     *
     * @param player The player running the command
     * @param command The command
     */
    public static void runCommand(ServerPlayerEntity player, String command) {
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
    public static ServerPlayerEntity findByName(String name) {
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
    public static ServerPlayerEntity getOnlinePlayer(UUID uuid) {
        return ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayer(uuid);
    }
}

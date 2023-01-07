package com.envyful.api.forge.server;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 *
 * Static utility class for handling forge server function
 *
 */
public class UtilForgeServer {

    /**
     *
     * Executes the given command from the server
     *
     * Ensure to set the server first
     *
     * @param command The command to execute
     */
    public static void executeCommand(String command) {
        ServerLifecycleHooks.getCurrentServer().getCommands().performCommand(ServerLifecycleHooks.getCurrentServer().createCommandSourceStack(), command);
    }


    /**
     *
     * Executes the given command from the given player
     *
     * Ensure to set the server first
     *
     * @param player THe player to execute the command as
     * @param command The command to execute
     */
    public static void executeCommand(ServerPlayer player, String command) {
        ServerLifecycleHooks.getCurrentServer().getCommands().performCommand(player.createCommandSourceStack(), command);
    }
}

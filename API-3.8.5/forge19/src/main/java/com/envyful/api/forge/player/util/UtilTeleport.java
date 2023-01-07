package com.envyful.api.forge.player.util;

import com.mojang.math.Vector3d;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

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
     * @param world The world
     * @param pos The x, y, z coords
     */
    public static void teleportPlayer(ServerPlayer player, Level world, Vector3d pos) {
        player.teleportTo((ServerLevel) world, pos.x, pos.y, pos.z, (float)player.getLookAngle().x, (float)player.getLookAngle().y);
    }

    /**
     *
     * Teleports the player to the given position in the world
     *
     * @param player The player
     * @param world The world
     * @param pos The x, y, z coords
     * @param pitch The pitch
     * @param yaw The yaw
     */
    public static void teleportPlayer(ServerPlayer player, Level world, Vector3d pos, float pitch, float yaw) {
        player.teleportTo((ServerLevel) world, pos.x, pos.y, pos.z, pitch, yaw);
    }
}

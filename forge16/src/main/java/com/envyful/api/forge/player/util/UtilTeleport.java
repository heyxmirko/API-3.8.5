package com.envyful.api.forge.player.util;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

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
    public static void teleportPlayer(ServerPlayerEntity player, World world, Vector3d pos) {
        player.teleportTo((ServerWorld) world, pos.get(Direction.Axis.X), pos.get(Direction.Axis.Y), pos.get(Direction.Axis.Z), (float)player.getLookAngle().x, (float)player.getLookAngle().y);
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
    public static void teleportPlayer(ServerPlayerEntity player, World world, Vector3d pos, float pitch, float yaw) {
        player.teleportTo((ServerWorld) world, pos.get(Direction.Axis.X), pos.get(Direction.Axis.Y), pos.get(Direction.Axis.Z), pitch, yaw);
    }
}

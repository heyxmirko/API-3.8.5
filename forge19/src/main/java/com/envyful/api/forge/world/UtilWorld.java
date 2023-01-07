package com.envyful.api.forge.world;

import com.envyful.api.math.UtilRandom;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 *
 * Static utility class for world methods
 *
 */
public class UtilWorld {

    /**
     *
     * Gets a random position in the world
     * Returns the BlockPos selected
     *
     * @param world The name of the world
     * @param radius the x and z radius distance from 0
     * @return The BlockPos found to be valid/safe
     */
    public static BlockPos getRandomPosition(Level world, int radius) {
        return getRandomPosition(world, radius, radius);
    }

    /**
     *
     * Gets a random position in the world
     * Returns the BlockPos selected
     *
     * @param world The name of the world
     * @param radiusX the radius distance from 0
     * @param RadiusZ the radius distance from 0
     * @return The BlockPos found to be valid/safe
     */
    public static BlockPos getRandomPosition(Level world, int radiusX, int RadiusZ) {
        BlockPos pos = null;
        int y = -1;
        while (pos == null || y == -1) {
            pos = getRandomXAndZPosition(radiusX, RadiusZ);

            if (world.dimensionType().hasCeiling()) {
                y = getNetherYPosition(world, pos);
            } else {
                y = world.getChunk(pos).getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
            }
        }

        return new BlockPos(pos.getX(), y, pos.getZ());
    }

    private static BlockPos getRandomXAndZPosition(int radiusX, int radiusZ) {
        return new BlockPos(
                (UtilRandom.randomBoolean() ? 1 : -1) * UtilRandom.randomInteger(0, radiusX),
                0,
                (UtilRandom.randomBoolean() ? 1 : -1) * UtilRandom.randomInteger(0, radiusZ));
    }

    private static int getNetherYPosition(Level world, BlockPos pos) {
        for (int i = world.getHeight(); i > 5; i--) {
            BlockPos testPos = new BlockPos(pos.getX(), i, pos.getZ());
            if (!world.getBlockState(testPos).getBlock().equals(Blocks.BEDROCK) && world.getBlockState(testPos).getBlock().equals(Blocks.AIR)) {
                if (world.getBlockState(testPos.below(1)).getBlock().equals(Blocks.AIR)) {
                    BlockState groundState = world.getBlockState(testPos.below(2));
                    if (groundState.getMaterial().isSolid() && !groundState.getMaterial().isLiquid()) {
                        return testPos.getY() - 1;
                    }
                }
            }
        }

        return -1;
    }

    /**
     *
     * Finds a world represented by the given name.
     * Returns null if not found
     *
     * @param name The name of the world to be found
     * @return The world found
     */
    public static Level findWorld(String name) {
        for (ServerLevel world : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            if (getName(world).equalsIgnoreCase(name)) {
                return world;
            }
        }

        return null;
    }

    /**
     *
     * Obtains the name of the world and abstracts the impl away from the platform.
     *
     * @param world The world
     * @return The name of the world
     */
    public static String getName(Level world) {
        if (!(world instanceof ServerLevel) || !(world.getLevelData() instanceof ServerLevelData)) {
            return "NONE";
        }

        return ((ServerLevelData) world.getLevelData()).getLevelName();
    }
}

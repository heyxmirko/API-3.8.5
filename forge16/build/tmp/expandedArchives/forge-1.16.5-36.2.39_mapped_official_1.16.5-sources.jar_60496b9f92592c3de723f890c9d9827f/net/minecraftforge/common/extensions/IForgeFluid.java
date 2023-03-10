/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.extensions;

import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.tags.Tag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;

public interface IForgeFluid
{
    default Fluid getFluid()
    {
        return (Fluid) this;
    }

    /**
     * Called when the entity is inside this block, may be used to determined if the entity can breathing,
     * display material overlays, or if the entity can swim inside a block.
     *
     * @param world that is being tested.
     * @param pos position thats being tested.
     * @param entity that is being tested.
     * @param yToTest, primarily for testingHead, which sends the the eye level of the entity, other wise it sends a y that can be tested vs liquid height.
     * @param tag Fluid category
     * @param testingHead when true, its testing the entities head for vision, breathing ect... otherwise its testing the body, for swimming and movement adjustment.
     */
    default boolean isEntityInside(FluidState state, IWorldReader world, BlockPos pos, Entity entity, double yToTest, Tag<Fluid> tag, boolean testingHead)
    {
        return state.is(tag) && yToTest < (double)(pos.getY() + state.getHeight(world, pos) + 0.11111111F);
    }

    /**
     * Called when boats or fishing hooks are inside the block to check if they are inside
     * the material requested.
     *
     * @param world world that is being tested.
     * @param pos block thats being tested.
     * @param boundingBox box to test, generally the bounds of an entity that are besting tested.
     * @param materialIn to check for.
     * @return null for default behavior, true if the box is within the material, false if it was not.
     */
    @Nullable
    default Boolean isAABBInsideMaterial(FluidState state, IWorldReader world, BlockPos pos, AxisAlignedBB boundingBox, Material materialIn)
    {
        return null;
    }

    /**
     * Called when entities are moving to check if they are inside a liquid
     *
     * @param world world that is being tested.
     * @param pos block thats being tested.
     * @param boundingBox box to test, generally the bounds of an entity that are besting tested.
     * @return null for default behavior, true if the box is within the material, false if it was not.
     */
    @Nullable
    default Boolean isAABBInsideLiquid(FluidState state, IWorldReader world, BlockPos pos, AxisAlignedBB boundingBox)
    {
        return null;
    }

    /**
     * Location sensitive version of getExplosionResistance
     *
     * @param world The current world
     * @param pos Block position in world
     * @param explosion The explosion
     * @return The amount of the explosion absorbed.
     */
    @SuppressWarnings("deprecation")
    default float getExplosionResistance(FluidState state, IBlockReader world, BlockPos pos, Explosion explosion)
    {
        return state.getExplosionResistance();
    }

    /**
     * Queries if this fluid should render in a given layer.
     * A custom {@link IBakedModel} can use {@link net.minecraftforge.client.MinecraftForgeClient#getRenderLayer()} to alter the model based on layer.
     */
    /* TODO: reimplement
    default boolean canRenderInLayer(IFluidState state, BlockRenderLayer layer)
    {
        return this.getFluid().getRenderLayer() == layer;
    }*/

    /**
     * Retrieves a list of tags names this is known to be associated with.
     * This should be used in favor of TagCollection.getOwningTags, as this caches the result and automatically updates when the TagCollection changes.
     */
    Set<ResourceLocation> getTags();

    /**
     * Retrieves the non-vanilla fluid attributes, including localized name.
     */
    FluidAttributes getAttributes();
}

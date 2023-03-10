/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.model.ModelManager;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.registries.IRegistryDelegate;

/**
 * Wrapper around ItemModeMesher that cleans up the internal maps to respect ID remapping.
 */
public class ItemModelMesherForge extends ItemModelMesher
{
    final Map<IRegistryDelegate<Item>, ModelResourceLocation> locations = Maps.newHashMap();
    final Map<IRegistryDelegate<Item>, IBakedModel> models = Maps.newHashMap();

    public ItemModelMesherForge(ModelManager manager)
    {
        super(manager);
    }

    @Override
    @Nullable
    public IBakedModel getItemModel(Item item)
    {
        return models.get(item.delegate);
    }

    @Override
    public void register(Item item, ModelResourceLocation location)
    {
        IRegistryDelegate<Item> key = item.delegate;
        locations.put(key, location);
        models.put(key, getModelManager().getModel(location));
    }

    @Override
    public void rebuildCache()
    {
        final ModelManager manager = this.getModelManager();
        for (Map.Entry<IRegistryDelegate<Item>, ModelResourceLocation> e : locations.entrySet())
        {
        	models.put(e.getKey(), manager.getModel(e.getValue()));
        }
    }

    public ModelResourceLocation getLocation(@Nonnull ItemStack stack)
    {
        ModelResourceLocation location = locations.get(stack.getItem().delegate);

        if (location == null)
        {
            location = ModelBakery.MISSING_MODEL_LOCATION;
        }

        return location;
    }
}

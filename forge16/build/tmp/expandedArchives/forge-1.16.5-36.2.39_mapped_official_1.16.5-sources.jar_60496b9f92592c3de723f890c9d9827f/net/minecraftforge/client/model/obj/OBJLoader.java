/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.model.obj;

import com.google.common.collect.Maps;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModelLoader;

import javax.annotation.Nullable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class OBJLoader implements IModelLoader<OBJModel>
{
    public static OBJLoader INSTANCE = new OBJLoader();

    private final Map<OBJModel.ModelSettings, OBJModel> modelCache = Maps.newHashMap();
    private final Map<ResourceLocation, MaterialLibrary> materialCache = Maps.newHashMap();

    private IResourceManager manager = Minecraft.getInstance().getResourceManager();

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        modelCache.clear();
        materialCache.clear();
        manager = resourceManager;
    }

    @Override
    public OBJModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents)
    {
        if (!modelContents.has("model"))
            throw new RuntimeException("OBJ Loader requires a 'model' key that points to a valid .OBJ model.");

        String modelLocation = modelContents.get("model").getAsString();

        boolean detectCullableFaces = JSONUtils.getAsBoolean(modelContents, "detectCullableFaces", true);
        boolean diffuseLighting = JSONUtils.getAsBoolean(modelContents, "diffuseLighting", false);
        boolean flipV = JSONUtils.getAsBoolean(modelContents, "flip-v", false);
        boolean ambientToFullbright = JSONUtils.getAsBoolean(modelContents, "ambientToFullbright", true);
        @Nullable
        String materialLibraryOverrideLocation = modelContents.has("materialLibraryOverride") ? JSONUtils.getAsString(modelContents, "materialLibraryOverride") : null;

        return loadModel(new OBJModel.ModelSettings(new ResourceLocation(modelLocation), detectCullableFaces, diffuseLighting, flipV, ambientToFullbright, materialLibraryOverrideLocation));
    }

    public OBJModel loadModel(OBJModel.ModelSettings settings)
    {
        return modelCache.computeIfAbsent(settings, (data) -> {

            try(IResource resource = manager.getResource(settings.modelLocation);
                LineReader rdr = new LineReader(resource))
            {
                return new OBJModel(rdr, settings);
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("Could not find OBJ model", e);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Could not read OBJ model", e);
            }
        });
    }

    public MaterialLibrary loadMaterialLibrary(ResourceLocation materialLocation)
    {
        return materialCache.computeIfAbsent(materialLocation, (location) -> {
            try(IResource resource = manager.getResource(location);
                LineReader rdr = new LineReader(resource))
            {
                return new MaterialLibrary(rdr);
            }
            catch (FileNotFoundException e)
            {
                throw new RuntimeException("Could not find OBJ material library", e);
            }
            catch (Exception e)
            {
                throw new RuntimeException("Could not read OBJ material library", e);
            }
        });
    }
}

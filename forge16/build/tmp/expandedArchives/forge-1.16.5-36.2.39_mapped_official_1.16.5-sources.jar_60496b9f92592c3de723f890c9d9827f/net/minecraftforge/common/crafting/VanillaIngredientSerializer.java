/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.common.crafting;

import java.util.stream.Stream;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;

public class VanillaIngredientSerializer implements IIngredientSerializer<Ingredient>
{
    public static final VanillaIngredientSerializer INSTANCE  = new VanillaIngredientSerializer();

    public Ingredient parse(PacketBuffer buffer)
    {
        return Ingredient.fromValues(Stream.generate(() -> new Ingredient.SingleItemList(buffer.readItem())).limit(buffer.readVarInt()));
    }

    public Ingredient parse(JsonObject json)
    {
       return Ingredient.fromValues(Stream.of(Ingredient.valueFromJson(json)));
    }

    public void write(PacketBuffer buffer, Ingredient ingredient)
    {
        ItemStack[] items = ingredient.getItems();
        buffer.writeVarInt(items.length);

        for (ItemStack stack : items)
            buffer.writeItem(stack);
    }
}

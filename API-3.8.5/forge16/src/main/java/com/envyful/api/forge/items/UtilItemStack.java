package com.envyful.api.forge.items;

import com.google.common.collect.Lists;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;

import java.util.Collections;
import java.util.List;

/**
 *
 * Static utility class for generic {@link ItemStack} methods.
 *
 */
public class UtilItemStack {

    /**
     *
     * Returns the lore of the {@param itemStack} as a {@link List} of Strings.
     *
     * Will return {@link Collections#emptyList()} if the parameter is null
     *
     * @param itemStack The item to get the lore of
     * @return The lore of the item
     */
    public static List<String> getLore(ItemStack itemStack) {
        if (itemStack == null) {
            return Collections.emptyList();
        }

        List<String> lore = Lists.newArrayList();

        ListNBT currentLore = itemStack.getOrCreateTagElement("display").getList("Lore", 8);

        for (INBT nbtBase : currentLore) {
            if (nbtBase instanceof StringNBT) {
                lore.add(nbtBase.getAsString());
            }
        }

        return lore;
    }

    /**
     *
     * Returns the lore of the {@param itemStack} as a {@link List} of ITextComponent.
     *
     * Will return {@link Collections#emptyList()} if the parameter is null
     *
     * @param itemStack The item to get the lore of
     * @return The lore of the item
     */
    public static List<ITextComponent> getRealLore(ItemStack itemStack) {
        if (itemStack == null) {
            return Collections.emptyList();
        }

        List<ITextComponent> lore = Lists.newArrayList();

        ListNBT currentLore = itemStack.getOrCreateTagElement("display").getList("Lore", 8);

        for (INBT nbtBase : currentLore) {
            if (nbtBase instanceof StringNBT) {
                lore.add(ITextComponent.Serializer.fromJson(nbtBase.getAsString()));
            }
        }

        return lore;
    }

    /**
     *
     * Sets the lore of the specified itemstack to the given list
     *
     * @param itemStack The itemstack to update the lore of
     * @param lore The new lore
     */
    public static void setLore(ItemStack itemStack, List<String> lore) {
        CompoundNBT display = itemStack.getOrCreateTagElement("display");
        ListNBT newLore = new ListNBT();

        lore.forEach(s -> newLore.add(StringNBT.valueOf(s)));

        display.put("Lore", newLore);
        itemStack.addTagElement("display", display);
    }
}

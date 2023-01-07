package com.envyful.api.spigot.item;

import com.google.common.collect.Lists;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 *
 * Spigot {@link ItemStack} builder class.
 *
 */
public class ItemBuilder extends ItemStack {

    public ItemBuilder() {}

    public ItemBuilder type(Material type) {
        this.setType(type);
        return this;
    }

    public ItemBuilder amount(int amount) {
        this.setAmount(amount);
        return this;
    }

    public ItemBuilder name(Component name) {
        this.getItemMeta().displayName(name);
        return this;
    }

    public ItemBuilder lore(Component... lore) {
        this.lore(Lists.newArrayList(lore));
        return this;
    }

    public ItemBuilder addLore(Component... lore) {
        List<Component> lore1 = this.lore();
        lore1.addAll(Lists.newArrayList(lore));
        this.lore(lore1);
        return this;
    }

    public ItemBuilder unbreakable(boolean unbreakable) {
        this.getItemMeta().setUnbreakable(unbreakable);
        return this;
    }

    public ItemBuilder itemFlags(ItemFlag... itemFlags) {
        this.getItemMeta().addItemFlags(itemFlags);
        return this;
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.getItemMeta().addEnchant(enchantment, level, true);
        return this;
    }

    public ItemStack build() {
        return this;
    }
}

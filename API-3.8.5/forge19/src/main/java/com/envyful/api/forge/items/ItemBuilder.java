package com.envyful.api.forge.items;

import com.envyful.api.forge.chat.UtilChatColour;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * Forge {@link ItemStack} builder class. Allows specifying {@link ItemFlag}s, unbreakability, nbt data, enchants (etc).
 *
 */
public class ItemBuilder implements Cloneable {

    private Item type = Items.AIR;
    private int amount = 1;
    private Component name = (Component) Component.EMPTY;
    private boolean unbreakable = false;
    private List<Component> lore = Lists.newArrayList();
    private List<ItemFlag> itemFlags = Lists.newArrayList();
    private Map<String, Tag> nbtData = Maps.newHashMap();
    private Map<Enchantment, Integer> enchants = Maps.newHashMap();

    /**
     *
     * Basic constructor providing empty item builder with no specific values to begin with.
     *
     */
    public ItemBuilder() {}

    /**
     *
     * Converts {@link ItemStack} to builder
     *
     * @param itemStack The itemstack being converted
     */
    public ItemBuilder(ItemStack itemStack) {
        this.type = itemStack.getItem();
        this.amount = itemStack.getCount();
        this.name = itemStack.getDisplayName();
        this.lore = UtilItemStack.getRealLore(itemStack);

        if (itemStack.getTag() != null) {
            for (String s : itemStack.getTag().getAllKeys()) {
                this.nbtData.put(s, itemStack.getTag().get(s));
            }
        }
    }

    /**
     *
     * Sets the type of item that the itemstack will be
     *
     * @param type The minecraft type of the item
     * @return The builder
     */
    public ItemBuilder type(Item type) {
        this.type = type;
        return this;
    }

    /**
     *
     * Sets the amount of the item in the itemstack
     *
     * @param amount The amount
     * @return The builder
     */
    public ItemBuilder amount(int amount) {
        this.amount = amount;
        return this;
    }

    /**
     *
     * Sets the new name of the item
     *
     * @param name The new name
     * @return The builder
     */
    public ItemBuilder name(String name) {
        return this.name(UtilChatColour.colour(name));
    }

    /**
     *
     * Sets the new name of the item
     *
     * @param name The new name
     * @return The builder
     */
    public ItemBuilder name(Component name) {
        this.name = name;
        return this;
    }

    /**
     *
     * Sets the list of the strings as the stored lore (doesn't ADD to the lore)
     *
     * @param lore The new lore for the item
     * @return The builder
     */
    public ItemBuilder lore(List<Component> lore) {
        this.lore = lore;
        return this;
    }

    /**
     *
     * Sets the array of the strings as the stored lore (doesn't ADD to the lore)
     *
     * @param lore The new lore for the item
     * @return The builder
     */
    public ItemBuilder lore(String... lore) {
        this.lore = Arrays.stream(lore).map(Component::literal).collect(Collectors.toList());
        return this;
    }

    /**
     *
     * Sets the array of the ITextComponent as the stored lore (doesn't ADD to the lore)
     *
     * @param lore The new lore for the item
     * @return The builder
     */
    public ItemBuilder lore(Component... lore) {
        this.lore = Lists.newArrayList(lore);
        return this;
    }


    /**
     *
     * Adds the array of Strings to the stored lore (doesn't SET the lore)
     *
     * @param lore The lines to add to the lore
     * @return The builder
     */
    public ItemBuilder addLore(String... lore) {
        return this.addLore(Arrays.stream(lore).map(Component::literal).toArray(Component[]::new));
    }

    /**
     *
     * Adds the array of {@link Component} to the stored lore (doesn't SET the lore)
     *
     * @param lore The lines to add to the lore
     * @return The builder
     */
    public ItemBuilder addLore(Component... lore) {
        this.lore.addAll(Lists.newArrayList(lore));
        return this;
    }

    /**
     *
     * Sets the NBT base value at the key given
     *
     * @param key The key to set the value at
     * @param primitive The value to add under the given key
     * @return The builder
     */
    public ItemBuilder nbt(String key, Tag primitive) {
        this.nbtData.put(key, primitive);
        return this;
    }

    /**
     *
     * Sets if the item built should be breakable
     *
     * @param unbreakable true = unbreakable, false = breakable
     * @return The builder
     */
    public ItemBuilder unbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
        return this;
    }

    /**
     *
     * Adds the specified {@link ItemFlag} to the builder class
     *
     * @param itemFlag The item flag to add
     * @return The builder
     */
    public ItemBuilder itemFlag(ItemFlag itemFlag) {
        this.itemFlags.add(itemFlag);
        return this;
    }

    /**
     *
     * Adds the specified {@link ItemFlag}s to the builder class
     *
     * @param itemFlags The item flags to add
     * @return The builder
     */
    public ItemBuilder itemFlags(ItemFlag... itemFlags) {
        this.itemFlags.addAll(Arrays.asList(itemFlags));
        return this;
    }

    /**
     *
     * Enchants the item with the specified enchant and the given level.
     *
     * @param enchantment The enchantment to add to the builder
     * @param level The level of the enchant
     * @return The builder
     */
    public ItemBuilder enchant(Enchantment enchantment, int level) {
        this.enchants.put(enchantment, level);
        return this;
    }

    /**
     *
     * Gets the NBT tag for the specified key.
     *
     * Will return null if the value found at the key is not a tag compound.
     * However, will also insert a new tag compound (and return it) if it wasn't already there.
     *
     * @param key The key to be checked
     * @return The NBT tag at that key
     */
    public CompoundTag getCompound(String key) {
        Tag nbtBase = this.nbtData.computeIfAbsent(key, ___ -> new CompoundTag());

        if (!(nbtBase instanceof CompoundTag)) {
            return null;
        }

        return (CompoundTag) nbtBase;
    }

    /**
     *
     * Method to turn the {@link ItemBuilder} to a new forge {@link ItemStack} instance
     *
     * @return The forge item
     */
    public ItemStack build() {
        ItemStack itemStack = new ItemStack(this.type, this.amount);

        CompoundTag compound = itemStack.getOrCreateTag();

        for (Map.Entry<String, Tag> entry : nbtData.entrySet()) {
            compound.put(entry.getKey(), entry.getValue());
        }

        itemStack.setTag(compound);

        if (this.name != null) {
            CompoundTag display = itemStack.getOrCreateTagElement("display");
            Component name = this.name.copy().withStyle(style -> style.withItalic(false));
            display.put("Name", StringTag.valueOf(Component.Serializer.toJson(name)));
            itemStack.addTagElement("display", display);
        }

        if (this.lore != null && !this.lore.isEmpty()) {
            CompoundTag display = itemStack.getOrCreateTagElement("display");
            ListTag lore = new ListTag();

            this.lore.forEach(s -> lore.add(StringTag.valueOf(Component.Serializer.toJson(s.copy().withStyle(style -> style.withItalic(false))))));

            display.put("Lore", lore);
            itemStack.addTagElement("display", display);
        }

        if (this.unbreakable) {
            itemStack.getTag().putInt("Unbreakable", 1);
        }

        for (Enchantment enchantment : enchants.keySet()) {
            itemStack.enchant(enchantment, enchants.get(enchantment));
        }

        int hideFlags = 0;

        for (ItemFlag itemFlag : this.itemFlags) {
            hideFlags += itemFlag.getNbtId();
        }

        itemStack.getTag().putInt("HideFlags", hideFlags);

        return itemStack;
    }

    /**
     *
     * Method to create a copy of the ItemBuilder in a new instance
     *
     * @return The new item builder that the parameters have been copied to
     */
    @Override
    @SuppressWarnings("MethodDoesntCallSuperMethod")
    public ItemBuilder clone() {
        ItemBuilder copy = new ItemBuilder();
        copy.type(this.type);
        copy.name(this.name);
        copy.amount(this.amount);
        copy.lore(this.lore);
        return copy;
    }
}

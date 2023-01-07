package com.envyful.api.forge.config;

import com.envyful.api.config.type.ConfigItem;
import com.envyful.api.config.type.ExtendedConfigItem;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.items.ItemBuilder;
import com.envyful.api.forge.items.ItemFlag;
import com.envyful.api.forge.player.ForgeEnvyPlayer;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.gui.Transformer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.gui.item.Displayable;
import com.envyful.api.gui.pane.Pane;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.type.Pair;
import com.envyful.api.type.UtilParse;
import com.google.common.collect.Lists;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.Registry;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class UtilConfigItem {

    public static ConfigItemBuilder builder() {
        return new ConfigItemBuilder();
    }

    public static void addExtendedConfigItem(Pane pane, ForgeEnvyPlayer player, ExtendedConfigItem configItem, Transformer... transformers) {
        builder().extendedConfigItem(player, pane, configItem, transformers);
    }

    /**
     *
     * @deprecated Use {@link UtilConfigItem#builder()}
     */
    @Deprecated
    public static void addPermissibleConfigItem(Pane pane, ServerPlayer player, List<Transformer> transformers, ExtendedConfigItem configItem) {
        addPermissibleConfigItem(pane, player, configItem, transformers, null);
    }

    /**
     *
     * @deprecated Use {@link UtilConfigItem#builder()}
     */
    @Deprecated
    public static void addPermissibleConfigItem(Pane pane, ServerPlayer player, ExtendedConfigItem configItem,
                                                Transformer... transformers) {
        addPermissibleConfigItem(pane, player, configItem, Lists.newArrayList(transformers), null);
    }

    /**
     *
     * @deprecated Use {@link UtilConfigItem#builder()}
     */
    @Deprecated
    public static void addPermissibleConfigItem(Pane pane, ServerPlayer player, ExtendedConfigItem configItem,
                                                BiConsumer<EnvyPlayer<?>, Displayable.ClickType> clickHandler, Transformer... transformers) {
        addPermissibleConfigItem(pane, player, configItem, Lists.newArrayList(transformers), clickHandler);
    }

    /**
     *
     * @deprecated Use {@link UtilConfigItem#builder()}
     */
    @Deprecated
    public static void addPermissibleConfigItem(Pane pane, ServerPlayer player, ExtendedConfigItem configItem,
                                                List<Transformer> transformers,
                                                BiConsumer<EnvyPlayer<?>, Displayable.ClickType> clickHandler) {
        ItemStack itemStack = fromPermissibleItem(player, configItem, transformers);

        if (itemStack == null) {
            return;
        }

        for (Pair<Integer, Integer> position : configItem.getPositions()) {
            if (clickHandler == null) {
                pane.set(position.getX(), position.getY(), GuiFactory.displayable(itemStack));
            } else {
                pane.set(position.getX(), position.getY(), GuiFactory.displayableBuilder(itemStack)
                        .clickHandler(clickHandler).build());
            }
        }
    }

    /**
     *
     * @deprecated Use {@link UtilConfigItem#builder()}
     */
    @Deprecated
    public static void addConfigItem(Pane pane, ExtendedConfigItem configItem, Transformer... transformers) {
        addConfigItem(pane, configItem, Lists.newArrayList(transformers), null);
    }

    /**
     *
     * @deprecated Use {@link UtilConfigItem#builder()}
     */
    @Deprecated
    public static void addConfigItem(Pane pane, List<Transformer> transformers, ExtendedConfigItem configItem) {
        addConfigItem(pane, configItem, transformers,null);
    }

    /**
     *
     * @deprecated Use {@link UtilConfigItem#builder()}
     */
    @Deprecated
    public static void addConfigItem(Pane pane, ExtendedConfigItem configItem,
                                     BiConsumer<EnvyPlayer<?>, Displayable.ClickType> clickHandler, Transformer... transformers) {
        addConfigItem(pane, configItem, Lists.newArrayList(transformers), clickHandler);
    }

    /**
     *
     * @deprecated Use {@link UtilConfigItem#builder()}
     */
    @Deprecated
    public static void addConfigItem(Pane pane, ExtendedConfigItem configItem, List<Transformer> transformers,
                                     BiConsumer<EnvyPlayer<?>, Displayable.ClickType> clickHandler) {
        if (!configItem.isEnabled()) {
            return;
        }

        for (Pair<Integer, Integer> position : configItem.getPositions()) {
            if (clickHandler == null) {
                pane.set(position.getX(), position.getY(), GuiFactory.displayable(fromConfigItem(
                        configItem,
                        transformers
                )));
            } else {
                pane.set(position.getX(), position.getY(), GuiFactory.displayableBuilder(fromConfigItem(
                        configItem,
                        transformers
                )).clickHandler(clickHandler).build());
            }
        }
    }

    public static ItemStack fromPermissibleItem(ServerPlayer player, ExtendedConfigItem permissibleConfigItem, Transformer... transformers) {
        return fromPermissibleItem(player, permissibleConfigItem, Lists.newArrayList(transformers));
    }

    public static ItemStack fromPermissibleItem(ServerPlayer player, ExtendedConfigItem permissibleConfigItem, List<Transformer> transformers) {
        if (!permissibleConfigItem.isEnabled()) {
            return null;
        }

        if (!permissibleConfigItem.requiresPermission() || permissibleConfigItem.getPermission().isEmpty() ||
                UtilPlayer.hasPermission(player, permissibleConfigItem.getPermission())) {
            return fromConfigItem(permissibleConfigItem, transformers);
        }

        if (permissibleConfigItem.getElseItem() == null || !permissibleConfigItem.getElseItem().isEnabled()) {
            return null;
        }

        return fromConfigItem(permissibleConfigItem.getElseItem(), transformers);
    }

    public static ItemStack fromConfigItem(ExtendedConfigItem configItem, Transformer... transformers) {
        return fromConfigItem(configItem.asConfigItem(), Lists.newArrayList(transformers));
    }

    public static ItemStack fromConfigItem(ExtendedConfigItem configItem, List<Transformer> transformers) {
        return fromConfigItem(configItem.asConfigItem(), transformers);
    }

    public static ItemStack fromConfigItem(ConfigItem configItem, Transformer... transformers) {
        return fromConfigItem(configItem, Lists.newArrayList(transformers));
    }

    public static ItemStack fromConfigItem(ConfigItem configItem, List<Transformer> transformers) {
        if (!configItem.isEnabled()) {
            return null;
        }

        String name = configItem.getName();

        ItemBuilder itemBuilder = new ItemBuilder()
                .type(fromNameOrId(configItem.getType()))
                .amount(configItem.getAmount(transformers));

        List<String> lore = configItem.getLore();
        List<String> flags = configItem.getFlags();

        if (!transformers.isEmpty()) {
            for (Transformer transformer : transformers) {
                lore = transformer.transformLore(lore);
                name = transformer.transformName(name);
                flags = transformer.transformLore(flags);
            }
        }

        for (String flag : flags) {
            ItemFlag foundFlag = ItemFlag.valueOf(flag.toUpperCase());
            itemBuilder.itemFlag(foundFlag);
        }

        for (String s : lore) {
            itemBuilder.addLore(UtilChatColour.colour(s));
        }

        itemBuilder.name(UtilChatColour.colour(name));

        for (ConfigItem.EnchantData value : configItem.getEnchants().values()) {
            String enchantName = value.getEnchant();
            String level = value.getLevel();

            if (!transformers.isEmpty()) {
                for (Transformer transformer : transformers) {
                    enchantName = transformer.transformName(enchantName);
                    level = transformer.transformName(level);
                }
            }

            Enchantment enchantment = Registry.ENCHANTMENT.getOptional(new ResourceLocation(enchantName.toLowerCase())).orElse(null);
            int parsedLevel = UtilParse.parseInteger(level).orElse(1);

            if (enchantment == null) {
                continue;
            }

            itemBuilder.enchant(enchantment, parsedLevel);
        }

        for (Map.Entry<String, ConfigItem.NBTValue> nbtData : configItem.getNbt().entrySet()) {
            Pair<String, Tag> parsed = parseNBT(nbtData, transformers);

            if (parsed != null) {
                itemBuilder.nbt(parsed.getX(), parsed.getY());
            }
        }

        return itemBuilder.build();
    }

    public static Pair<String, Tag> parseNBT(Map.Entry<String, ConfigItem.NBTValue> nbtEntry, List<Transformer> transformers) {
        if (nbtEntry.getValue().getType().equalsIgnoreCase("nbt")) {
            CompoundTag compound = new CompoundTag();

            for (Map.Entry<String, ConfigItem.NBTValue> entry : nbtEntry.getValue().getSubData().entrySet()) {
                Pair<String, Tag> parsed = parseNBT(entry, transformers);

                if (parsed != null) {
                    compound.put(parsed.getX(), parsed.getY());
                }
            }

            return Pair.of(nbtEntry.getKey(), compound);
        }

        if (nbtEntry.getValue().getType().equalsIgnoreCase("list")) {
            ListTag list = new ListTag();

            for (Map.Entry<String, ConfigItem.NBTValue> nbtValue : nbtEntry.getValue().getSubData().entrySet()) {
                Pair<String, Tag> parsed = parseNBT(nbtValue, transformers);

                if (parsed != null) {
                    CompoundTag compound = new CompoundTag();
                    compound.put(parsed.getX(), parsed.getY());
                    list.add(compound);
                }
            }

            return Pair.of(nbtEntry.getKey(), list);
        }

        return Pair.of(nbtEntry.getKey(), parseBasic(nbtEntry.getValue(), transformers));
    }

    public static Tag parseBasic(ConfigItem.NBTValue value, List<Transformer> transformers) {
        String data = value.getData();

        if (!transformers.isEmpty()) {
            for (Transformer transformer : transformers) {
                data = transformer.transformName(data);
            }
        }

        Tag base;

        switch (value.getType().toLowerCase()) {
            case "int":
            case "integer":
                base = IntTag.valueOf(Integer.parseInt(data));
                break;
            case "long":
                base = LongTag.valueOf(Long.parseLong(data));
                break;
            case "byte":
                base = ByteTag.valueOf(Byte.parseByte(data));
                break;
            case "double":
                base = DoubleTag.valueOf(Double.parseDouble(data));
                break;
            case "float":
                base = FloatTag.valueOf(Float.parseFloat(data));
                break;
            case "short":
                base = ShortTag.valueOf(Short.parseShort(data));
                break;
            default:
            case "string":
                base = StringTag.valueOf(data);
                break;
        }

        return base;
    }

    public static Item fromNameOrId(String data) {
        try {
            Item item = Registry.ITEM.getOptional(new ResourceLocation(data)).orElse(null);

            if (item != null) {
                return item;
            }

            int integer = UtilParse.parseInteger(data).orElse(-1);

            if (integer == -1) {
                return null;
            }

            return Item.byId(integer);
        } catch (ResourceLocationException e) {
            return null;
        }
    }

}

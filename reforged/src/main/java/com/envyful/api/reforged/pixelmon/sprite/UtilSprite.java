package com.envyful.api.reforged.pixelmon.sprite;

import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.items.ItemBuilder;
import com.envyful.api.reforged.pixelmon.config.SpriteConfig;
import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.battles.attacks.Attack;
import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.ExtraStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.IVStore;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.StatsType;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.extraStats.MewStats;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumSpecial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class UtilSprite {

    private static final Map<EnumSpecies, String> ABILITIES = Maps.newHashMap();

    public static ItemStack getPokemonSprite(final Pokemon pokemon, final Function<Pokemon, List<String>> loreFunction) {
        ItemStack itemStack = getPixelmonSprite(pokemon);
        String nickname = pokemon.getNickname();
        itemStack.setStackDisplayName("§b" + pokemon.getSpecies().getLocalizedName() + ((nickname != null && !nickname.isEmpty()) ? " (" + nickname + ")" : "") + "");
        NBTTagCompound compound = itemStack.getOrCreateSubCompound("display");
        NBTTagList lore = new NBTTagList();

        for (String s : loreFunction.apply(pokemon)) {
            lore.appendTag(new NBTTagString(s));
        }

        compound.setTag("Lore", lore);
        itemStack.setTagInfo("display", compound);

        return itemStack;
    }

    public static ItemStack getStarterPixelmonSprite(EnumSpecies pokemon, String nameColour) {
        ItemStack itemStack = new ItemBuilder().type(PixelmonItems.itemPixelmonSprite)
                .name(nameColour + pokemon.getLocalizedName())
                .lore(
                        "§fGeneration: " + nameColour + pokemon.getGeneration(),
                        "§fType: " + nameColour + pokemon.getBaseStats().getType1().getLocalizedName(),
                        "§fAbilities: " + nameColour + getAbilities(pokemon)
                )
                .build();

        itemStack.setTagInfo("ndex", new NBTTagShort((short) pokemon.getNationalPokedexInteger()));
        return itemStack;
    }

    private static String getAbilities(EnumSpecies species) {
        if (ABILITIES.containsKey(species)) {
            return ABILITIES.get(species);
        }

        StringBuilder builder = new StringBuilder();
        String[] abilities = species.getBaseStats().abilities;

        for (int i = 0; i < 3; i++) {
            if (abilities.length <= i || abilities[i] == null) {
                continue;
            }

            builder.append(abilities[i]);

            if (i == 2) {
                builder.append(" (HA)");
            }

            builder.append(", ");
        }

        ABILITIES.put(species, builder.substring(0, builder.length() - 2));
        return builder.substring(0, builder.length() - 2);
    }

    public static ItemStack getPixelmonSprite(EnumSpecies pokemon) {
        ItemStack itemStack = new ItemStack(PixelmonItems.itemPixelmonSprite);
        itemStack.setStackDisplayName("§b" + pokemon.getLocalizedName());
        itemStack.setTagInfo("ndex", new NBTTagShort((short) pokemon.getNationalPokedexInteger()));
        return itemStack;
    }

    public static ItemStack getPokemonElement(Pokemon pokemon) {
        return getPokemonElement(pokemon, SpriteConfig.DEFAULT);
    }

    public static ItemStack getPokemonElement(Pokemon pokemon, SpriteConfig config) {
        ItemStack itemStack = getPixelmonSprite(pokemon);

        itemStack.setStackDisplayName(UtilChatColour.translateColourCodes(
                '&',
                config.getName()
                        .replace("%species_name%", pokemon.getSpecies().getLocalizedName())
                        .replace("%nickname%", (pokemon.getNickname() != null && !pokemon.getNickname().isEmpty() ?
                                " (" + pokemon.getNickname() + ")" : ""))
        ));

        NBTTagCompound compound = itemStack.getOrCreateSubCompound("display");
        NBTTagList lore = new NBTTagList();

        for (String s : getPokemonDesc(pokemon, config)) {
            lore.appendTag(new NBTTagString(s));
        }

        compound.setTag("Lore", lore);
        itemStack.setTagInfo("display", compound);

        return itemStack;
    }

    public static ItemStack getPixelmonSprite(Pokemon pokemon) {
        ItemStack itemStack = new ItemStack(PixelmonItems.itemPixelmonSprite);
        itemStack.setTagInfo("ndex", new NBTTagShort((short) pokemon.getSpecies().getNationalPokedexInteger()));
        itemStack.setTagInfo("form", new NBTTagByte((byte) pokemon.getForm()));
        itemStack.setTagInfo("gender", new NBTTagByte(pokemon.getGender().getForm()));
        itemStack.setTagInfo("Shiny", new NBTTagByte(pokemon.isShiny() ? (byte) 1 : (byte) 0));

        if (pokemon.getFormEnum() != EnumSpecial.Base) {
            itemStack.setTagInfo("specialTexture", new NBTTagByte(pokemon.getFormEnum().getForm()));
        }

        if (pokemon.getNickname() != null && !pokemon.getNickname().isEmpty()) {
            itemStack.setTagInfo("Nickname", new NBTTagString(pokemon.getNickname()));
        }

        return itemStack;
    }

    public static List<String> getPokemonDesc(Pokemon pokemon, SpriteConfig config) {
        List<String> lore = new ArrayList<>();

        IVStore iVs = pokemon.getIVs();
        float ivHP = iVs.get(StatsType.HP);
        float ivAtk = iVs.get(StatsType.Attack);
        float ivDef = iVs.get(StatsType.Defence);
        float ivSpeed = iVs.get(StatsType.Speed);
        float ivSAtk = iVs.get(StatsType.SpecialAttack);
        float ivSDef = iVs.get(StatsType.SpecialDefence);
        int percentage = Math.round(((ivHP + ivDef + ivAtk + ivSpeed + ivSAtk + ivSDef) / 186f) * 100);
        float evHP = pokemon.getEVs().get(StatsType.HP);
        float evAtk = pokemon.getEVs().get(StatsType.Attack);
        float evDef = pokemon.getEVs().get(StatsType.Defence);
        float evSpeed = pokemon.getEVs().get(StatsType.Speed);
        float evSAtk = pokemon.getEVs().get(StatsType.SpecialAttack);
        float evSDef = pokemon.getEVs().get(StatsType.SpecialDefence);
        ExtraStats extraStats = pokemon.getExtraStats();


        for (String line : config.getLore()) {
            String newLine =
                    line
                            .replace("%level%", pokemon.getLevel() + "")
                            .replace("%gender%", pokemon.getGender() == Gender.Male ? config.getMaleFormat() :
                                    pokemon.getGender() == Gender.None ? config.getNoneFormat() :
                                            config.getFemaleFormat())
                            .replace("%breedable%", pokemon.hasSpecFlag("unbreedable") ?
                                    config.getUnbreedableTrueFormat() : config.getUnbreedableFalseFormat())
                            .replace("%nature%", config.getNatureFormat()
                                    .replace("%nature_name%",
                                             pokemon.getMintNature() != null ?
                                                     pokemon.getBaseNature().getLocalizedName() :
                                                     pokemon.getNature().getLocalizedName())
                                    .replace("%mint_nature%", pokemon.getMintNature() != null ?
                                            config.getMintNatureFormat().replace("%mint_nature_name%", pokemon.getMintNature().getLocalizedName()) : ""))
                            .replace("%ability%", config.getAbilityFormat()
                                    .replace("%ability_name%", pokemon.getAbility().getLocalizedName())
                                    .replace("%ability_ha%", pokemon.getAbilitySlot() == 2 ? config.getHaFormat() : ""))
                            .replace("%friendship%", pokemon.getFriendship() + "")
                            .replace("%untradeable%", pokemon.hasSpecFlag("untradeable") ?
                                    config.getUntrdeableTrueFormat() : config.getUntradeableFalseFormat())
                            .replace("%iv_percentage%", percentage + "")
                            .replace("%iv_hp%", getColour(config, iVs, StatsType.HP) + ((int) ivHP) + "")
                            .replace("%iv_attack%", getColour(config, iVs, StatsType.Attack) + ((int) ivAtk) + "")
                            .replace("%iv_defence%", getColour(config, iVs, StatsType.Defence) + ((int) ivDef) + "")
                            .replace("%iv_spattack%", getColour(config, iVs, StatsType.SpecialAttack) + ((int) ivSAtk) + "")
                            .replace("%iv_spdefence%", getColour(config, iVs, StatsType.SpecialDefence) + ((int) ivSDef) + "")
                            .replace("%iv_speed%", getColour(config, iVs, StatsType.Speed) + ((int) ivSpeed) + "")
                            .replace("%ev_hp%", ((int) evHP) + "")
                            .replace("%ev_attack%", ((int) evAtk) + "")
                            .replace("%ev_defence%", ((int) evDef) + "")
                            .replace("%ev_spattack%", ((int) evSAtk) + "")
                            .replace("%ev_spdefence%", ((int) evSDef) + "")
                            .replace("%ev_speed%", ((int) evSpeed) + "")
                            .replace("%move_1%", getMove(pokemon, 0))
                            .replace("%move_2%", getMove(pokemon, 1))
                            .replace("%move_3%", getMove(pokemon, 2))
                            .replace("%move_4%", getMove(pokemon, 3))
                            .replace("%shiny%", pokemon.isShiny() ? config.getShinyTrueFormat() : config.getShinyFalseFormat())
                            .replace("%form%", pokemon.getFormEnum().getLocalizedName())
                            .replace("%size%", pokemon.getGrowth().getLocalizedName())
                            .replace("%custom_texture%", pokemon.getCustomTexture())
                    .replace("%friendship%", pokemon.getFriendship() + "");

            if (extraStats instanceof MewStats) {
                newLine = newLine.replace("%mew_cloned%", config.getMewClonedFormat()
                                .replace("%cloned%", ((MewStats) extraStats).numCloned + ""));
            } else {
                if (newLine.contains("%mew_cloned%")) {
                    continue;
                }
            }

            if (extraStats instanceof LakeTrioStats) {
                newLine = newLine.replace("%trio_gemmed%", config.getGemmedFormat()
                                .replace("%gemmed%", ((LakeTrioStats) extraStats).numEnchanted + ""));
            } else {
                if (newLine.contains("%trio_gemmed%")) {
                    continue;
                }
            }

            lore.add(UtilChatColour.translateColourCodes('&', newLine));
        }

        return lore;
    }

    private static String getColour(SpriteConfig config, IVStore ivStore, StatsType statsType) {
        if (ivStore.isHyperTrained(statsType)) {
            return config.getHyperIvColour();
        }

        return config.getNormalIvColour();
    }

    private static String getMove(Pokemon pokemon, int pos) {
        if (pokemon.getMoveset() == null) {
            return "";
        }

        if (pokemon.getMoveset().attacks.length <= pos) {
            return "";
        }

        if (pokemon.getMoveset().attacks[pos] == null) {
            return "";
        }

        return pokemon.getMoveset().attacks[pos].getActualMove().getLocalizedName();
    }
}

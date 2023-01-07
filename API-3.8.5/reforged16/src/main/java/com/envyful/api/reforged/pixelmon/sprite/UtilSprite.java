package com.envyful.api.reforged.pixelmon.sprite;

import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.reforged.pixelmon.config.SpriteConfig;
import com.pixelmonmod.api.Flags;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;
import com.pixelmonmod.pixelmon.api.pokemon.stats.BattleStatsType;
import com.pixelmonmod.pixelmon.api.pokemon.stats.ExtraStats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.IVStore;
import com.pixelmonmod.pixelmon.api.pokemon.stats.extraStats.LakeTrioStats;
import com.pixelmonmod.pixelmon.api.pokemon.stats.extraStats.MewStats;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;

import java.util.ArrayList;
import java.util.List;

public class UtilSprite {

    public static ItemStack getPokemonElement(Pokemon pokemon) {
        return getPokemonElement(pokemon, SpriteConfig.DEFAULT);
    }

    public static ItemStack getPokemonElement(Pokemon pokemon, SpriteConfig config) {
        ItemStack itemStack = getPixelmonSprite(pokemon);

        CompoundNBT compound = itemStack.getOrCreateTagElement("display");
        ListNBT lore = new ListNBT();

        for (ITextComponent s : getPokemonDesc(pokemon, config)) {
            if (s instanceof IFormattableTextComponent) {
                s = ((IFormattableTextComponent) s).setStyle(s.getStyle().withItalic(false));
            }

            lore.add(StringNBT.valueOf(ITextComponent.Serializer.toJson(s)));
        }

        ITextComponent colour = UtilChatColour.colour(replacePokemonPlaceholders(config.getName(), pokemon, config));
        colour = colour.copy().withStyle(colour.getStyle().withItalic(false));
        compound.putString("Name", ITextComponent.Serializer.toJson(colour));
        compound.put("Lore", lore);
        CompoundNBT tag = itemStack.getTag();

        tag.put("display", compound);
        itemStack.setTag(tag);

        return itemStack;
    }

    public static ItemStack getPixelmonSprite(Species pokemon) {
        ItemStack itemStack = new ItemStack(PixelmonItems.pixelmon_sprite);
        CompoundNBT tagCompound = new CompoundNBT();
        itemStack.setTag(tagCompound);
        tagCompound.putShort("ndex", (short)pokemon.getDex());
        tagCompound.putString("form", pokemon.getDefaultForm().getName());
        tagCompound.putByte("gender", (byte)Gender.MALE.ordinal());
        tagCompound.putString("palette", pokemon.getDefaultForm().getDefaultGenderProperties().getDefaultPalette().getName());

        return itemStack;
    }

    public static ItemStack getPixelmonSprite(Pokemon pokemon) {
        return SpriteItemHelper.getPhoto(pokemon);
    }

    public static List<ITextComponent> getPokemonDesc(Pokemon pokemon, SpriteConfig config) {
        List<ITextComponent> lore = new ArrayList<>();

        for (String line : config.getLore()) {
            line = replacePokemonPlaceholders(line, pokemon, config);

            if (line == null) {
                continue;
            }

            lore.add(UtilChatColour.colour(line));
        }

        return lore;
    }

    public static String replacePokemonPlaceholders(String line, Pokemon pokemon, SpriteConfig config) {
        IVStore iVs = pokemon.getIVs();
        float ivHP = iVs.getStat(BattleStatsType.HP);
        float ivAtk = iVs.getStat(BattleStatsType.ATTACK);
        float ivDef = iVs.getStat(BattleStatsType.DEFENSE);
        float ivSpeed = iVs.getStat(BattleStatsType.SPEED);
        float ivSAtk = iVs.getStat(BattleStatsType.SPECIAL_ATTACK);
        float ivSDef = iVs.getStat(BattleStatsType.SPECIAL_DEFENSE);
        int percentage = Math.round(((ivHP + ivDef + ivAtk + ivSpeed + ivSAtk + ivSDef) / 186f) * 100);
        float evHP = pokemon.getEVs().getStat(BattleStatsType.HP);
        float evAtk = pokemon.getEVs().getStat(BattleStatsType.ATTACK);
        float evDef = pokemon.getEVs().getStat(BattleStatsType.DEFENSE);
        float evSpeed = pokemon.getEVs().getStat(BattleStatsType.SPEED);
        float evSAtk = pokemon.getEVs().getStat(BattleStatsType.SPECIAL_ATTACK);
        float evSDef = pokemon.getEVs().getStat(BattleStatsType.SPECIAL_DEFENSE);
        ExtraStats extraStats = pokemon.getExtraStats();

        line = line
                .replace("%nickname%", pokemon.getDisplayName())
                .replace("%held_item%", pokemon.getHeldItem().getHoverName().getString())
                .replace("%palette%", pokemon.getPalette().getLocalizedName())
                .replace("%species_name%", pokemon.isEgg() ? "Egg" : pokemon.getSpecies().getLocalizedName())
                .replace("%level%", pokemon.getPokemonLevel() + "")
                        .replace("%gender%", pokemon.getGender() == Gender.MALE ? config.getMaleFormat() :
                                pokemon.getGender() == Gender.NONE ? config.getNoneFormat() :
                                        config.getFemaleFormat())
                        .replace("%breedable%", pokemon.hasFlag(Flags.UNBREEDABLE) ?
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
                                .replace("%ability_ha%", pokemon.hasHiddenAbility() ? config.getHaFormat() : ""))
                        .replace("%friendship%", pokemon.getFriendship() + "")
                        .replace("%untradeable%", pokemon.hasFlag("untradeable") ?
                                config.getUntrdeableTrueFormat() : config.getUntradeableFalseFormat())
                        .replace("%iv_percentage%", percentage + "")
                        .replace("%iv_hp%", getColour(config, iVs, BattleStatsType.HP) + ((int) ivHP) + "")
                        .replace("%iv_attack%", getColour(config, iVs, BattleStatsType.ATTACK) + ((int) ivAtk) + "")
                        .replace("%iv_defence%", getColour(config, iVs, BattleStatsType.DEFENSE) + ((int) ivDef) + "")
                        .replace("%iv_spattack%", getColour(config, iVs, BattleStatsType.SPECIAL_ATTACK) + ((int) ivSAtk) + "")
                        .replace("%iv_spdefence%", getColour(config, iVs, BattleStatsType.SPECIAL_DEFENSE) + ((int) ivSDef) + "")
                        .replace("%iv_speed%", getColour(config, iVs, BattleStatsType.SPEED) + ((int) ivSpeed) + "")
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
                        .replace("%form%", pokemon.getForm().getLocalizedName())
                        .replace("%size%", pokemon.getGrowth().getLocalizedName())
                        .replace("%friendship%", pokemon.getFriendship() + "");

        if (extraStats instanceof MewStats) {
            line = line.replace("%mew_cloned%", config.getMewClonedFormat()
                    .replace("%cloned%", ((MewStats) extraStats).numCloned + ""));
        } else {
            if (line.contains("%mew_cloned%") || line.contains("%cloned%")) {
                line = null;
            }
        }

        if (line != null) {
            if (extraStats instanceof LakeTrioStats) {
                line = line.replace("%trio_gemmed%", config.getGemmedFormat()
                        .replace("%gemmed%", ((LakeTrioStats)extraStats).numEnchanted + ""));
            } else {
                if (line.contains("%trio_gemmed%") || line.contains("%gemmed%")) {
                    line = null;
                }
            }
        }

        return line;
    }

    private static String getColour(SpriteConfig config, IVStore ivStore, BattleStatsType statsType) {
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

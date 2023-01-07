package com.envyful.api.reforged.pixelmon;

import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.spawning.SpawnInfo;
import com.pixelmonmod.pixelmon.api.spawning.SpawnSet;
import com.pixelmonmod.pixelmon.api.spawning.archetypes.entities.pokemon.SpawnInfoPokemon;
import com.pixelmonmod.pixelmon.api.util.helpers.BiomeHelper;
import com.pixelmonmod.pixelmon.api.world.WorldTime;
import com.pixelmonmod.pixelmon.spawning.PixelmonSpawning;
import net.minecraft.world.biome.Biome;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UtilPokemonInfo {

    public static List<String> getSpawnBiomes(Stats pokemon) {
        List<String> names = Lists.newArrayList();

        for (SpawnSet next : PixelmonSpawning.getAll().values().stream().flatMap(Collection::stream).collect(Collectors.toList())) {
            for (SpawnInfo spawnInfo : next.spawnInfos) {
                if (!(spawnInfo instanceof SpawnInfoPokemon)) {
                    continue;
                }

                SpawnInfoPokemon spawnInfoPokemon = (SpawnInfoPokemon)spawnInfo;

                if (!spawnInfoPokemon.getSpecies().equals(pokemon.getParentSpecies()) || spawnInfoPokemon.spawnSpecificBossRate != null) {
                    continue;
                }

                for (Biome biome : spawnInfoPokemon.condition.biomes) {
                    names.add(BiomeHelper.getLocalizedBiomeName(biome).getString());
                }
            }
        }

        return names;
    }

    public static List<String> getSpawnTimes(Stats pokemon) {
        List<String> names = Lists.newArrayList();

        for (SpawnSet next : PixelmonSpawning.getAll().values().stream().flatMap(Collection::stream).collect(Collectors.toList())) {
            for (SpawnInfo spawnInfo : next.spawnInfos) {
                if (!(spawnInfo instanceof SpawnInfoPokemon)) {
                    continue;
                }

                SpawnInfoPokemon spawnInfoPokemon = (SpawnInfoPokemon) spawnInfo;

                if (!spawnInfoPokemon.getSpecies().equals(pokemon.getParentSpecies()) || spawnInfoPokemon.condition.times == null) {
                    continue;
                }

                for (WorldTime time : spawnInfoPokemon.condition.times) {
                    if (time == null || names.contains(time.getLocalizedName())) {
                        continue;
                    }

                    names.add(time.getLocalizedName());
                }
            }
        }

        return names;
    }


    public static List<String> getCatchRate(Stats pokemon) {
        double males = pokemon.getMalePercentage();
        if (males == (double) -1) {
            return Collections.singletonList("§7Base rate: " + String.format("%.2f", pokemon.getCatchRate() / 255.0D * 100.0D) + "%");
        } else {
            return Lists.newArrayList(
                    "§b♂ Male: " + String.format("%.2f", pokemon.getMalePercentage()) + "%",
                    "§d♀ Female: " + String.format("%.2f", (100 - pokemon.getMalePercentage())) + "%"
            );
        }
    }
}

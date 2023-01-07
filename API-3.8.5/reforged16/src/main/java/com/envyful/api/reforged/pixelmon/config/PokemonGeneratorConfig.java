package com.envyful.api.reforged.pixelmon.config;

import com.google.common.collect.Sets;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@ConfigSerializable
public class PokemonGeneratorConfig {

    private transient Set<Species> blockedSpecies = null;
    private List<String> blockedTypes;
    private List<String> blockedTypeAndForm;
    private boolean speciesRequirement;
    private boolean allowLegends;
    private boolean allowUltraBeasts;
    private boolean genderRequirement;
    private boolean growthRequirement;
    private boolean natureRequirement;
    private int potentialGrowthRequirements;
    private int potentialNatureRequirements;
    private boolean allowEvolutions;
    private boolean ivRequirement;
    private boolean randomIVGeneration;
    private int minIVPercentage;
    private int maxIVPercentage;
    private boolean onlyLegends;

    public PokemonGeneratorConfig(Set<Species> blockedTypes, List<String> blockedTypeAndForm, boolean speciesRequirement, boolean allowLegends,
                                  boolean allowUltraBeasts, boolean genderRequirement, boolean growthRequirement,
                                  boolean natureRequirement, int potentialGrowthRequirements,
                                  int potentialNatureRequirements, boolean allowEvolutions, boolean ivRequirement,
                                  boolean randomIVGeneration, int minIVPercentage, int maxIVPercentage,
                                  boolean onlyLegends) {
        this.blockedSpecies = blockedTypes;
        this.blockedTypes = blockedTypes.stream().map(Species::getName).collect(Collectors.toList());
        this.blockedTypeAndForm = blockedTypeAndForm;
        this.speciesRequirement = speciesRequirement;
        this.allowLegends = allowLegends;
        this.allowUltraBeasts = allowUltraBeasts;
        this.genderRequirement = genderRequirement;
        this.growthRequirement = growthRequirement;
        this.natureRequirement = natureRequirement;
        this.potentialGrowthRequirements = potentialGrowthRequirements;
        this.potentialNatureRequirements = potentialNatureRequirements;
        this.allowEvolutions = allowEvolutions;
        this.ivRequirement = ivRequirement;
        this.randomIVGeneration = randomIVGeneration;
        this.minIVPercentage = minIVPercentage;
        this.maxIVPercentage = maxIVPercentage;
        this.onlyLegends = onlyLegends;
    }

    public PokemonGeneratorConfig() {
    }

    public Set<Species> getBlockedTypes() {
        if (this.blockedSpecies == null) {
            Set<Species> blockedSpecies = Sets.newHashSet();

            for (String blockedType : this.blockedTypes) {
                blockedSpecies.add(PixelmonSpecies.fromNameOrDex(blockedType).orElse(null));
            }

            this.blockedSpecies = blockedSpecies;
        }

        return this.blockedSpecies;
    }

    public List<String> getBlockedTypeAndForm() {
        return this.blockedTypeAndForm;
    }

    public boolean isSpeciesRequirement() {
        return this.speciesRequirement;
    }

    public boolean isAllowLegends() {
        return this.allowLegends;
    }

    public boolean isAllowUltraBeasts() {
        return this.allowUltraBeasts;
    }

    public boolean isGenderRequirement() {
        return this.genderRequirement;
    }

    public boolean isGrowthRequirement() {
        return this.growthRequirement;
    }

    public boolean isNatureRequirement() {
        return this.natureRequirement;
    }

    public int getPotentialGrowthRequirements() {
        return this.potentialGrowthRequirements;
    }

    public int getPotentialNatureRequirements() {
        return this.potentialNatureRequirements;
    }

    public boolean isAllowEvolutions() {
        return this.allowEvolutions;
    }

    public boolean isIvRequirement() {
        return this.ivRequirement;
    }

    public boolean isRandomIVGeneration() {
        return this.randomIVGeneration;
    }

    public int getMinIVPercentage() {
        return this.minIVPercentage;
    }

    public int getMaxIVPercentage() {
        return this.maxIVPercentage;
    }

    public boolean isOnlyLegends() {
        return this.onlyLegends;
    }
}

package com.envyful.api.reforged.pixelmon.config;

import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Set;

@ConfigSerializable
public class PokemonGeneratorConfig {

    private Set<EnumSpecies> blockedTypes;
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

    public PokemonGeneratorConfig(Set<EnumSpecies> blockedTypes, boolean speciesRequirement, boolean allowLegends,
                                  boolean allowUltraBeasts, boolean genderRequirement, boolean growthRequirement,
                                  boolean natureRequirement, int potentialGrowthRequirements,
                                  int potentialNatureRequirements, boolean allowEvolutions, boolean ivRequirement,
                                  boolean randomIVGeneration, int minIVPercentage, int maxIVPercentage,
                                  boolean onlyLegends) {
        this.blockedTypes = blockedTypes;
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

    public Set<EnumSpecies> getBlockedTypes() {
        return this.blockedTypes;
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

package com.envyful.api.reforged.pixelmon;

import com.envyful.api.math.UtilRandom;
import com.envyful.api.reforged.pixelmon.config.PokemonGeneratorConfig;
import com.envyful.api.type.requirement.impl.MinimumIntegerRequirement;
import com.envyful.api.type.requirement.impl.RandomMinimumIntegerRequirement;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import com.pixelmonmod.pixelmon.enums.EnumNature;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;

import java.util.List;
import java.util.Set;

/**
 *
 * A java object for generating pokemon with random specs
 *
 */
public class PokemonGenerator {

    private final Set<EnumSpecies> blockedTypes;
    private final boolean speciesRequirement;
    private final boolean allowLegends;
    private final boolean allowUltraBeasts;
    private final boolean genderRequirement;
    private final boolean growthRequirement;
    private final boolean natureRequirement;
    private final int potentialGrowthRequirements;
    private final int potentialNatureRequirements;
    private final boolean allowEvolutions;
    private final boolean ivRequirement;
    private final boolean randomIVGeneration;
    private final int minIVPercentage;
    private final int maxIVPercentage;
    private final boolean onlyLegends;

    private PokemonGenerator(Set<EnumSpecies> blockedTypes, boolean speciesRequirement, boolean allowLegends, boolean allowUltraBeasts, boolean genderRequirement,
                             boolean growthRequirement, boolean natureRequirement, int potentialGrowthRequirements, int potentialNatureRequirements,
                             boolean allowEvolutions, boolean ivRequirement, boolean randomIVGeneration,
                             int minIVPercentage, int maxIVPercentage, boolean onlyLegends) {
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

    public PokemonGenerator(PokemonGeneratorConfig config) {
        this(config.getBlockedTypes(), config.isSpeciesRequirement(), config.isAllowLegends(),
             config.isAllowUltraBeasts(), config.isGenderRequirement(), config.isGrowthRequirement(),
             config.isNatureRequirement(), config.getPotentialGrowthRequirements(),
             config.getPotentialNatureRequirements(), config.isAllowEvolutions(), config.isIvRequirement(),
             config.isRandomIVGeneration(), config.getMinIVPercentage(), config.getMaxIVPercentage(), config.isOnlyLegends()
        );
    }

    public PokemonSpec generate() {
        PokemonSpec.Builder builder = PokemonSpec.builder();

        if (this.speciesRequirement) {
            builder.species(this.getRandomSpecies());
            builder.allowEvolutions(this.allowEvolutions);
        }

        if (this.genderRequirement) {
            builder.gender(UtilRandom.getRandomElementExcluding(Gender.values(), Gender.None));
        }

        if (this.growthRequirement) {
            List<EnumGrowth> alreadyFound = Lists.newArrayList(EnumGrowth.Ginormous, EnumGrowth.Microscopic);
            for (int i = 0; i < this.potentialGrowthRequirements; i++) {
                EnumGrowth growth = UtilRandom.getRandomElementExcluding(EnumGrowth.values(), alreadyFound.toArray(new EnumGrowth[0]));
                builder.growth(growth);
                alreadyFound.add(growth);
            }
        }

        if (this.natureRequirement) {
            List<EnumNature> alreadyFound = Lists.newArrayList();
            for (int i = 0; i < this.potentialNatureRequirements; i++) {
                EnumNature nature = UtilRandom.getRandomElementExcluding(EnumNature.values(), alreadyFound.toArray(new EnumNature[0]));
                builder.nature(nature);
                alreadyFound.add(nature);
            }
        }

        if (this.ivRequirement) {
            if (this.randomIVGeneration) {
                builder.ivRequirement(new RandomMinimumIntegerRequirement(this.minIVPercentage, this.maxIVPercentage));
            } else {
                builder.ivRequirement(new MinimumIntegerRequirement(this.maxIVPercentage));
            }
        }

        return builder.build();
    }

    private EnumSpecies getRandomSpecies() {
        if (this.onlyLegends) {
            EnumSpecies species = UtilRandom.getRandomElement(EnumSpecies.legendaries.toArray(new EnumSpecies[0]));

            while (!this.isAllowedSpecies(species)) {
                species = UtilRandom.getRandomElement(EnumSpecies.legendaries.toArray(new EnumSpecies[0]));
            }

            return species;
        }

        EnumSpecies species = EnumSpecies.randomPoke(this.allowLegends);

        while (!this.isAllowedSpecies(species)) {
            species = EnumSpecies.randomPoke(this.allowLegends);
        }

        return species;
    }

    private boolean isAllowedSpecies(EnumSpecies species) {
        if (!this.allowUltraBeasts && species.isUltraBeast()) {
            return false;
        }

        if (!this.allowLegends && species.isLegendary()) {
            return false;
        }

        if (this.genderRequirement && species.getBaseStats().isGenderless()) {
            return false;
        }

        return !this.blockedTypes.contains(species);
    }

    public static PokemonGenerator.Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Set<EnumSpecies> blockedTypes = Sets.newHashSet();
        private boolean speciesRequirement = true;
        private boolean allowLegends = false;
        private boolean allowUltraBeasts = false;
        private boolean genderRequirement = true;
        private boolean growthRequirement = false;
        private boolean natureRequirement = false;
        private int potentialGrowthRequirements = -1;
        private int potentialNatureRequirements = -1;
        private boolean allowEvolutions = true;
        private boolean randomIVGeneration = false;
        private boolean ivRequirement = false;
        private int minIVPercentage = 0;
        private int maxIVPercentage = 100;
        private boolean legendsOnly = false;

        protected Builder() {}

        public Builder setAllowLegends(boolean allowLegends) {
            this.allowLegends = allowLegends;
            return this;
        }

        public Builder setAllowUltraBeasts(boolean allowUltraBeasts) {
            this.allowUltraBeasts = allowUltraBeasts;
            return this;
        }

        public Builder addBlockedType(EnumSpecies species) {
            this.blockedTypes.add(species);
            return this;
        }

        public Builder setSpeciesRequired(boolean speciesRequirement) {
            this.speciesRequirement = speciesRequirement;
            return this;
        }

        public Builder setGenderRequirement(boolean genderRequirement) {
            this.genderRequirement = genderRequirement;
            return this;
        }

        public Builder setGrowthRequirement(boolean growthRequirement) {
            this.growthRequirement = growthRequirement;
            return this;
        }

        public Builder setNatureRequirement(boolean natureRequirement) {
            this.natureRequirement = natureRequirement;
            return this;
        }

        public Builder setPotentialRequiredGrowths(int potentialGrowthRequirements) {
            this.potentialGrowthRequirements = potentialGrowthRequirements;
            return this;
        }

        public Builder setPotentialNatureRequirements(int potentialNatureRequirements) {
            this.potentialNatureRequirements = potentialNatureRequirements;
            return this;
        }

        public Builder setAllowEvolutions(boolean allowEvolutions) {
            this.allowEvolutions = allowEvolutions;
            return this;
        }

        public Builder setRandomIVGeneration(boolean randomIVGeneration) {
            this.randomIVGeneration = randomIVGeneration;
            return this;
        }

        public Builder setIVRequirement(boolean ivRequirement) {
            this.ivRequirement = ivRequirement;
            return this;
        }

        public Builder setMinimumIVPercentage(int minIVPercentage) {
            this.minIVPercentage = minIVPercentage;
            return this;
        }

        public Builder setMaximumIVPercentage(int maxIVPercentage) {
            this.maxIVPercentage = maxIVPercentage;
            return this;
        }

        public Builder setOnlyLegends(boolean legendsOnly) {
            this.legendsOnly = legendsOnly;
            return this;
        }

        public PokemonGenerator build() {
            return new PokemonGenerator(this.blockedTypes,
                    this.speciesRequirement,
                    this.allowLegends,
                    this.allowUltraBeasts,
                    this.genderRequirement,
                    this.growthRequirement,
                    this.natureRequirement,
                    this.potentialGrowthRequirements,
                    this.potentialNatureRequirements,
                    this.allowEvolutions,
                    this.ivRequirement,
                    this.randomIVGeneration,
                    this.minIVPercentage,
                    this.maxIVPercentage, this.legendsOnly);
        }
    }
}

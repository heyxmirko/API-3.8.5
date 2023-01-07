package com.envyful.api.reforged.pixelmon;

import com.envyful.api.forge.items.UtilItemStack;
import com.envyful.api.type.requirement.Requirement;
import com.google.common.collect.Lists;
import com.pixelmonmod.pixelmon.api.pokemon.Nature;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.gender.Gender;
import com.pixelmonmod.pixelmon.api.registries.PixelmonForms;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonPalettes;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.entities.pixelmon.PixelmonEntity;
import com.pixelmonmod.pixelmon.enums.EnumGrowth;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 *
 * POJO for handling pokemon details and then determining if a given entity matches the details in the specification.
 *
 */
public class PokemonSpec {

    protected final Species species;
    protected final String form;
    protected final String palette;
    protected final boolean allowEvolutions;
    protected final Gender gender;
    protected final Requirement<Integer> ivRequirement;
    protected final List<Nature> natures;
    protected final List<EnumGrowth> growths;

    protected PokemonSpec(Species species, String form, String palette, boolean allowEvolutions, Gender gender,
                          Requirement<Integer> ivRequirement,
                          List<Nature> natures, List<EnumGrowth> growths) {
        this.species = species;
        this.form = form;
        this.palette = palette;
        this.allowEvolutions = allowEvolutions;
        this.gender = gender;
        this.ivRequirement = ivRequirement;
        this.natures = natures;
        this.growths = growths;
    }

    /**
     *
     * Gets the name of the Pokemon represented by the {@link PokemonSpec}
     *
     * @return The name
     */
    public String getDisplayName() {
        return this.species.getLocalizedName();
    }

    /**
     *
     * Gets a photo of the species of the {@link PokemonSpec}
     *
     * @return The photo generated
     */
    public ItemStack getPhoto() {
        ItemStack itemStack = new ItemStack(PixelmonItems.pixelmon_sprite);
        CompoundNBT tagCompound = new CompoundNBT();
        itemStack.setTag(tagCompound);
        if (this.species == null) {
            tagCompound.putShort("ndex", (short)PixelmonSpecies.MISSINGNO.getValueUnsafe().getDex());
        } else {
            tagCompound.putShort("ndex", (short) this.species.getDex());
        }

        if (this.form == null) {
            tagCompound.putString("form", PixelmonForms.NONE);
        } else {
            tagCompound.putString("form", this.form);
        }

        if (this.palette == null) {
            tagCompound.putString("palette", PixelmonPalettes.NONE);
        } else {
            tagCompound.putString("palette", this.palette);
        }

        if (this.gender == null) {
            tagCompound.putByte("gender", (byte) Gender.MALE.ordinal());
        } else {
            tagCompound.putByte("gender", (byte) this.gender.ordinal());
        }

        return itemStack;
    }

    /**
     *
     * Gets the description of the {@link PokemonSpec}
     *
     * @return The description
     */
    public List<String> getDescription(String colour, String offColour) {
        List<String> desc = Lists.newArrayList(
                colour + "Species: " + offColour + this.species.getLocalizedName()
        );

        if (this.gender != null) {
            desc.add(colour + "Gender: "+ offColour + this.gender.getLocalizedName());
        }

        if (this.ivRequirement != null) {
            desc.add(colour + "IVs: "+ offColour + this.ivRequirement.get() + "%");
        }

        if (this.growths != null && !this.growths.isEmpty()) {
            desc.add(colour + "Growths: ");

            for (EnumGrowth growth : this.growths) {
                desc.add(colour + " • " + offColour + growth.getLocalizedName());
            }
        }

        if (this.natures != null && !this.natures.isEmpty()) {
            desc.add(colour + "Natures: ");

            for (Nature nature : this.natures) {
                desc.add(colour + " • " + offColour + nature.getLocalizedName());
            }
        }

        return desc;
    }

    /**
     *
     * Method used to determine if the entity passed matches this pokemon specification
     *
     * @param pixelmon The entity being checked
     * @return True if it matches - false otherwise
     */
    public boolean matches(PixelmonEntity pixelmon) {
        return this.matches(pixelmon.getPokemon());
    }

    /**
     *
     * Method used to determine if the pokemon passed matches this pokemon specification
     *
     * @param pokemon The pokemon being checked
     * @return True if it matches - false otherwise
     */
    public boolean matches(Pokemon pokemon) {
        if (!this.doesSpeciesMatch(pokemon)) {
            return false;
        }

        if (this.gender != null && !Objects.equals(this.gender, pokemon.getGender())) {
            return false;
        }

        if (!this.doesGrowthMatch(pokemon)) {
            return false;
        }

        if (!this.doesNatureMatch(pokemon)) {
            return false;
        }

        if (this.ivRequirement == null) {
            return true;
        }

        return this.ivRequirement.fits((int) pokemon.getIVs().getPercentage(1));
    }

    public boolean doesSpeciesMatch(Pokemon pokemon) {
        if (this.species == null) {
            return true;
        }

        if (this.allowEvolutions) {
            if (!Objects.equals(this.species, pokemon.getSpecies())) {
                for (Species preEvolution : pokemon.getForm().getPreEvolutions()) {
                    if (Objects.equals(pokemon.getSpecies(), preEvolution)) {
                        return true;
                    }
                }
            }
        }

        return Objects.equals(this.species, pokemon.getSpecies());
    }

    private boolean doesNatureMatch(Pokemon pokemon) {
        if (this.natures.isEmpty()) {
            return true;
        }

        return this.natures.contains(pokemon.getNature());
    }

    private boolean doesGrowthMatch(Pokemon pokemon) {
        if (this.growths.isEmpty()) {
            return true;
        }

        return this.growths.contains(pokemon.getGrowth());
    }

    @Override
    public String toString() {
        return "PokemonSpec{" +
                "species=" + species +
                ", allowEvolutions=" + allowEvolutions +
                ", gender=" + gender +
                ", natures=" + natures +
                ", growths=" + growths +
                ", ivRequirement=" + ivRequirement +
                '}';
    }

    /**
     *
     * Static factory method for getting the builder
     *
     * @return A new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Species species = null;
        private String form = null;
        private String palette = null;
        private boolean allowEvolutions = false;
        private Gender gender = null;
        private Requirement<Integer> ivRequirement = null;
        private List<Nature> natures = Lists.newArrayList();
        private List<EnumGrowth> growths = Lists.newArrayList();

        private Builder() {}

        public Builder species(Species species) {
            this.species = species;
            return this;
        }

        public Builder form(String form) {
            this.form = form;
            return this;
        }

        public Builder palette(String palette) {
            this.palette = palette;
            return this;
        }

        public Builder allowEvolutions(boolean allowEvolutions) {
            this.allowEvolutions = allowEvolutions;
            return this;
        }

        public Builder gender(Gender gender) {
            this.gender = gender;
            return this;
        }

        public Builder ivRequirement(Requirement<Integer> ivRequirement) {
            this.ivRequirement = ivRequirement;
            return this;
        }

        public Builder nature(Nature nature) {
            this.natures.add(nature);
            return this;
        }

        public Builder natures(Nature... nature) {
            this.natures.addAll(Arrays.asList(nature));
            return this;
        }

        public Builder growth(EnumGrowth growth) {
            this.growths.add(growth);
            return this;
        }

        public Builder growths(EnumGrowth... growths) {
            this.growths.addAll(Arrays.asList(growths));
            return this;
        }

        public PokemonSpec build() {
            return new PokemonSpec(this.species, this.form, this.palette, this.allowEvolutions, this.gender, this.ivRequirement, this.natures,
                    this.growths);
        }
    }

    public static class Display {

        protected final PokemonSpec spec;

        private List<String> description = null;

        public Display(PokemonSpec spec) {
            this.spec = spec;
        }

        public ItemStack getPhoto() {
            ItemStack itemStack = new ItemStack(PixelmonItems.pixelmon_sprite);
            CompoundNBT tagCompound = new CompoundNBT();

            itemStack.setTag(tagCompound);
            tagCompound.putShort("ndex", (short) this.spec.species.getDex());

            UtilItemStack.setLore(itemStack, this.getDescription());

            return itemStack;
        }

        private List<String> getDescription() {
            if (this.description == null) {
                this.description = Lists.newArrayList(
                        "",
                        "§fRequirements:"
                );

                if (this.spec.allowEvolutions) {
                    this.description.add("§7• Evolutions are allowed");
                } else {
                    this.description.add("§7• Evolutions are not allowed");
                }

                if (this.spec.gender != null) {
                    this.description.add("§7• " + this.spec.gender.name() + " Gender");
                }

                if (!this.spec.natures.isEmpty()) {
                    this.description.add("§7• Natures: ");

                    for (Nature nature : this.spec.natures) {
                        this.description.add("  §7• " + nature.getLocalizedName());
                    }
                }

                if (!this.spec.growths.isEmpty()) {
                    this.description.add("§7• Growths: ");

                    for (EnumGrowth growth : this.spec.growths) {
                        this.description.add("  §7• " + growth.name());
                    }
                }

                if (this.spec.ivRequirement != null) {
                    this.description.add("§7• IV Requirement: " + this.spec.ivRequirement.get() + "%");
                }
            }

            return description;
        }
    }
}

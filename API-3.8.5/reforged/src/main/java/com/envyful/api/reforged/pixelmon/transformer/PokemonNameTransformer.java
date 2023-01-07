package com.envyful.api.reforged.pixelmon.transformer;

import com.envyful.api.gui.Transformer;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;

public class PokemonNameTransformer implements Transformer {

    private final String name;

    public static PokemonNameTransformer of(Pokemon pokemon) {
        return of(pokemon.getSpecies());
    }

    public static PokemonNameTransformer of(EnumSpecies species) {
        return new PokemonNameTransformer(species.getLocalizedName());
    }

    private PokemonNameTransformer(String name) {this.name = name;}

    @Override
    public String transformName(String name) {
        return name.replace("%pokemon%", this.name + "");
    }
}

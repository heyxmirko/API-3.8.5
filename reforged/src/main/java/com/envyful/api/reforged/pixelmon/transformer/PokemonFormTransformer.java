package com.envyful.api.reforged.pixelmon.transformer;

import com.envyful.api.gui.Transformer;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

public class PokemonFormTransformer implements Transformer {

    private final String name;

    public static PokemonFormTransformer of(Pokemon pokemon) {
        return new PokemonFormTransformer(pokemon.getFormEnum().getLocalizedName());
    }

    private PokemonFormTransformer(String name) {this.name = name;}

    @Override
    public String transformName(String name) {
        return name.replace("%form%", this.name + "");
    }
}

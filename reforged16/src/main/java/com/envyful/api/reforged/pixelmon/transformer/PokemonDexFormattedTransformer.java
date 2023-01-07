package com.envyful.api.reforged.pixelmon.transformer;

import com.envyful.api.gui.Transformer;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;

public class PokemonDexFormattedTransformer implements Transformer {

    private final String ndex;

    public static PokemonDexFormattedTransformer of(Pokemon pokemon) {
        return of(pokemon.getSpecies());
    }

    public static PokemonDexFormattedTransformer of(Species species) {
        return new PokemonDexFormattedTransformer(species.getFormattedDex());
    }

    private PokemonDexFormattedTransformer(String ndex) {this.ndex = ndex;}

    @Override
    public String transformName(String name) {
        return name.replace("%pokedex_formatted%", this.ndex);
    }
}

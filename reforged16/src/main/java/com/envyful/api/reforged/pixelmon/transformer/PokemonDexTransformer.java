package com.envyful.api.reforged.pixelmon.transformer;

import com.envyful.api.gui.Transformer;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;

public class PokemonDexTransformer implements Transformer {

    private final int ndex;

    public static PokemonDexTransformer of(Pokemon pokemon) {
        return of(pokemon.getSpecies());
    }

    public static PokemonDexTransformer of(Species species) {
        return new PokemonDexTransformer(species.getDex());
    }

    private PokemonDexTransformer(int ndex) {this.ndex = ndex;}

    @Override
    public String transformName(String name) {
        return name.replace("%pokedex%", this.ndex + "");
    }
}

package com.envyful.api.reforged.pixelmon.transformer;

import com.envyful.api.gui.Transformer;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;

public class PokemonSpriteTransformer implements Transformer {

    private final String spritePath;

    public static PokemonSpriteTransformer of(Pokemon pokemon) {
        return new PokemonSpriteTransformer(pokemon.getPalette().getSprite().toString());
    }

    private PokemonSpriteTransformer(String spritePath) {this.spritePath = spritePath;}

    @Override
    public String transformName(String name) {
        return name.replace("%sprite%", this.spritePath);
    }
}

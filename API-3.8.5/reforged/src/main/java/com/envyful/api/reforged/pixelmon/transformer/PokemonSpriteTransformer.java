package com.envyful.api.reforged.pixelmon.transformer;

import com.envyful.api.gui.Transformer;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.client.gui.GuiResources;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;

public class PokemonSpriteTransformer implements Transformer {

    private final String spritePath;

    public static PokemonSpriteTransformer of(Pokemon pokemon) {
        return of(pokemon.getSpecies());
    }

    public static PokemonSpriteTransformer of(EnumSpecies species) {
        return new PokemonSpriteTransformer("pixelmon:textures/" + GuiResources.getSpritePath(species, -1, Gender.Male,
                                                                                            "", false) + ".png");
    }

    private PokemonSpriteTransformer(String spritePath) {this.spritePath = spritePath;}

    @Override
    public String transformName(String name) {
        return name.replace("%sprite%", this.spritePath);
    }
}

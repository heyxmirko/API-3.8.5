package com.envyful.api.reforged.pixelmon.sprite;

import com.pixelmonmod.pixelmon.config.PixelmonItems;
import com.pixelmonmod.pixelmon.entities.pixelmon.stats.Gender;
import com.pixelmonmod.pixelmon.enums.EnumSpecies;
import com.pixelmonmod.pixelmon.enums.forms.EnumSpecial;
import com.pixelmonmod.pixelmon.enums.forms.IEnumForm;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;

public class SpriteBuilder {

    private EnumSpecies species;
    private Gender gender = Gender.Male;
    private boolean shiny = false;

    private String nick = null;
    private IEnumForm form = null;

    public SpriteBuilder() {}

    public SpriteBuilder species(EnumSpecies species) {
        this.species = species;
        return this;
    }

    public SpriteBuilder gender(Gender gender) {
        this.gender = gender;
        return this;
    }

    public SpriteBuilder shiny(boolean shiny) {
        this.shiny = shiny;
        return this;
    }

    public SpriteBuilder nick(String nick) {
        this.nick = nick;
        return this;
    }

    public SpriteBuilder form(IEnumForm form) {
        this.form = form;
        return this;
    }

    public ItemStack build() {
        ItemStack itemStack = new ItemStack(PixelmonItems.itemPixelmonSprite);
        itemStack.setTagInfo("ndex", new NBTTagShort((short) this.species.getNationalPokedexInteger()));

        if (this.form != null) {
            itemStack.setTagInfo("form", new NBTTagByte((byte) this.form.getForm()));
        }

        itemStack.setTagInfo("gender", new NBTTagByte(this.gender.getForm()));
        itemStack.setTagInfo("Shiny", new NBTTagByte(this.shiny ? (byte) 1 : (byte) 0));

        if (this.form != EnumSpecial.Base) {
            itemStack.setTagInfo("specialTexture", new NBTTagByte(this.form.getForm()));
        }

        if (this.nick != null && !this.nick.isEmpty()) {
            itemStack.setTagInfo("Nickname", new NBTTagString(this.nick));
        }

        return itemStack;
    }
}

package com.envyful.api.forge.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ConfigSound {

    private String sound;
    private transient ResourceLocation cachedSound = null;
    private float volume;
    private float pitch;

    public ConfigSound(String sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    public ConfigSound() {
    }

    public void playSound(Player player) {
        if (this.cachedSound == null) {
            this.cachedSound = new ResourceLocation(this.sound);
        }

        player.playSound(new SoundEvent(this.cachedSound), this.volume, this.pitch);
    }
}

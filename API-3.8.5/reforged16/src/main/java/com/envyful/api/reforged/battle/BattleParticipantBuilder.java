package com.envyful.api.reforged.battle;

import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PartyStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StoragePosition;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.TrainerParticipant;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.awt.*;

public class BattleParticipantBuilder {

    private Entity player;

    private BattleParticipantBuilder() {}

    public BattleParticipantBuilder entity(Entity player) {
        this.player = player;
        return this;
    }

    public BattleParticipantBuilder team(Pokemon... tempTeam) {
        if (!(this.player instanceof ServerPlayerEntity)) {
            return this;
        }

        PlayerPartyStorage storage = StorageProxy.getParty(this.player.getUUID());

        if (storage == null) {
            return this;
        }

        storage.setInTemporaryMode(true, Color.RED, tempTeam);

        for (int i = 0; i < tempTeam.length; i++) {
            tempTeam[i].setStorage(storage, new StoragePosition(-1, i));
        }

        return this;
    }

    public BattleParticipant build() {
        PartyStorage storage = StorageProxy.getParty(this.player.getUUID());

        if (this.player instanceof ServerPlayerEntity) {
            return new PlayerParticipant((ServerPlayerEntity) this.player, storage.getAndSendOutFirstAblePokemon(this.player));
        }

        return new TrainerParticipant((NPCTrainer) this.player, null, 1);
    }

    public static BattleParticipantBuilder builder() {
        return new BattleParticipantBuilder();
    }
}

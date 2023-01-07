package com.envyful.api.reforged.battle;

import com.google.common.collect.Maps;
import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.api.enums.ExperienceGainType;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.ExperienceGainEvent;
import com.pixelmonmod.pixelmon.api.events.PixelmonFaintEvent;
import com.pixelmonmod.pixelmon.api.events.SpectateEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;
import com.pixelmonmod.pixelmon.battles.controller.participants.PixelmonWrapper;
import com.pixelmonmod.pixelmon.battles.controller.participants.PlayerParticipant;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class BattleBuilderFactory {

    private static Map<Integer, BattleBuilder> LISTENED_CONTROLLERS = Maps.newConcurrentMap();

    static {
        Pixelmon.EVENT_BUS.register(new BattleBuilderFactory());
    }

    public static void registerBattleBuilder(BattleController battleController, BattleBuilder battleBuilder) {
        LISTENED_CONTROLLERS.put(battleController.battleIndex, battleBuilder);
    }

    @SubscribeEvent
    public void onBattleEvent(BattleStartedEvent event) {
        BattleBuilder battleBuilder = LISTENED_CONTROLLERS.get(event.bc.battleIndex);

        if (battleBuilder == null || battleBuilder.startConsumer == null) {
            return;
        }

        battleBuilder.startConsumer.accept(event);
    }

    @SubscribeEvent
    public void onBattleEvent(BattleEndEvent event) {
        BattleBuilder battleBuilder = LISTENED_CONTROLLERS.get(event.getBattleController().battleIndex);

        if (battleBuilder == null) {
            return;
        }

        if (battleBuilder.endConsumer != null) {
            battleBuilder.endConsumer.accept(event);
        }

        LISTENED_CONTROLLERS.remove(event.getBattleController().battleIndex);

        for (BattleParticipant battleParticipant : event.getResults().keySet()) {
            if (!(battleParticipant instanceof PlayerParticipant)) {
                continue;
            }

            if (((PlayerParticipant) battleParticipant).getStorage().inTemporaryMode()) {
                ((PlayerParticipant)battleParticipant).getStorage().setInTemporaryMode(false, null);

                for (PixelmonWrapper pixelmonWrapper : battleParticipant.controlledPokemon) {
                    pixelmonWrapper.entity.kill();
                }
            }
        }
    }

    @SubscribeEvent
    public void onFaint(PixelmonFaintEvent.Pre event) {
        if (event.getPokemon() == null || !event.getPokemon().getPixelmonEntity().isPresent() ||
                event.getPokemon().getPixelmonEntity().get().battleController == null) {
            return;
        }

        BattleBuilder battleBuilder = LISTENED_CONTROLLERS.get(event.getPokemon().getPixelmonEntity().get().battleController.battleIndex);

        if (battleBuilder == null || battleBuilder.faintConsumer == null) {
            return;
        }

        battleBuilder.faintConsumer.accept(event);
    }

    @SubscribeEvent
    public void onExpGained(ExperienceGainEvent event) {
        if (event.getType() != ExperienceGainType.BATTLE) {
            return;
        }

        if (event.pokemon.getBattleController() == null) {
            return;
        }

        BattleBuilder battleBuilder = LISTENED_CONTROLLERS.get(event.pokemon.getBattleController().battleIndex);

        if (battleBuilder == null) {
            return;
        }

        if (battleBuilder.disableExp) {
            event.setCanceled(true);
            event.setExperience(0);
        }
    }


    @SubscribeEvent
    public void onSpectateAttempt(SpectateEvent.StartSpectate event) {
        BattleBuilder battleBuilder = LISTENED_CONTROLLERS.get(event.battleController.battleIndex);

        if (battleBuilder != null && battleBuilder.allowSpectators) {
            event.setCanceled(true);
        }
    }
}

package com.envyful.api.reforged.battle;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.forge.concurrency.UtilForgeConcurrency;
import com.pixelmonmod.pixelmon.api.events.BattleStartedEvent;
import com.pixelmonmod.pixelmon.api.events.PixelmonFaintEvent;
import com.pixelmonmod.pixelmon.api.events.battles.BattleEndEvent;
import com.pixelmonmod.pixelmon.battles.BattleRegistry;
import com.pixelmonmod.pixelmon.battles.api.rules.BattleRules;
import com.pixelmonmod.pixelmon.battles.api.rules.teamselection.TeamSelectionRegistry;
import com.pixelmonmod.pixelmon.battles.controller.BattleController;
import com.pixelmonmod.pixelmon.battles.controller.participants.BattleParticipant;

import java.util.function.Consumer;

public class BattleBuilder {

    protected BattleParticipant[] teamOne;
    protected BattleParticipant[] teamTwo;
    protected BattleRules rules;
    protected Consumer<BattleEndEvent> endConsumer;
    protected Consumer<BattleStartedEvent> startConsumer;
    protected Consumer<PixelmonFaintEvent.Pre> faintConsumer;
    protected boolean disableExp = false;
    protected boolean allowSpectators = true;
    protected boolean teamSelection = false;
    protected TeamSelectionRegistry.Builder selectionBuilder = null;
    protected long startDelayMillis = -1;
    protected boolean startSync = false;

    private BattleBuilder() {
    }

    public BattleBuilder teamOne(BattleParticipant... teamOne) {
        this.teamOne = teamOne;
        return this;
    }

    public BattleBuilder teamTwo(BattleParticipant... teamTwo) {
        this.teamTwo = teamTwo;
        return this;
    }

    public BattleBuilder rules(BattleRules rules) {
        this.rules = rules;
        return this;
    }

    public BattleBuilder endHandler(Consumer<BattleEndEvent> endConsumer) {
        this.endConsumer = endConsumer;
        return this;
    }

    public BattleBuilder startHandler(Consumer<BattleStartedEvent> startConsumer) {
        this.startConsumer = startConsumer;
        return this;
    }

    public BattleBuilder faintHandler(Consumer<PixelmonFaintEvent.Pre> faintConsumer) {
        this.faintConsumer = faintConsumer;
        return this;
    }

    public BattleBuilder disableExp() {
        return this.expEnabled(false);
    }

    public BattleBuilder expEnabled(boolean expEnabled) {
        this.disableExp = !expEnabled;
        return this;
    }

    public BattleBuilder allowSpectators() {
        return this.allowSpectators(true);
    }

    public BattleBuilder disallowSpectators() {
        return this.allowSpectators(false);
    }

    public BattleBuilder allowSpectators(boolean allowSpectators) {
        this.allowSpectators = allowSpectators;
        return this;
    }

    public BattleBuilder teamSelection() {
        return this.teamSelection(true);
    }

    public BattleBuilder teamSelection(boolean teamSelection) {
        this.teamSelection = teamSelection;

        if (this.teamSelection) {
            this.selectionBuilder = TeamSelectionRegistry.builder();
        }

        return this;
    }

    public BattleBuilder teamSelectionBuilder(TeamSelectionRegistry.Builder selectionBuilder) {
        this.selectionBuilder = selectionBuilder;
        return this;
    }

    public BattleBuilder startDelayTicks(long ticks) {
        this.startDelayMillis = 50 * ticks;
        return this;
    }

    public BattleBuilder startDelay(long startDelayMillis) {
        this.startDelayMillis = startDelayMillis;
        return this;
    }

    public BattleBuilder startSync() {
        this.startSync = true;
        return this;
    }

    public BattleBuilder startAsync() {
        this.startSync = false;
        return this;
    }

    public void start() {
        if (this.startDelayMillis != -1) {
            if (this.startSync) {
                UtilForgeConcurrency.runLater(this::startBattle, (int) (this.startDelayMillis / 50L));
            } else {
                UtilConcurrency.runLater(this::startBattle, this.startDelayMillis);
            }
            return;
        }

        this.startBattle();
    }

    protected void startBattle() {
        if (this.teamSelection) {
            this.selectionBuilder
                    .members(this.teamOne[0].getStorage(), this.teamTwo[0].getStorage())
                    .battleStartConsumer(battleController -> BattleBuilderFactory.registerBattleBuilder(battleController, this))
                    .start();
        } else {
            BattleController controller = BattleRegistry.startBattle(this.teamOne, this.teamTwo, this.rules);
            BattleBuilderFactory.registerBattleBuilder(controller, this);
        }
    }

    public static BattleBuilder builder() {
        return new BattleBuilder();
    }
}

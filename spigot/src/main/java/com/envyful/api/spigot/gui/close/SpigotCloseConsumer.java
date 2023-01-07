package com.envyful.api.spigot.gui.close;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.gui.close.CloseConsumer;
import com.envyful.api.gui.factory.GuiFactory;
import com.envyful.api.spigot.gui.factory.SpigotGuiFactory;
import com.envyful.api.spigot.player.SpigotEnvyPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class SpigotCloseConsumer implements CloseConsumer<SpigotEnvyPlayer, Player> {

    private final int delayTicks;
    private final Predicate<SpigotEnvyPlayer> predicate;
    private final Consumer<SpigotEnvyPlayer> handler;
    private final boolean async;

    public SpigotCloseConsumer(int delayTicks, Predicate<SpigotEnvyPlayer> predicate, Consumer<SpigotEnvyPlayer> handler, boolean async) {
        this.delayTicks = delayTicks;
        this.predicate = predicate;
        this.handler = handler;
        this.async = async;
    }

    @Override
    public void handle(SpigotEnvyPlayer spigotEnvyPlayer) {
        if (!this.predicate.test(spigotEnvyPlayer)) {
            return;
        }

        if (this.delayTicks <= 0) {
            if (this.async) {
                UtilConcurrency.runAsync(() -> this.handler.accept(spigotEnvyPlayer));
            } else {
                Bukkit.getScheduler().runTask(((SpigotGuiFactory)GuiFactory.getPlatformFactory()).getPlugin(),
                        () -> this.handler.accept(spigotEnvyPlayer));
            }
            return;
        }

        if (this.async) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(((SpigotGuiFactory)GuiFactory.getPlatformFactory()).getPlugin(),
                    () -> this.handler.accept(spigotEnvyPlayer), this.delayTicks);
        } else {
            Bukkit.getScheduler().runTaskLater(((SpigotGuiFactory)GuiFactory.getPlatformFactory()).getPlugin(),
                    () -> this.handler.accept(spigotEnvyPlayer), this.delayTicks);
        }
    }

    public static class Builder implements CloseConsumer.Builder<SpigotEnvyPlayer, Player> {

        private int delayTicks = 0;
        private Predicate<SpigotEnvyPlayer> predicate = spigotEnvyPlayer -> true;
        private Consumer<SpigotEnvyPlayer> handler = spigotEnvyPlayer -> {};
        private boolean async = true;

        public Builder() {}

        @Override
        public CloseConsumer.Builder<SpigotEnvyPlayer, Player> delayTicks(int delayTicks) {
            this.delayTicks = delayTicks;
            return this;
        }

        @Override
        public CloseConsumer.Builder<SpigotEnvyPlayer, Player> predicate(Predicate<SpigotEnvyPlayer> predicate) {
            this.predicate = predicate;
            return this;
        }

        @Override
        public CloseConsumer.Builder<SpigotEnvyPlayer, Player> handler(Consumer<SpigotEnvyPlayer> player) {
            this.handler = player;
            return this;
        }

        @Override
        public CloseConsumer.Builder<SpigotEnvyPlayer, Player> async(boolean async) {
            this.async = async;
            return this;
        }

        @Override
        public CloseConsumer<SpigotEnvyPlayer, Player> build() {
            return new SpigotCloseConsumer(this.delayTicks, this.predicate, this.handler, this.async);
        }
    }
}

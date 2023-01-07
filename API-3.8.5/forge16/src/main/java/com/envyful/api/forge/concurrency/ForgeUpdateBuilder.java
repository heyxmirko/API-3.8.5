package com.envyful.api.forge.concurrency;

import com.envyful.api.concurrency.UpdateBuilder;
import com.envyful.api.forge.listener.LazyListener;
import com.envyful.api.forge.player.util.UtilPlayer;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

/**
 *
 * Forge Implementation of the {@link UpdateBuilder} class
 *
 */
public class ForgeUpdateBuilder extends UpdateBuilder<ServerPlayerEntity> {

    private static final BiPredicate<ServerPlayerEntity, String> PERM_PREDICATE = (ServerPlayerEntity, s) -> UtilPlayer.hasPermission(ServerPlayerEntity, s);
    private static final BiConsumer<ServerPlayerEntity, String> MESSAGE_CONSUMER = (ServerPlayerEntity, s) -> ServerPlayerEntity.sendMessage(new StringTextComponent(s), Util.NIL_UUID);

    public static ForgeUpdateBuilder instance() {
        return new ForgeUpdateBuilder();
    }

    private ForgeUpdateBuilder() {}

    @Override
    public void start() {
        super.start();

        new JoinListener();
    }

    private final class JoinListener extends LazyListener {

        @SubscribeEvent
        public void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
            if (!ForgeUpdateBuilder.this.upToDate) {
                ForgeUpdateBuilder.this.attemptSendMessage((ServerPlayerEntity) event.getPlayer(), PERM_PREDICATE, MESSAGE_CONSUMER);
            }
        }
    }
}

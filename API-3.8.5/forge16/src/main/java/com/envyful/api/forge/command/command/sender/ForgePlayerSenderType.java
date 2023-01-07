package com.envyful.api.forge.command.command.sender;

import com.envyful.api.command.sender.SenderType;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;

public class ForgePlayerSenderType implements SenderType<ICommandSource, ServerPlayerEntity> {

    @Override
    public Class<?> getType() {
        return ServerPlayerEntity.class;
    }

    @Override
    public boolean isAccepted(ICommandSource sender) {
        return sender instanceof ServerPlayerEntity;
    }

    @Override
    public ServerPlayerEntity getInstance(ICommandSource sender) {
        return (ServerPlayerEntity)sender;
    }
}

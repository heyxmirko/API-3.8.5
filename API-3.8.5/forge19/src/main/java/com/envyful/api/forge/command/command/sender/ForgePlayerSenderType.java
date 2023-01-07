package com.envyful.api.forge.command.command.sender;

import com.envyful.api.command.sender.SenderType;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.level.ServerPlayer;

public class ForgePlayerSenderType implements SenderType<CommandSource, ServerPlayer> {

    @Override
    public Class<?> getType() {
        return ServerPlayer.class;
    }

    @Override
    public boolean isAccepted(CommandSource sender) {
        return sender instanceof ServerPlayer;
    }

    @Override
    public ServerPlayer getInstance(CommandSource sender) {
        return (ServerPlayer)sender;
    }
}

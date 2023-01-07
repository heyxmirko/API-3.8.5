package com.envyful.api.velocity.player.command.command.sender;

import com.envyful.api.command.sender.SenderType;
import com.velocitypowered.api.command.CommandSource;

public class ConsoleSenderType implements SenderType<CommandSource, CommandSource> {
    
    @Override
    public Class<?> getType() {
        return CommandSource.class;
    }

    @Override
    public boolean isAccepted(CommandSource sender) {
        return true;
    }

    @Override
    public CommandSource getInstance(CommandSource sender) {
        return sender;
    }
}

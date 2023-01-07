package com.envyful.api.forge.command.command.sender;

import com.envyful.api.command.sender.SenderType;
import net.minecraft.commands.CommandSource;

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

package com.envyful.api.forge.command.command.sender;

import com.envyful.api.command.sender.SenderType;
import net.minecraft.command.ICommandSource;

public class ConsoleSenderType implements SenderType<ICommandSource, ICommandSource> {
    
    @Override
    public Class<?> getType() {
        return ICommandSource.class;
    }

    @Override
    public boolean isAccepted(ICommandSource sender) {
        return true;
    }

    @Override
    public ICommandSource getInstance(ICommandSource sender) {
        return sender;
    }
}

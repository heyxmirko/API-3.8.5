package com.envyful.api.velocity.player.command.command.sender;

import com.envyful.api.command.sender.SenderType;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;

public class VelocityPlayerSenderType implements SenderType<CommandSource, Player> {

    @Override
    public Class<?> getType() {
        return Player.class;
    }

    @Override
    public boolean isAccepted(CommandSource sender) {
        return sender instanceof Player;
    }

    @Override
    public Player getInstance(CommandSource sender) {
        return (Player) sender;
    }
}

package com.envyful.api.forge.command.completion.player;

import com.envyful.api.command.injector.TabCompleter;
import com.google.common.collect.Lists;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.lang.annotation.Annotation;
import java.util.List;

public class PlayerTabCompleter implements TabCompleter<String, ServerPlayerEntity> {

    @Override
    public Class<ServerPlayerEntity> getSenderClass() {
        return ServerPlayerEntity.class;
    }

    @Override
    public Class<String> getCompletedClass() {
        return String.class;
    }

    @Override
    public List<String> getCompletions(ServerPlayerEntity sender, String[] currentData, Annotation... completionData) {
        List<String> playerNames = Lists.newArrayList();

        for (ServerPlayerEntity player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (completionData.length < 1 || completionData[0] instanceof ExcludeSelfCompletion) {
                if (player.getName().equals(sender.getName())) {
                    continue;
                }
            }

            playerNames.add(player.getName().getString());
        }

        return playerNames;
    }
}

package com.envyful.api.velocity.player.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import com.velocitypowered.api.proxy.Player;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class FillerTabCompleter implements TabCompleter<String, Player> {
    @Override
    public Class<Player> getSenderClass() {
        return Player.class;
    }

    @Override
    public Class<String> getCompletedClass() {
        return String.class;
    }

    @Override
    public List<String> getCompletions(Player sender, String[] currentData, Annotation... completionData) {
        return Collections.emptyList();
    }
}

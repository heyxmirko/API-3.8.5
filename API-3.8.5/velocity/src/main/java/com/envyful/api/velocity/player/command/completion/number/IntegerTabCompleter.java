package com.envyful.api.velocity.player.command.completion.number;

import com.envyful.api.command.injector.TabCompleter;
import com.google.common.collect.Lists;
import com.velocitypowered.api.proxy.Player;

import java.lang.annotation.Annotation;
import java.util.List;

public class IntegerTabCompleter implements TabCompleter<Integer, Player> {

    @Override
    public Class<Player> getSenderClass() {
        return Player.class;
    }

    @Override
    public Class<Integer> getCompletedClass() {
        return Integer.class;
    }

    @Override
    public List<String> getCompletions(Player sender, String[] currentData, Annotation... completionData) {
        if (completionData.length != 1 || !(completionData[0] instanceof IntCompletionData)) {
            List<String> completions = Lists.newArrayList();

            for (int i = 1; i <= 100; i++) {
                completions.add(i + "");
            }

            return completions;
        }

        List<String> completions = Lists.newArrayList();
        IntCompletionData data = (IntCompletionData) completionData[0];

        for (int i = data.min(); i <= data.max(); i++) {
            completions.add(i + "");
        }

        return completions;
    }
}

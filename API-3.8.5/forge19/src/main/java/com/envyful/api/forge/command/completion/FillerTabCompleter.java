package com.envyful.api.forge.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import net.minecraft.server.level.ServerPlayer;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class FillerTabCompleter implements TabCompleter<String, ServerPlayer> {
    @Override
    public Class<ServerPlayer> getSenderClass() {
        return ServerPlayer.class;
    }

    @Override
    public Class<String> getCompletedClass() {
        return String.class;
    }

    @Override
    public List<String> getCompletions(ServerPlayer sender, String[] currentData, Annotation... completionData) {
        return Collections.emptyList();
    }
}

package com.envyful.api.forge.command.completion;

import com.envyful.api.command.injector.TabCompleter;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

public class FillerTabCompleter implements TabCompleter<String, ServerPlayerEntity> {
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
        return Collections.emptyList();
    }
}

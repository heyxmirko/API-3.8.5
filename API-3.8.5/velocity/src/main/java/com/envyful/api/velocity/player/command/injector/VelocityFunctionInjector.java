package com.envyful.api.velocity.player.command.injector;

import com.envyful.api.command.injector.ArgumentInjector;
import com.velocitypowered.api.command.CommandSource;

import java.util.function.BiFunction;

/**
 *
 * The Velocity implementation of the {@link ArgumentInjector} interface
 *
 * @param <T> The type being converted to
 */
public class VelocityFunctionInjector<T> implements ArgumentInjector<T, CommandSource> {

    private final Class<T> superClass;
    private final boolean multipleArgs;
    private final BiFunction<CommandSource, String[], T> function;

    public VelocityFunctionInjector(Class<T> superClass, boolean multipleArgs, BiFunction<CommandSource, String[], T> function) {
        this.superClass = superClass;
        this.multipleArgs = multipleArgs;
        this.function = function;
    }

    @Override
    public Class<T> getConvertedClass() {
        return this.superClass;
    }

    @Override
    public boolean doesRequireMultipleArgs() {
        return this.multipleArgs;
    }

    @Override
    public T instantiateClass(CommandSource sender, String... args) {
        return this.function.apply(sender, args);
    }
}

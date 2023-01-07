package com.envyful.api.forge.command.command.executor;

import com.envyful.api.command.injector.ArgumentInjector;
import com.envyful.api.command.injector.TabCompleter;
import com.envyful.api.command.sender.SenderType;
import com.envyful.api.command.sender.SenderTypeFactory;
import com.envyful.api.concurrency.UtilLogger;
import com.envyful.api.forge.command.completion.FillerTabCompleter;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.envyful.api.type.Pair;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.apache.logging.log4j.Level;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 *
 * A simple data transfer object for the command methods obtained through reflection.
 * Ensures no laggy reflection during user use-age as the methods, object instances, and required injectors are cached at
 * server start for future use.
 *
 */
@SuppressWarnings("unchecked")
public class CommandExecutor {

    private final String identifier;
    private final int senderPosition;
    private final SenderType<?, ?> sender;
    private final Object commandClass;
    private final Method executor;
    private final boolean executeAsync;
    private final int justArgsPos;
    private final int requiredArgs;
    private final String requiredPermission;
    private final Pair<ArgumentInjector<?, ICommandSource>, String>[] arguments;
    private final List<TabCompleter<?, ?>> tabCompleters;
    private final List<Annotation[]> extraTabData;

    /**
     *
     * Simple constructor taking all required parameters
     *
     * @param identifier The identifier string for the sub-command
     * @param sender The sender type for the sub-command
     * @param senderPosition The position in the parameters of the sender
     * @param commandClass An instance of the command class
     * @param executor The method instance
     * @param executeAsync If the command should be run asynchronously
     * @param requiredPermission The permission required to execute the command
     * @param arguments The injected argument types
     */
    public CommandExecutor(String identifier, SenderType<?, ?> sender, int senderPosition, Object commandClass, Method executor,
                           boolean executeAsync, int justArgsPos, String requiredPermission,
                           Pair<ArgumentInjector<?, ICommandSource>, String>[] arguments,
                           List<TabCompleter<?, ?>> tabCompleters,
                           List<Annotation[]> extraTabData) {
        this.identifier = identifier;
        this.senderPosition = senderPosition;
        this.sender = sender;
        this.commandClass = commandClass;
        this.executor = executor;
        this.executeAsync = executeAsync;
        this.justArgsPos = justArgsPos;
        this.requiredPermission = requiredPermission;
        this.arguments = arguments;
        this.requiredArgs = this.calculateRequiredArgs();
        this.tabCompleters = tabCompleters;
        this.extraTabData = extraTabData;
    }

    /**
     *
     * Calculates the arguments required based on the injected arguments.
     * If there is a single multiple arg requirement then it will return -1.
     *
     * @return Number of args required for the sub command
     */
    private int calculateRequiredArgs() {
        if (this.justArgsPos != -1) {
            return -1;
        }

        int defaults = 0;

        for (Pair<ArgumentInjector<?, ICommandSource>, String> argument : this.arguments) {
            if (argument != null && argument.getX().doesRequireMultipleArgs()) {
                return -1;
            }

            if (argument != null && argument.getY() != null) {
                ++defaults;
            }
        }

        return this.arguments.length - defaults;
    }

    /**
     *
     * Gets the sub-commands identifying string
     *
     * @return The name of the sub command
     */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     *
     * Gets the sender type for the sub command
     *
     * @return The sender type
     */
    public SenderType<?, ?> getSender() {
        return this.sender;
    }

    /**
     *
     * Determines if the command can be executed asynchronously or not. Defaults as true
     *
     * @return If it should be run off thread
     */
    public boolean isExecutedAsync() {
        return this.executeAsync;
    }

    /**
     *
     * Gets the cached number of required arguments for this command
     *
     * @return The number of required args
     */
    public int getRequiredArgs() {
        return this.requiredArgs;
    }

    /**
     *
     * Determines if the command sender specified can execute this command (based on permissions)
     *
     * @param sender The entity attempting to run the command
     * @return If they can execute the command
     */
    public boolean canExecute(ICommandSource sender) {
        if (this.requiredPermission == null || this.requiredPermission.isEmpty()) {
            return true;
        }

        if (!(sender instanceof ServerPlayerEntity)) {
            return true;
        }

        return UtilPlayer.hasPermission((ServerPlayerEntity) sender, this.requiredPermission);
    }

    /**
     *
     * Attempts to execute the command with the specified sender, and arguments.
     *
     * Will return false if it fails to execute or an error occurs during runtime.
     *
     * @param sender The entity that executed the command
     * @param arguments The arguments that have been passed from the entity
     * @return If the command failed to run
     */
    public boolean execute(ICommandSource sender, String[] arguments) {
        Object[] args = new Object[this.arguments.length];
        int subtract = 0;
        int skipped = 0;

        for (int i = 0; i < this.arguments.length; i++) {
            Pair<ArgumentInjector<?, ICommandSource>, String> argument = this.arguments[i];

            if (argument == null) {
                args[i] = null;
                ++subtract;
                continue;
            }

            if (argument.getX().doesRequireMultipleArgs()) {
                String[] remainingArgs = Arrays.copyOfRange(arguments, i - subtract, arguments.length);

                if ((remainingArgs == null || remainingArgs.length == 0) && argument.getY() != null) {
                    remainingArgs = new String[] { argument.getY() };
                }

                args[i] = argument.getX().instantiateClass(sender, remainingArgs);

                if (args[i] == null) {
                    return false;
                }
            } else {
                if (arguments.length <= 0 || arguments.length <= (i - subtract) || (i - subtract) < 0) {
                    args[i] = argument.getX().instantiateClass(sender, argument.getY());

                    if (args[i] == null) {
                        return false;
                    } else {
                        ++subtract;
                        ++skipped;
                        continue;
                    }
                }

                args[i] = argument.getX().instantiateClass(sender, arguments[i - subtract]);

                if (args[i] == null) {
                    if (argument.getY() != null) {
                        args[i] = argument.getX().instantiateClass(sender, argument.getY());

                        if (args[i] == null) {
                            return false;
                        } else {
                            ++subtract;
                            ++skipped;
                        }
                    } else {
                        return false;
                    }
                }
            }
        }


        if (!((SenderType<ICommandSource, ?>) this.sender).isAccepted(sender)) {
            UtilLogger.getLogger().error("You cannot use this command from this source (player only).");
            return false;
        }

        args[this.senderPosition] = ((SenderType<ICommandSource, ?>) this.sender).getInstance(sender);

        if (this.justArgsPos != -1) {
            if (arguments == null) {
                arguments = new String[0];
            }

            args[this.justArgsPos] = Arrays.copyOfRange(arguments, this.arguments.length - 2 - skipped, arguments.length);
        }

        return this.execute(args);
    }

    private boolean execute(Object... args) {
        try {
            this.executor.invoke(this.commandClass, args);
            return true;
        } catch (Exception e) {
            UtilLogger.getLogger().catching(Level.FATAL, e);
        }

        return false;
    }

    public <A> List<String> tabComplete(ICommandSource sender, String[] args) {
        if (this.tabCompleters.isEmpty() || this.tabCompleters == null || this.requiredArgs == -1) {
            return Collections.emptyList();
        }

        int pos = Math.min(this.requiredArgs, Math.max(args.length - 1, 0));

        if (pos >= this.tabCompleters.size()) {
            return Collections.emptyList();
        }

        TabCompleter<?, A> completer = (TabCompleter<?, A>) this.tabCompleters.get(pos);

        if (completer instanceof FillerTabCompleter) {
            return Collections.emptyList();
        }

        return completer.getCompletions((A)
                        SenderTypeFactory.getSenderType(completer.getSenderClass()).get().getInstance(sender),
                args, this.extraTabData.get(pos));
    }
}

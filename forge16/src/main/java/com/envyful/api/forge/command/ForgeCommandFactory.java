package com.envyful.api.forge.command;

import com.envyful.api.command.CommandFactory;
import com.envyful.api.command.annotate.*;
import com.envyful.api.command.annotate.executor.*;
import com.envyful.api.command.exception.CommandLoadException;
import com.envyful.api.command.injector.ArgumentInjector;
import com.envyful.api.command.injector.TabCompleter;
import com.envyful.api.command.sender.SenderType;
import com.envyful.api.command.sender.SenderTypeFactory;
import com.envyful.api.forge.command.command.ForgeCommand;
import com.envyful.api.forge.command.command.executor.CommandExecutor;
import com.envyful.api.forge.command.command.sender.ConsoleSenderType;
import com.envyful.api.forge.command.command.sender.ForgePlayerSenderType;
import com.envyful.api.forge.command.completion.FillerTabCompleter;
import com.envyful.api.forge.command.completion.number.IntegerTabCompleter;
import com.envyful.api.forge.command.completion.player.PlayerTabCompleter;
import com.envyful.api.forge.command.injector.ForgeFunctionInjector;
import com.envyful.api.type.Pair;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Forge implementation of the {@link CommandFactory} interface
 *
 */
public class ForgeCommandFactory implements CommandFactory<CommandDispatcher<CommandSource>, ICommandSource> {

    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

    private final List<ArgumentInjector<?, ICommandSource>> registeredInjectors = Lists.newArrayList();
    private final Map<Class<?>, TabCompleter<?, ?>> registeredCompleters = Maps.newConcurrentMap();

    public ForgeCommandFactory() {
        SenderTypeFactory.register(new ConsoleSenderType(), new ForgePlayerSenderType());

        this.registerInjector(ServerPlayerEntity.class, (sender, args) -> ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByName(args[0]));
        this.registerInjector(int.class, (ICommandSource, args) -> {
            try {
                return Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        });
        this.registerInjector(String.class, (ICommandSource, args) -> args[0]);
        this.registerInjector(double.class, ((ICommandSource, args) -> {
            try {
                return Double.parseDouble(args[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }));
        this.registerInjector(long.class, ((ICommandSource, args) -> {
            try {
                return Long.parseLong(args[0]);
            } catch (NumberFormatException e) {
                return null;
            }
        }));
        this.registerCompleter(new IntegerTabCompleter());
        this.registerCompleter(new PlayerTabCompleter());
    }

    @Override
    public boolean registerCommand(CommandDispatcher<CommandSource> dispatcher, Object o) throws CommandLoadException {
        ForgeCommand command = this.createCommand(o.getClass(), o);

        LiteralCommandNode<CommandSource> args = dispatcher.register(Commands.literal(command.getName())
                .requires(commandSource -> command.checkPermission(commandSource.getServer(), commandSource.getEntity()))
                .then(Commands.argument("", StringArgumentType.greedyString())
                        .suggests((context, builder) -> this.buildSuggestions(command, context, builder))
                        .executes(context -> this.handleExecution(command, context)))
                .executes(context -> this.handleExecution(command, context)));

        for (String alias : command.getAliases()) {
            dispatcher.getRoot().addChild(buildRedirect(alias, args));
        }

        return true;
    }

    /**
     * Returns a literal node that redirects its execution to
     * the given destination node.
     *
     * @param alias the command alias
     * @param destination the destination node
     * @return the built node
     */
    public static LiteralCommandNode<CommandSource> buildRedirect(
            final String alias, final LiteralCommandNode<CommandSource> destination) {
        // Redirects only work for nodes with children, but break the top argument-less command.
        // Manually adding the root command after setting the redirect doesn't fix it.
        // See https://github.com/Mojang/brigadier/issues/46). Manually clone the node instead.
        LiteralArgumentBuilder<CommandSource> builder = LiteralArgumentBuilder
                .<CommandSource>literal(alias.toLowerCase(Locale.ENGLISH))
                .requires(destination.getRequirement())
                .forward(
                        destination.getRedirect(), destination.getRedirectModifier(), destination.isFork())
                .executes(destination.getCommand());
        for (CommandNode<CommandSource> child : destination.getChildren()) {
            builder.then(child);
        }
        return builder.build();
    }

    private int handleExecution(ForgeCommand command, CommandContext<CommandSource> context) {
        ICommandSource source = context.getSource().getEntity();

        if (source == null) {
            source = context.getSource().getServer();
        }

        try {
            command.execute(context.getSource().getServer(), source, context.getArgument("", String.class).split(" "));
        } catch (IllegalArgumentException e) {
            command.execute(context.getSource().getServer(), source, new String[0]);
        }
        return 1;
    }

    private CompletableFuture<Suggestions> buildSuggestions(ForgeCommand command, CommandContext<CommandSource> context, SuggestionsBuilder builder) {
        List<String> tabCompletions;
        String[] initialArgs = context.getInput().split(" ");
        List<String> args = Lists.newArrayList(Arrays.copyOfRange(initialArgs, 1, initialArgs.length));
        int spaces = 0;
        Matcher matcher = SPACE_PATTERN.matcher(context.getInput());

        while (matcher.find()) {
            spaces++;
        }

        while (spaces > args.size()) {
            args.add(" ");
            spaces--;
        }

        tabCompletions = command.getTabCompletions(context.getSource().getServer(),
                context.getSource().getEntity(),
                args.toArray(new String[0]),
                new BlockPos(context.getSource().getPosition()));

        if (args.size() > 0 && !args.get(args.size() - 1).trim().isEmpty()) {
            builder = builder.createOffset(context.getInput().length() - args.get(args.size() - 1).length());
        } else {
            builder = builder.createOffset(context.getInput().length());
        }

        for (String tabCompletion : tabCompletions) {
            if (args.isEmpty()) {
                builder.suggest(tabCompletion);
                continue;
            }

            String currentWord = args.get(args.size() - 1);

            if (currentWord.isEmpty() || currentWord.equals(" ")) {
                builder.suggest(tabCompletion);
                continue;
            }

            if (!tabCompletion.toLowerCase().startsWith(currentWord.toLowerCase())) {
                continue;
            }

            builder.suggest(tabCompletion);
        }

        return builder.buildFuture();
    }

    private ForgeCommand createCommand(Class<?> clazz) throws CommandLoadException {
        return this.createCommand(clazz, null);
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    private ForgeCommand createCommand(Class<?> clazz, Object instance) throws CommandLoadException {
        List<ForgeCommand> subCommands = this.getSubCommands(clazz);
        Command commandData = clazz.getAnnotation(Command.class);

        if (commandData == null) {
            throw new CommandLoadException(clazz.getSimpleName(), "missing @Command annotation on class!");
        }

        String defaultPermission = this.getDefaultPermission(clazz);

        if (instance == null) {
            instance = this.createInstance(clazz);

            if (instance == null) {
                throw new CommandLoadException(clazz.getSimpleName(), "cannot instantiate sub-command as there's no public constructor");
            }
        }

        List<CommandExecutor> subExecutors = Lists.newArrayList();
        BiFunction<ICommandSource, String[], List<String>> tabCompleter = null;

        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            CommandProcessor processorData = declaredMethod.getAnnotation(CommandProcessor.class);

            if (processorData == null) {
                TabCompletions tabCompletions = declaredMethod.getAnnotation(TabCompletions.class);

                if (tabCompletions != null) {
                    if (declaredMethod.getReturnType().equals(List.class) && declaredMethod.getParameterCount() == 2) {
                        Object finalInstance = instance;
                        tabCompleter = (sender, args) -> {
                            try {
                                return (List<String>) declaredMethod.invoke(finalInstance, sender, args);
                            } catch (IllegalAccessException | InvocationTargetException e) {
                                e.printStackTrace();
                            }

                            return Collections.emptyList();
                        };
                    }

                    continue;
                }

                continue;
            }

            String requiredPermission = this.getPermission(declaredMethod);
            List<Pair<ArgumentInjector<?, ICommandSource>, String>> arguments = Lists.newArrayList();
            Class<?>[] parameterTypes = declaredMethod.getParameterTypes();
            Annotation[][] annotations = declaredMethod.getParameterAnnotations();
            SenderType<?, ?> senderType = null;
            int senderPosition = -1;
            int justArgsPos = -1;
            List<TabCompleter<?, ?>> tabCompleters = Lists.newArrayList();
            List<Annotation[]> extraTabData = Lists.newArrayList();

            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == String[].class) {
                    arguments.add(null);
                    justArgsPos = i;
                    continue;
                }

                if (annotations.length <= i) {
                    continue;
                }

                if (annotations[i][0] instanceof Sender) {
                    senderType = SenderTypeFactory.getSenderType(parameterTypes[i]).orElse(null);

                    if (senderType == null) {
                        throw new RuntimeException("Unregistered sender type " + parameterTypes[i].getSimpleName());
                    }

                    senderPosition = i;
                    arguments.add(null);
                } else {
                    if (annotations[i][0] instanceof Completable) {
                        tabCompleters.add(this.registeredCompleters.get(((Completable) annotations[i][0]).value()));
                        List<Annotation> data = Lists.newArrayList();

                        for (int x = 1; x < annotations[i].length; x++) {
                            data.add(annotations[i][x]);
                        }

                        extraTabData.add(data.toArray(new Annotation[0]));
                    } else {
                        tabCompleters.add(new FillerTabCompleter());
                        try {
                            extraTabData.add(new Annotation[] { TypeFactory.annotation(
                                    Filler.class,
                                    Maps.newHashMap()
                            ) });
                        } catch (AnnotationFormatException e) {
                            e.printStackTrace();
                        }
                    }

                    Annotation argument = annotations[i][annotations[i].length - 1];
                    String defaultValue = null;

                    if (argument instanceof Argument) {
                        defaultValue = ((Argument) argument).defaultValue();

                        if (defaultValue.isEmpty()) {
                            defaultValue = null;
                        }
                    }

                    arguments.add(Pair.of(this.getInjectorFor(parameterTypes[i]), defaultValue));
                }
            }

            if (senderType == null) {
                throw new CommandLoadException(clazz.getSimpleName(), "Command must have a sender!");
            }

            subExecutors.add(new CommandExecutor(processorData.value(), senderType, senderPosition, instance, declaredMethod,
                    processorData.executeAsync(), justArgsPos, requiredPermission,
                    arguments.toArray(new Pair[0]), tabCompleters, extraTabData));
        }

        Child child = clazz.getAnnotation(Child.class);

        return new ForgeCommand(this, commandData.value(), child != null, commandData.description(), defaultPermission,
                                Arrays.asList(commandData.aliases()), subExecutors, subCommands, tabCompleter
        );
    }

    private String getPermission(Method method) {
        Permissible permissible = method.getAnnotation(Permissible.class);

        if (permissible == null) {
            return "";
        }

        return permissible.value();
    }

    private Object createInstance(Class<?> clazz) {
        if (clazz.getConstructors().length == 0) {
            try {
                return clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                e.printStackTrace();
                return null;
            }
        }

        for (Constructor<?> constructor : clazz.getConstructors()) {
            List<Object> objects = Lists.newArrayList();

            for (Class<?> parameterType : constructor.getParameterTypes()) {
                Object o = this.getInjectorFor(parameterType).instantiateClass(null);

                if (o == null) {
                    break;
                }

                objects.add(o);
            }

            if (objects.size() != constructor.getParameterTypes().length) {
                continue;
            }

            try {
                return constructor.newInstance(objects.toArray(new Object[0]));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private String getDefaultPermission(Class<?> clazz) {
        Permissible permissible = clazz.getAnnotation(Permissible.class);

        if (permissible == null) {
            return "";
        }

        return permissible.value();
    }

    private List<ForgeCommand> getSubCommands(Class<?> clazz) {
        SubCommands subCommands = clazz.getAnnotation(SubCommands.class);

        if (subCommands == null) {
            return Collections.emptyList();
        }

        List<ForgeCommand> commands = Lists.newArrayList();

        for (Class<?> subClazz : subCommands.value()) {
            commands.add(this.createCommand(subClazz));
        }

        return commands;
    }

    private ArgumentInjector<?, ICommandSource> getInjectorFor(Class<?> clazz) {
        for (ArgumentInjector<?, ICommandSource> registeredInjector : this.registeredInjectors) {
            if (registeredInjector.getConvertedClass().isAssignableFrom(clazz)) {
                return registeredInjector;
            }
        }

        return null;
    }

    @Override
    public boolean unregisterCommand(CommandDispatcher<CommandSource> server, Object o) {
        return false;
    }

    @Override
    public void registerInjector(Class<?> parentClass, boolean multipleArgs, BiFunction<ICommandSource, String[], ?> function) {
        this.registeredInjectors.add(new ForgeFunctionInjector(parentClass, multipleArgs, function));
    }

    @Override
    public void unregisterInjector(Class<?> parentClass) {
        this.registeredInjectors.removeIf(next -> Objects.equals(parentClass, next.getConvertedClass()));
    }

    @Override
    public void registerCompleter(TabCompleter<?, ?> tabCompleter) {
        this.registeredCompleters.put(tabCompleter.getClass(), tabCompleter);
    }
}

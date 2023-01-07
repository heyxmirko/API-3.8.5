package com.envyful.api.velocity.player.command;

import com.envyful.api.command.CommandFactory;
import com.envyful.api.command.annotate.*;
import com.envyful.api.command.annotate.executor.*;
import com.envyful.api.command.exception.CommandLoadException;
import com.envyful.api.command.injector.ArgumentInjector;
import com.envyful.api.command.injector.TabCompleter;
import com.envyful.api.command.sender.SenderType;
import com.envyful.api.command.sender.SenderTypeFactory;
import com.envyful.api.type.Pair;
import com.envyful.api.velocity.player.command.command.VelocityCommand;
import com.envyful.api.velocity.player.command.command.executor.CommandExecutor;
import com.envyful.api.velocity.player.command.command.sender.ConsoleSenderType;
import com.envyful.api.velocity.player.command.command.sender.VelocityPlayerSenderType;
import com.envyful.api.velocity.player.command.completion.FillerTabCompleter;
import com.envyful.api.velocity.player.command.completion.number.IntegerTabCompleter;
import com.envyful.api.velocity.player.command.completion.player.PlayerTabCompleter;
import com.envyful.api.velocity.player.command.injector.VelocityFunctionInjector;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import io.leangen.geantyref.AnnotationFormatException;
import io.leangen.geantyref.TypeFactory;

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
public class VelocityCommandFactory implements CommandFactory<CommandManager, CommandSource> {

    private static final Pattern SPACE_PATTERN = Pattern.compile("\\s");

    private final List<ArgumentInjector<?, CommandSource>> registeredInjectors = Lists.newArrayList();
    private final Map<Class<?>, TabCompleter<?, ?>> registeredCompleters = Maps.newConcurrentMap();

    private ProxyServer proxy;

    public VelocityCommandFactory(ProxyServer proxy) {
        this.proxy = proxy;

        SenderTypeFactory.register(new ConsoleSenderType(), new VelocityPlayerSenderType());

        this.registerInjector(Player.class, (sender, args) -> this.proxy.getPlayer(args[0]));
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
        this.registerCompleter(new PlayerTabCompleter(this.proxy));
    }

    public ProxyServer getProxy() {
        return this.proxy;
    }

    @Override
    public boolean registerCommand(CommandManager dispatcher, Object o) throws CommandLoadException {
        VelocityCommand command = this.createCommand(o.getClass(), o);

        BrigadierCommand brigadierCommand = new BrigadierCommand(LiteralArgumentBuilder.<CommandSource>literal(command.getName())
                .requires(command::checkPermission)
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("", StringArgumentType.greedyString())
                        .suggests((context, builder) -> this.buildSuggestions(command, context, builder))
                        .executes(context -> this.handleExecution(command, context)))
                .executes(context -> this.handleExecution(command, context)));

        dispatcher.register(brigadierCommand);

        for (String alias : command.getAliases()) {
            dispatcher.register(alias, brigadierCommand);
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

    private int handleExecution(VelocityCommand command, CommandContext<CommandSource> context) {
        try {
            CommandSource source = context.getSource();

            if (source == null) {
                source = context.getSource();
            }

            command.execute(context.getSource(), context.getArgument("", String.class).split(" "));
        } catch (IllegalArgumentException | CommandSyntaxException e) {

        }
        return 1;
    }

    private CompletableFuture<Suggestions> buildSuggestions(VelocityCommand command, CommandContext<CommandSource> context, SuggestionsBuilder builder) {
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

        tabCompletions = command.getTabCompletions(context.getSource(),
                args.toArray(new String[0]));

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

    private VelocityCommand createCommand(Class<?> clazz) throws CommandLoadException {
        return this.createCommand(clazz, null);
    }

    @SuppressWarnings("SuspiciousToArrayCall")
    private VelocityCommand createCommand(Class<?> clazz, Object instance) throws CommandLoadException {
        List<VelocityCommand> subCommands = this.getSubCommands(clazz);
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
        BiFunction<CommandSource, String[], List<String>> tabCompleter = null;

        for (Method declaredMethod : clazz.getDeclaredMethods()) {
            CommandProcessor processorData = declaredMethod.getAnnotation(CommandProcessor.class);

            if (processorData == null) {
                TabCompletions tabCompletions = declaredMethod.getAnnotation(TabCompletions.class);

                if (tabCompletions != null) {
                    if (declaredMethod.getReturnType().equals(List.class) && declaredMethod.getParameterCount() == 3) {
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
            List<Pair<ArgumentInjector<?, CommandSource>, String>> arguments = Lists.newArrayList();
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

        return new VelocityCommand(this, commandData.value(), child != null, commandData.description(), defaultPermission,
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

    private List<VelocityCommand> getSubCommands(Class<?> clazz) {
        SubCommands subCommands = clazz.getAnnotation(SubCommands.class);

        if (subCommands == null) {
            return Collections.emptyList();
        }

        List<VelocityCommand> commands = Lists.newArrayList();

        for (Class<?> subClazz : subCommands.value()) {
            commands.add(this.createCommand(subClazz));
        }

        return commands;
    }

    private ArgumentInjector<?, CommandSource> getInjectorFor(Class<?> clazz) {
        for (ArgumentInjector<?, CommandSource> registeredInjector : this.registeredInjectors) {
            if (registeredInjector.getConvertedClass().isAssignableFrom(clazz)) {
                return registeredInjector;
            }
        }

        return null;
    }

    @Override
    public boolean unregisterCommand(CommandManager server, Object o) {
        return false;
    }

    @Override
    public void registerInjector(Class<?> parentClass, boolean multipleArgs, BiFunction<CommandSource, String[], ?> function) {
        this.registeredInjectors.add(new VelocityFunctionInjector(parentClass, multipleArgs, function));
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

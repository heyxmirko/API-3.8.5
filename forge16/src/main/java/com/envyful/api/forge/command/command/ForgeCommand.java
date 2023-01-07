package com.envyful.api.forge.command.command;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.forge.command.ForgeCommandFactory;
import com.envyful.api.forge.command.command.executor.CommandExecutor;
import com.envyful.api.forge.concurrency.UtilForgeConcurrency;
import com.envyful.api.forge.player.util.UtilPlayer;
import com.google.common.collect.Lists;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSource;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 *
 * The forge command implementation for registering to the server. Handles checking all sub commands
 * and running all commands from the forge implementation.
 *
 */
@MethodsReturnNonnullByDefault
public class ForgeCommand {

    private static final ITextComponent NO_PERMISSION = new StringTextComponent("§c§l(!) §cNo permission!");

    private final ForgeCommandFactory commandFactory;
    private final String name;
    private final boolean child;
    private final List<TextComponent> description;
    private final String basePermission;
    private final List<String> aliases;
    private final List<CommandExecutor> executors;
    private final List<ForgeCommand> subCommands;
    private final BiFunction<ICommandSource, String[], List<String>> tabCompleter;

    public ForgeCommand(ForgeCommandFactory commandFactory, String name, boolean child, String[] description, String basePermission,
                        List<String> aliases, List<CommandExecutor> executors, List<ForgeCommand> subCommands,
                        BiFunction<ICommandSource, String[], List<String>> tabCompleter) {
        this.commandFactory = commandFactory;
        this.name = name;
        this.child = child;
        this.description = this.initializeDescription(description);
        this.basePermission = basePermission;
        this.aliases = aliases;
        this.executors = executors;
        this.subCommands = subCommands;
        this.tabCompleter = tabCompleter;
    }

    private List<TextComponent> initializeDescription(String[] description) {
        List<TextComponent> newDescription = Lists.newArrayList();

        for (String s : description) {
            newDescription.add(new StringTextComponent(s));
        }

        if (!this.child) {
            newDescription.add(new StringTextComponent(""));
            StringTextComponent textComponent = new StringTextComponent("§eFor further support visit the §nEnvyWare Ltd§e discord: ");
            textComponent.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.envyware.co.uk"));
            StringTextComponent textComponent2 = new StringTextComponent("§e§nhttps://discord.envyware.co.uk§e");
            textComponent2.getStyle().withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.envyware.co.uk"));
            newDescription.add(textComponent);
            newDescription.add(textComponent2);
        }

        return newDescription;
    }

    public String getName() {
        return this.name;
    }

    public List<String> getAliases() {
        return this.aliases;
    }

    @ParametersAreNonnullByDefault
    public boolean checkPermission(MinecraftServer server, ICommandSource sender) {
        if (this.basePermission.isEmpty() || !(sender instanceof ServerPlayerEntity)) {
            return true;
        }

        return UtilPlayer.hasPermission((ServerPlayerEntity) sender, this.basePermission);
    }

    @ParametersAreNonnullByDefault
    public void execute(MinecraftServer server, ICommandSource sender, String[] args) throws CommandException {
        UtilConcurrency.runAsync(() -> this.executeSync(server, sender, args));
    }

    public void executeSync(MinecraftServer server, ICommandSource sender, String[] args) {
        if (!this.checkPermission(server, sender)) {
            sender.sendMessage(NO_PERMISSION, Util.NIL_UUID);
            return;
        }

        if (args.length > 0) {
            for (ForgeCommand subCommand : this.subCommands) {
                if (this.fitsCommand(args[0], subCommand)) {
                    subCommand.executeSync(server, sender, Arrays.copyOfRange(args, 1, args.length));
                    return;
                }
            }
        }

        for (CommandExecutor executor : this.executors) {
            if (executor.getIdentifier().isEmpty()) {
                if (this.attemptRun(executor, sender, args)) {
                    return;
                }
            }

            if (args.length == 0) {
                continue;
            }

            if (!executor.getIdentifier().equalsIgnoreCase(args[0]) || (executor.getIdentifier().isEmpty() && !args[0].isEmpty())) {
                continue;
            }

            if (this.attemptRun(executor, sender, args)) {
                return;
            }
        }

        for (TextComponent textComponent : this.description) {
            sender.sendMessage(textComponent, Util.NIL_UUID);
        }
    }

    private boolean attemptRun(CommandExecutor executor, ICommandSource sender, String[] args) {
        if (!executor.canExecute(sender)) {
            sender.sendMessage(NO_PERMISSION, Util.NIL_UUID);
            return true;
        }

        if (executor.getRequiredArgs() == -1 || executor.getRequiredArgs() == 0) {
            if (!executor.isExecutedAsync()) {
                UtilForgeConcurrency.runSync(() -> executor.execute(sender, args));
                return true;
            }

            return executor.execute(sender, args);
        }

        if (executor.getRequiredArgs() <= (args.length + 1)) {
            if (!executor.isExecutedAsync()) {
                UtilForgeConcurrency.runSync(() -> {
                    if (executor.execute(sender, args)) {
                        return;
                    }

                    for (TextComponent textComponent : this.description) {
                        sender.sendMessage(textComponent, Util.NIL_UUID);
                    }
                });
                return true;
            }

            return executor.execute(sender, args);
        }

        return false;
    }

    private boolean fitsCommand(String arg, ForgeCommand subCommand) {
        if (subCommand.getName().equalsIgnoreCase(arg)) {
            return true;
        }

        for (String alias : subCommand.getAliases()) {
            if (alias.equalsIgnoreCase(arg)) {
                return true;
            }
        }

        return false;
    }

    public List<String> getTabCompletions(MinecraftServer server, ICommandSource sender, String[] args, @Nullable BlockPos pos) {
        if (this.tabCompleter != null) {
            return this.tabCompleter.apply(sender, args);
        }

        if (!this.executors.isEmpty()) {
            for (CommandExecutor executor : this.executors) {
                if (executor.getIdentifier().isEmpty()) {
                    List<String> values = executor.tabComplete(sender, args);

                    if (!values.isEmpty()) {
                        return values;
                    }
                }

                if (args.length == 0) {
                    continue;
                }

                if (!executor.getIdentifier().equalsIgnoreCase(args[0]) || (executor.getIdentifier().isEmpty() && !args[0].isEmpty())) {
                    continue;
                }

                List<String> values = executor.tabComplete(sender, Arrays.copyOfRange(args, 1, args.length));

                if (!values.isEmpty()) {
                    return values;
                }
            }
        }

        if (this.subCommands.size() == 0) {
            if (args.length == 0) {
                return this.getAllPlayers();
            } else {
                return this.getPlayers(sender, args[0]);
            }
        }

        List<String> values = Lists.newArrayList();

        for (ForgeCommand subCommand : this.subCommands) {
            if (args[0].equalsIgnoreCase(subCommand.getName()) || subCommand.getAliases().contains(args[0])) {
                values.addAll(subCommand.getTabCompletions(server, sender, Arrays.copyOfRange(args, 1, args.length), pos));
            }
        }

        if (values.isEmpty()) {
            values.addAll(this.getAccessibleSubCommands(sender));
        }

        return values;
    }

    protected List<String> getPlayers(ICommandSource sender, String name) {
        if (name.isEmpty()) {
            return this.getAllPlayers();
        }

        return this.getMatching(name, this.getAllPlayers());
    }

    protected List<String> getAllPlayers() {
        List<ServerPlayerEntity> players = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers();
        return players.stream().map(serverPlayerEntity -> serverPlayerEntity.getName().getString()).collect(Collectors.toList());
    }

    protected List<String> getAccessibleSubCommands(ICommandSource sender) {
        List<String> subCommands = Lists.newArrayList();

        for (ForgeCommand subCommand : this.subCommands) {
            if (!(sender instanceof ServerPlayerEntity)
                    || subCommand.checkPermission(ServerLifecycleHooks.getCurrentServer(), sender)) {
                subCommands.add(subCommand.name);
                subCommands.addAll(subCommand.aliases);
            }
        }

        return subCommands;
    }

    protected List<String> getMatching(String arg, List<String> potential) {
        List<String> args = Lists.newArrayList();

        for (String s : potential) {
            if (s.toLowerCase().startsWith(arg.toLowerCase())) {
                args.add(s);
            }
        }

        return args;
    }
}

package com.envyful.api.velocity.player.command.command;

import com.envyful.api.concurrency.UtilConcurrency;
import com.envyful.api.velocity.player.command.VelocityCommandFactory;
import com.envyful.api.velocity.player.command.command.executor.CommandExecutor;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 *
 * The Velocity command implementation for registering to the server. Handles checking all sub commands
 * and running all commands from the forge implementation.
 *
 */
public class VelocityCommand {

    private static final TextComponent NO_PERMISSION = Component.text("§c§l(!) §cNo permission!");

    private final VelocityCommandFactory commandFactory;
    private final String name;
    private final boolean child;
    private final List<Component> description;
    private final String basePermission;
    private final List<String> aliases;
    private final List<CommandExecutor> executors;
    private final List<VelocityCommand> subCommands;
    private final BiFunction<CommandSource, String[], List<String>> tabCompleter;

    public VelocityCommand(VelocityCommandFactory commandFactory, String name, boolean child, String[] description, String basePermission,
                           List<String> aliases, List<CommandExecutor> executors, List<VelocityCommand> subCommands,
                           BiFunction<CommandSource, String[], List<String>> tabCompleter) {
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

    private List<Component> initializeDescription(String[] description) {
        List<Component> newDescription = Lists.newArrayList();

        for (String s : description) {
            newDescription.add(Component.text(s));
        }

        if (!this.child) {
            newDescription.add(Component.text(""));
            Component textComponent = Component.text("§eFor further support visit the §nEnvyWare Ltd§e discord: ");
            textComponent = textComponent.style(textComponent.style().clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://discord.envyware.co.uk")));
            Component textComponent2 = Component.text("§e§nhttps://discord.envyware.co.uk§e");
            textComponent2 = textComponent2.style(textComponent2.style().clickEvent(ClickEvent.clickEvent(ClickEvent.Action.OPEN_URL, "https://discord.envyware.co.uk")));
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
    public boolean checkPermission(CommandSource sender) {
        if (this.basePermission.isEmpty() || !(sender instanceof Player)) {
            return true;
        }

        return sender.hasPermission(this.basePermission);
    }

    @ParametersAreNonnullByDefault
    public void execute(CommandSource sender, String[] args) throws CommandSyntaxException {
        UtilConcurrency.runAsync(() -> this.executeSync(sender, args));
    }

    public void executeSync(CommandSource sender, String[] args) {
        if (!this.checkPermission(sender)) {
            sender.sendMessage(NO_PERMISSION);
            return;
        }

        if (args.length > 0) {
            for (VelocityCommand subCommand : this.subCommands) {
                if (this.fitsCommand(args[0], subCommand)) {
                    subCommand.executeSync(sender, Arrays.copyOfRange(args, 1, args.length));
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

        for (Component textComponent : this.description) {
            sender.sendMessage(textComponent);
        }
    }

    private boolean attemptRun(CommandExecutor executor, CommandSource sender, String[] args) {
        if (!executor.canExecute(sender)) {
            sender.sendMessage(NO_PERMISSION);
            return true;
        }

        if (executor.getRequiredArgs() == -1 || executor.getRequiredArgs() == 0) {
            if (!executor.isExecutedAsync()) {
                executor.execute(sender, args);
                return true;
            }

            return executor.execute(sender, args);
        }

        if (executor.getRequiredArgs() <= (args.length + 1)) {
            if (!executor.isExecutedAsync()) {
                if (executor.execute(sender, args)) {
                    return true;
                }

                for (Component textComponent : this.description) {
                    sender.sendMessage(textComponent);
                }
                return true;
            }

            return executor.execute(sender, args);
        }

        return false;
    }

    private boolean fitsCommand(String arg, VelocityCommand subCommand) {
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

    public List<String> getTabCompletions(CommandSource sender, String[] args) {
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

        for (VelocityCommand subCommand : this.subCommands) {
            if (args[0].equalsIgnoreCase(subCommand.getName()) || subCommand.getAliases().contains(args[0])) {
                values.addAll(subCommand.getTabCompletions(sender, Arrays.copyOfRange(args, 1, args.length)));
            }
        }

        values.addAll(this.getAccessibleSubCommands(sender));
        return values;
    }

    protected List<String> getPlayers(CommandSource sender, String name) {
        if (name.isEmpty()) {
            return this.getAllPlayers();
        }

        return this.getMatching(name, this.getAllPlayers());
    }

    protected List<String> getAllPlayers() {
        Collection<Player> players = this.commandFactory.getProxy().getAllPlayers();
        return players.stream().map(serverPlayerEntity -> serverPlayerEntity.getUsername()).collect(Collectors.toList());
    }

    protected List<String> getAccessibleSubCommands(CommandSource sender) {
        List<String> subCommands = Lists.newArrayList();

        for (VelocityCommand subCommand : this.subCommands) {
            if (!(sender instanceof Player)
                    || subCommand.checkPermission(sender)) {
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

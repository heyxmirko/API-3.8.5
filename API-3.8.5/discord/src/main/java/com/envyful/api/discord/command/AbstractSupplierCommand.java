package com.envyful.api.discord.command;

import com.envyful.api.discord.permission.UtilRole;
import com.google.common.collect.Maps;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractSupplierCommand extends AbstractCommand {

    private final Map<Long, Function<Guild, Role>> roleSuppliers = Maps.newConcurrentMap();

    protected AbstractSupplierCommand(CommandData commandData) {
        super(commandData);
    }

    protected void registerRole(Guild guild, Function<Guild, Role> supplier) {
        this.roleSuppliers.put(guild.getIdLong(), supplier);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        if (event.isAcknowledged()) {
            return;
        }

        Function<Guild, Role> role = this.roleSuppliers.get(event.getGuild().getIdLong());

        if (role == null) {
            this.handleEvent(event);
            return;
        }

        Role apply = role.apply(event.getGuild());

        if (apply == null) {
            this.handleEvent(event);
            return;
        }

        if (!UtilRole.isHigherThan(event.getMember(), apply)) {
            event.reply(this.permissible.denyMessage()).setEphemeral(true).complete();
            return;
        }

        this.handleEvent(event);
    }
}

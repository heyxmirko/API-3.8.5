package com.envyful.api.discord.command;

import com.envyful.api.discord.command.data.Permissible;
import com.envyful.api.discord.listener.SubscribeEvent;
import com.envyful.api.discord.permission.UtilRole;
import com.google.common.collect.Maps;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Map;

/**
 *
 * Abstract command implementation to handle the permission checking from {@link Permissible}
 *
 */
public abstract class AbstractCommand implements Command {

    private final CommandData commandData;
    private final Map<Long, Role> guildToRole = Maps.newConcurrentMap();

    protected final Permissible permissible;

    protected AbstractCommand(CommandData commandData) {
        this.commandData = commandData;
        this.permissible = this.getClass().getAnnotation(Permissible.class);
    }

    @Override
    public void handle(SlashCommandInteractionEvent event) {
        if (event.isAcknowledged()) {
            return;
        }

        if (this.permissible != null) {
            Role role = this.guildToRole.get(event.getGuild().getIdLong());

            if (role == null) {
                this.handleEvent(event);
                return;
            }

            if (!UtilRole.isHigherThan(event.getMember(), role)) {
                event.reply(this.permissible.denyMessage()).setEphemeral(true).complete();
                return;
            }

            this.handleEvent(event);
            return;
        }

        this.handleEvent(event);
    }

    /**
     *
     * Method called after the permission checking has been completed
     *
     * @param event The slash command event
     */
    protected abstract void handleEvent(SlashCommandInteractionEvent event);

    @Override
    public void register(JDA jda, Guild guild) {
        guild.upsertCommand(this.commandData).complete();
        jda.addEventListener(this);

        if (this.permissible != null) {
            for (int i = 0; i < permissible.guild().length; i++) {
                long id = permissible.guild()[i];

                if (id == guild.getIdLong()) {
                    guildToRole.put(guild.getIdLong(), guild.getRoleById(this.permissible.role()[i]));
                    return;
                }
            }
        }
    }

    @SubscribeEvent
    public void onCommand(SlashCommandInteractionEvent event) {
        if (!event.getName().equals(this.commandData.getName())) {
            return;
        }

        this.handle(event);
    }
}

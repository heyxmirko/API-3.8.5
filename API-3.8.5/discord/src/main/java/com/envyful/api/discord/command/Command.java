package com.envyful.api.discord.command;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/**
 *
 * Interface to represent a Discord slash command, handling and registering the slash command
 *
 */
public interface Command {

    /**
     *
     * Called when a user runs the defined slash command
     *
     * @param event The event fired for the slash command
     */
    void handle(SlashCommandInteractionEvent event);

    /**
     *
     * Registers the command for the guild
     *
     * @param jda The running JDA
     * @param guild The guild being registered for
     */
    void register(JDA jda, Guild guild);

}

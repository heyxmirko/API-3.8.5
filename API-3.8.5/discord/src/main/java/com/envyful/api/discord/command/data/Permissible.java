package com.envyful.api.discord.command.data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *
 * Annotation for setting the required role ID for a given discord command
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Permissible {

    /**
     *
     * The long ID of the Discord {@link net.dv8tion.jda.api.entities.Guild} id's
     * Keep the index the same as the {@link Permissible#role()} indexes as it's treated as a map
     *
     * @return The discord guild ids
     */
    long[] guild() default {};

    /**
     *
     * The long ID of the Discord {@link net.dv8tion.jda.api.entities.Role} id's
     * Keep the index the same as the {@link Permissible#guild()} indexes as it's treated as a map
     *
     * @return The discord role ids
     */
    long[] role() default {};

    /**
     *
     * The message sent to the user when they're denied access to the command
     *
     * @return The message denying access
     */
    String denyMessage() default "";

}

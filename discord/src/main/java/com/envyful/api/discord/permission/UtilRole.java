package com.envyful.api.discord.permission;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;

/**
 *
 * Static utility class for Discord roles
 *
 */
public class UtilRole {

    /**
     *
     * Checks if a {@link Member} has a role greater than or equal to the given {@link Role}
     * Equivalent of checking if a {@link Member} has permission for certain actions (i.e. X must be manager or higher to do Y)
     *
     * @param member The member being checked
     * @param role The role they need to be greater than or equal to
     * @return True if they have permission - false otherwise
     */
    public static boolean isHigherThan(Member member, Role role) {
        for (Role memberRole : member.getRoles()) {
            if (memberRole.getPosition() >= role.getPosition()) {
                return true;
            }
        }

        return false;
    }

}

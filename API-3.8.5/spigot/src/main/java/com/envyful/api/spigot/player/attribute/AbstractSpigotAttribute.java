package com.envyful.api.spigot.player.attribute;

import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.spigot.player.SpigotEnvyPlayer;

import java.util.UUID;

/**
 *
 * Abstract implementation of the {@link PlayerAttribute} for the Spigot platform. This handles storing the manager
 * and the Spigot implementation of the "parent" {@link SpigotEnvyPlayer} class.
 *
 * @param <A> The manager class parameter
 */
public abstract class AbstractSpigotAttribute<A> implements PlayerAttribute<A> {

    protected final transient A manager;
    protected final transient SpigotEnvyPlayer parent;
    protected final UUID uuid;

    protected AbstractSpigotAttribute(A manager, SpigotEnvyPlayer parent) {
        this.manager = manager;
        this.parent = parent;
        this.uuid = parent.getUuid();
    }

    protected AbstractSpigotAttribute(UUID uuid) {
        this.manager = null;
        this.parent = null;
        this.uuid = uuid;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    public SpigotEnvyPlayer getParent() {
        return parent;
    }

    public A getManager() {
        return manager;
    }
}

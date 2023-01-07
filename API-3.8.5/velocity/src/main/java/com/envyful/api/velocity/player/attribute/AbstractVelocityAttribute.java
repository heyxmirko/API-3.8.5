package com.envyful.api.velocity.player.attribute;

import com.envyful.api.player.attribute.PlayerAttribute;
import com.envyful.api.velocity.player.VelocityEnvyPlayer;

import java.util.UUID;

/**
 *
 * Abstract implementation of the {@link PlayerAttribute} for the Velocity platform. This handles storing the manager
 * and the Velocity implementation of the "parent" {@link VelocityEnvyPlayer} class.
 *
 * @param <A> The manager class parameter
 */
public abstract class AbstractVelocityAttribute<A> implements PlayerAttribute<A> {

    protected final transient A manager;
    protected final transient VelocityEnvyPlayer parent;
    protected final UUID uuid;

    protected AbstractVelocityAttribute(A manager, VelocityEnvyPlayer parent) {
        this.manager = manager;
        this.parent = parent;
        this.uuid = parent.getUuid();
    }

    protected AbstractVelocityAttribute(UUID uuid) {
        this.manager = null;
        this.parent = null;
        this.uuid = uuid;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    public VelocityEnvyPlayer getParent() {
        return this.parent;
    }

    public A getManager() {
        return this.manager;
    }
}

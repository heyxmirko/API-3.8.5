package com.envyful.api.velocity.player;

import com.envyful.api.config.ConfigLocation;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.google.common.collect.Maps;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.Map;
import java.util.UUID;

/**
 *
 * Velocity implementation of the {@link EnvyPlayer} interface
 *
 */
public class VelocityEnvyPlayer implements EnvyPlayer<Player> {

    protected final Map<Class<?>, PlayerAttribute<?>> attributes = Maps.newHashMap();

    private ProxyServer proxy;
    private Player player;
    private UUID uuid;

    protected VelocityEnvyPlayer(ProxyServer proxy, UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.player.getUsername();
    }

    @Override
    public Player getParent() {
        return this.player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    @Override
    public void message(Object... messages) {
        for (Object message : messages) {
            if (message instanceof Component) {
                this.getParent().sendMessage((Component) message);
            } else if (message instanceof String) {
                //TODO: convert
            }
        }
    }

    @Override
    public void executeCommands(String... commands) {
        for (String command : commands) {
            this.executeCommand(command);
        }
    }

    @Override
    public void executeCommand(String command) {
        this.proxy.getCommandManager().executeAsync(this.getParent(), command);
    }

    @Override
    public void teleport(ConfigLocation location) {
        throw new UnsupportedOperationException("Cannot teleport players on the proxy!");
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends PlayerAttribute<B>, B> A getAttribute(Class<B> plugin) {
        return (A) this.attributes.get(plugin);
    }
}

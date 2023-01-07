package com.envyful.api.spigot.player;

import com.envyful.api.config.ConfigLocation;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.google.common.collect.Maps;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 *
 * Spigot implementation of the {@link EnvyPlayer} interface
 *
 */
public class SpigotEnvyPlayer implements EnvyPlayer<Player> {

    protected final Map<Class<?>, PlayerAttribute<?>> attributes = Maps.newHashMap();

    private Player player;
    private UUID uuid;

    protected SpigotEnvyPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public UUID getUuid() {
        return this.uuid;
    }

    @Override
    public String getName() {
        return this.player.getName();
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
        this.getParent().performCommand(command);
    }

    @Override
    public void teleport(ConfigLocation location) {
        //TODO:
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends PlayerAttribute<B>, B> A getAttribute(Class<B> plugin) {
        return (A) this.attributes.get(plugin);
    }
}

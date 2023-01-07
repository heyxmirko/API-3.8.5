package com.envyful.api.forge.player;

import com.envyful.api.config.ConfigLocation;
import com.envyful.api.forge.chat.UtilChatColour;
import com.envyful.api.forge.world.UtilWorld;
import com.envyful.api.player.EnvyPlayer;
import com.envyful.api.player.attribute.PlayerAttribute;
import com.google.common.collect.Maps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 * Forge implementation of the {@link EnvyPlayer} interface
 *
 */
public class ForgeEnvyPlayer implements EnvyPlayer<ServerPlayer> {

    protected final Map<Class<?>, PlayerAttribute<?>> attributes = Maps.newHashMap();

    private ServerPlayer player;

    protected ForgeEnvyPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public UUID getUuid() {
        return this.player.getUUID();
    }

    @Override
    public String getName() {
        return this.player.getName().getString();
    }

    @Override
    public ServerPlayer getParent() {
        return this.player;
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    @Override
    public void message(Object... messages) {
        for (Object message : messages) {
            if (message instanceof String) {
                this.getParent().sendSystemMessage(UtilChatColour.colour((String) message));
            } else if (message instanceof Component) {
                this.getParent().sendSystemMessage((Component) message);
            } else if (message instanceof List) {
                for (Object subMessage : ((List) message)) {
                    this.message(subMessage);
                }
            } else {
                throw new RuntimeException("Unsupported message type");
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
        ServerLifecycleHooks.getCurrentServer().getCommands().performCommand(this.player.createCommandSourceStack(), command);
    }

    @Override
    public void teleport(ConfigLocation location) {
        this.getParent().teleportTo((ServerLevel) UtilWorld.findWorld(location.getWorldName()),
                location.getPosX(), location.getPosY(), location.getPosZ(), (float)location.getPitch(), (float)location.getYaw());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <A extends PlayerAttribute<B>, B> A getAttribute(Class<B> plugin) {
        return (A) this.attributes.get(plugin);
    }
}

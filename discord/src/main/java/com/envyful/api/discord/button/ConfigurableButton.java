package com.envyful.api.discord.button;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 *
 * Config serializable entry for a Discord button
 *
 */
@ConfigSerializable
public class ConfigurableButton {

    protected String idOrUrl;
    protected ButtonStyle style;
    protected String label;
    protected boolean enabled = true;

    public ConfigurableButton(String idOrUrl, ButtonStyle style, String label) {
        this.idOrUrl = idOrUrl;
        this.style = style;
        this.label = label;
    }

    public ConfigurableButton() {
        this("example", ButtonStyle.PRIMARY, "Haha, Hacko Smelly!");
    }

    public String getIdOrUrl() {
        return this.idOrUrl;
    }

    public ButtonStyle getStyle() {
        return this.style;
    }

    public String getLabel() {
        return this.label;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public Button create(JDA jda) {
        return Button.of(this.style, this.idOrUrl, this.label)
                .withDisabled(!this.enabled);
    }
}

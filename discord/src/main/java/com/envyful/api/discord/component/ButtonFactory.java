package com.envyful.api.discord.component;

import com.envyful.api.discord.listener.SubscribeEvent;
import com.google.common.collect.Maps;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.interactions.component.ButtonImpl;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ButtonFactory {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final Map<String, Builder> REGISTERED_BUTTONS = Maps.newConcurrentMap();

    @SubscribeEvent
    public static void onButtonInteract(ButtonInteractionEvent event) {
        Builder builder = REGISTERED_BUTTONS.get(event.getComponentId().toLowerCase(Locale.ROOT));

        if (builder == null) {
            return;
        }

        builder.interactionHandler.accept(event);
    }

    public static Builder button() {
        return new Builder();
    }

    public static Builder primary() {
        return new Builder().style(ButtonStyle.PRIMARY);
    }

    public static Builder danger() {
        return new Builder().style(ButtonStyle.DANGER);
    }

    public static Builder secondary() {
        return new Builder().style(ButtonStyle.SECONDARY);
    }

    public static class Builder {

        protected String id;
        protected String label;
        protected ButtonStyle style;
        protected Emoji emoji;
        protected boolean disabled;
        protected Consumer<ButtonInteractionEvent> interactionHandler;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder style(ButtonStyle style) {
            this.style = style;
            return this;
        }

        public Builder emoji(Emoji emoji) {
            this.emoji = emoji;
            return this;
        }

        public Builder emoji(String unicode) {
            this.emoji = Emoji.fromUnicode(unicode);
            return this;
        }

        public Builder disabled() {
            this.disabled = true;
            return this;
        }

        public Builder enabled() {
            this.disabled = false;
            return this;
        }

        public Builder handler(Consumer<ButtonInteractionEvent> interactionHandler) {
            this.interactionHandler = interactionHandler;
            return this;
        }

        public Button build(JDA jda) {
            if (!REGISTERED.get()) {
                REGISTERED.set(true);
                jda.addEventListener(ButtonFactory.class);
            }

            REGISTERED_BUTTONS.put(this.id.toLowerCase(Locale.ROOT), this);
            return new ButtonImpl(this.id, this.label, this.style, this.disabled, this.emoji);
        }
    }
}

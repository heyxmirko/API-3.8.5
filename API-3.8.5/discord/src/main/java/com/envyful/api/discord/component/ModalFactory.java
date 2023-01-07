package com.envyful.api.discord.component;

import com.envyful.api.discord.listener.SubscribeEvent;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.Modal;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ModalFactory {

    private static final AtomicBoolean REGISTERED = new AtomicBoolean(false);
    private static final Map<String, Builder> REGISTERED_MODALS = Maps.newConcurrentMap();

    @SubscribeEvent
    public static void onModalInteract(ModalInteractionEvent event) {
        Builder builder = REGISTERED_MODALS.get(event.getModalId().toLowerCase(Locale.ROOT));

        if (builder == null) {
            return;
        }

        builder.interactionHandler.accept(event);
    }

    public static Builder modal() {
        return new Builder();
    }

    public static class Builder {

        protected String id;
        protected String title;
        protected final List<ActionRow> components = new ArrayList<>(5);
        protected Consumer<ModalInteractionEvent> interactionHandler;

        private Builder() {
        }

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder row(ItemComponent... components) {
            this.components.add(ActionRow.of(components));
            return this;
        }

        public Builder rows(ActionRow... rows) {
            this.components.addAll(Lists.newArrayList(rows));
            return this;
        }

        public Builder handler(Consumer<ModalInteractionEvent> interactionHandler) {
            this.interactionHandler = interactionHandler;
            return this;
        }

        public Modal build(JDA jda) {
            if (!REGISTERED.get()) {
                REGISTERED.set(true);
                jda.addEventListener(ModalFactory.class);
            }

            REGISTERED_MODALS.put(this.id.toLowerCase(Locale.ROOT), this);
            return Modal.create(this.id, this.title)
                    .addActionRows(this.components)
                    .build();
        }
    }
}

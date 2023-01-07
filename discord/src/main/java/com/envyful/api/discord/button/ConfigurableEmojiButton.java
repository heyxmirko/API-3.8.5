package com.envyful.api.discord.button;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

/**
 *
 * Config serializable entry for a Discord button with optional Emoji
 *
 */
@ConfigSerializable
public class ConfigurableEmojiButton extends ConfigurableButton {

    private boolean unicode = false;
    private boolean markdown = false;
    private String emojiId;
    private transient Emoji emoji = null;

    public ConfigurableEmojiButton(String idOrUrl, ButtonStyle style, String label, String emojiId) {
        super(idOrUrl, style, label);

        this.emojiId = emojiId;
    }

    public ConfigurableEmojiButton() {
        super();
    }

    public Emoji getEmoji(JDA jda) {
        if (this.unicode) {
            return Emoji.fromUnicode(this.emojiId);
        }

        if (this.markdown) {
            return Emoji.fromFormatted(this.emojiId);
        }

        return jda.getEmojiById(Long.parseLong(this.emojiId));
    }

    @Override
    public Button create(JDA jda) {
        return super.create(jda)
                .withEmoji(this.getEmoji(jda));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        protected boolean unicode = false;
        protected boolean markdown = false;
        protected String emojiId;
        protected String idOrUrl;
        protected ButtonStyle style;
        protected String label;
        protected boolean enabled = true;

        private Builder() {}

        public Builder idOrUrl(String idOrUrl) {
            this.idOrUrl = idOrUrl;
            return this;
        }

        public Builder style(ButtonStyle style) {
            this.style = style;
            return this;
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder enabled() {
            this.enabled = true;
            return this;
        }

        public Builder disabled() {
            this.enabled = false;
            return this;
        }

        public Builder emoji(String emoji) {
            this.emojiId = emoji;
            return this;
        }

        public Builder unicodeEmoji() {
            this.unicode = true;
            return this;
        }

        public Builder markdownEmoji() {
            this.markdown = true;
            this.unicode = false;
            return this;
        }

        public ConfigurableEmojiButton build() {
            ConfigurableEmojiButton button = new ConfigurableEmojiButton(
                    this.idOrUrl, this.style, this.label, this.emojiId
            );

            button.unicode = this.unicode;
            button.markdown = this.markdown;
            button.enabled = this.enabled;
            return button;
        }
    }
}

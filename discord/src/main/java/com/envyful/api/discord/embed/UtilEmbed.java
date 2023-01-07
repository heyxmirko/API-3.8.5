package com.envyful.api.discord.embed;

import com.envyful.api.config.Replacer;
import com.envyful.api.config.UtilReplacer;
import com.envyful.api.discord.EmbedObject;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/**
 *
 * Static utility for handling discord {@link MessageEmbed}s
 *
 */
public class UtilEmbed {

    /**
     *
     * Static utility method for converting from the JSON API object to the actual Discord {@link MessageEmbed} object
     *
     * @param object The original JSON object
     * @param replacers Replacers for handling any configurable placeholders
     * @return The original embed
     */
    public static MessageEmbed fromAPI(EmbedObject object, Replacer... replacers) {
        EmbedBuilder builder = new EmbedBuilder()
                .setTitle(UtilReplacer.getReplacedText(object.getTitle(), replacers))
                .setColor(object.getColor());

        if (object.getImage() != null) {
            builder.setImage(UtilReplacer.getReplacedText(object.getImage().getUrl(), replacers));
        }

        if (object.getAuthor() != null) {
            builder.setAuthor(
                    UtilReplacer.getReplacedText(object.getAuthor().getName(), replacers),
                    UtilReplacer.getReplacedText(object.getAuthor().getUrl(), replacers),
                    UtilReplacer.getReplacedText(object.getAuthor().getIconUrl(), replacers));
        }

        if (object.getDescription() != null) {
            builder.setDescription(UtilReplacer.getReplacedText(object.getDescription(), replacers));
        }

        if (object.getThumbnail() != null) {
            builder.setThumbnail(UtilReplacer.getReplacedText(object.getThumbnail().getUrl(), replacers));
        }

        if (object.getFooter() != null) {
            builder.setFooter(UtilReplacer.getReplacedText(object.getFooter().getText(), replacers), UtilReplacer.getReplacedText(object.getFooter().getIconUrl(), replacers));
        }

        for (EmbedObject.Field field : object.getFields()) {
            builder.addField(UtilReplacer.getReplacedText(field.getName(), replacers),
                    UtilReplacer.getReplacedText(field.getValue(), replacers),
                    field.isInline());
        }

        return builder.build();
    }
}

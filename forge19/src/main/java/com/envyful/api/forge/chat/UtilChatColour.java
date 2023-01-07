package com.envyful.api.forge.chat;

import com.envyful.api.config.Replacer;
import com.envyful.api.config.UtilReplacer;
import com.google.common.collect.Lists;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * Static utility methods relating to colour codes
 *
 */
public class UtilChatColour {

    private static final char COLOUR_CHAR = '§';
    private static final String CHARACTERS = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";
    private static final Pattern COLOUR_PATTERN = Pattern.compile("&(#\\w{6}|[\\da-zA-Z])");
    private static final Pattern STRIP_PATTERN = Pattern.compile("(?i)&([0-9A-FK-ORX]|#([A-F0-9]{6}|[A-F0-9]{3}))");

    /**
     *
     * Parses the string to a {@link Component} with the correctly formatted colour codes and hex codes
     *
     * @param text The unformatted text
     * @return The newly formatted text
     */
    public static Component colour(String text, Replacer... placeholders) {
        if (text.contains("{")) {
            return Component.Serializer.fromJson(text);
        }

        Matcher matcher = COLOUR_PATTERN.matcher(text);
        MutableComponent textComponent = Component.literal("");
        ChatFormatting nextApply = null;
        int lastEnd = 0;
        TextColor lastColor = null;

        while (matcher.find()) {
            int start = matcher.start();
            String segment = text.substring(lastEnd, start);
            MutableComponent iFormattableTextComponent = attemptAppend(textComponent, segment, lastColor, placeholders);

            if (nextApply != null && iFormattableTextComponent != null) {
                iFormattableTextComponent.withStyle(nextApply);
            }

            lastEnd = matcher.end();
            String colourCode = matcher.group(1);
            Optional<TextColor> colour = parseColour(colourCode);

            if (colour.isPresent()) {
                lastColor = colour.get();
                nextApply = null;
            } else {
                ChatFormatting byCode = ChatFormatting.getByCode(colourCode.toCharArray()[0]);

                if (byCode != null) {
                    nextApply = byCode;
                } else {
                    textComponent.append(Component.literal("&" + colourCode));
                }
            }
        }

        String segment = text.substring(lastEnd);
        MutableComponent iFormattableTextComponent = attemptAppend(textComponent, segment, lastColor, placeholders);

        if (nextApply != null && iFormattableTextComponent != null) {
            iFormattableTextComponent.withStyle(nextApply);
        }

        return textComponent;
    }

    /**
     *
     * Attempts to append the segment to the {@link Component} with the given (nullable) colour
     *
     * @param textComponent The text component
     * @param segment The segment
     * @param lastColour The colour
     */
    public static MutableComponent attemptAppend(MutableComponent textComponent, String segment, TextColor lastColour, Replacer... placeholers) {
        if (segment.isEmpty()) {
            return null;
        }

        segment = UtilReplacer.getReplacedText(segment, placeholers);
        MutableComponent appended = Component.literal(segment);

        if (lastColour != null) {
            appended.setStyle(Style.EMPTY.withColor(lastColour));
        }

        textComponent.append(appended);
        return appended;
    }

    /**
     *
     * Attempts to parse the colour code firstly as a hex, then as a legacy
     *
     * @param colourCode The colour code
     * @return The potential equivalent colour
     */
    public static Optional<TextColor> parseColour(String colourCode) {
        TextColor colour = TextColor.parseColor(colourCode);

        if (colour != null) {
            return Optional.of(colour);
        }

        if (colourCode.length() > 1) {
            return Optional.empty();
        }

        ChatFormatting byCode = getByCode(colourCode.toCharArray()[0]);

        if (byCode == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(TextColor.fromLegacyFormat(byCode));
    }

    public static ChatFormatting getByCode(char p_211165_0_) {
        char c0 = Character.toString(p_211165_0_).toLowerCase(Locale.ROOT).charAt(0);

        switch (c0) {
            case '0': return ChatFormatting.BLACK;
            case '1': return ChatFormatting.DARK_BLUE;
            case '2': return ChatFormatting.DARK_GREEN;
            case '3': return ChatFormatting.DARK_AQUA;
            case '4': return ChatFormatting.DARK_RED;
            case '5': return ChatFormatting.DARK_PURPLE;
            case '6': return ChatFormatting.GOLD;
            case '7': return ChatFormatting.GRAY;
            case '8': return ChatFormatting.DARK_GRAY;
            case '9': return ChatFormatting.BLUE;
            case 'a': return ChatFormatting.GREEN;
            case 'b': return ChatFormatting.AQUA;
            case 'c': return ChatFormatting.RED;
            case 'd': return ChatFormatting.LIGHT_PURPLE;
            case 'e': return ChatFormatting.YELLOW;
            case 'f': return ChatFormatting.WHITE;
            case 'k': return ChatFormatting.OBFUSCATED;
            case 'l': return ChatFormatting.BOLD;
            case 'm': return ChatFormatting.STRIKETHROUGH;
            case 'n': return ChatFormatting.UNDERLINE;
            case 'o': return ChatFormatting.ITALIC;
            case 'r': return ChatFormatting.RESET;
        }

        return null;
    }

    /**
     *
     * Translates the text to coloured text.
     * Reference: https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/ChatColor.java#216
     *
     * @param altColorChar The character
     * @param textToTranslate The text
     * @return The coloured text
     */
    public static String translateColourCodes(char altColorChar, String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for (int i = 0; i < b.length - 1; i++) {
            if (b[i] == altColorChar && CHARACTERS.indexOf(b[i + 1]) > -1) {
                b[i] = COLOUR_CHAR;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    /**
     *
     * Translates each line in the list to coloured text.
     * Using {@link UtilChatColour#translateColourCodes(char, String)}
     *
     * @param altColorChar The character
     * @param lines The lines of text
     * @return The coloured text
     */
    public static List<String> translateColourCodes(char altColorChar, List<String> lines) {
        List<String> newLines = Lists.newArrayList();

        for (String line : lines) {
            newLines.add(translateColourCodes('&', line));
        }

        return newLines;
    }

    /**
     *
     * Removes the colour codes in a message
     *
     * @param input The original message
     * @return The stripped message
     */
    public static String stripColor(@Nullable String input) {
        if (input == null) {
            return null;
        }

        return STRIP_PATTERN.matcher(input).replaceAll("");
    }
}

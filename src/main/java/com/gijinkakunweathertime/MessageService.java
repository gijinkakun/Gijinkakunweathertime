package com.gijinkakunweathertime;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides access to localized plugin messages with placeholder replacement.
 */
public class MessageService {

    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("(?i)(?:&#|<#|#)([0-9a-f]{6})(?:>)?");

    private FileConfiguration messagesConfig;
    private FileConfiguration fallbackConfig;
    private final Logger logger;

    /**
     * @param messagesConfig dedicated configuration file that stores localized strings
     * @param fallbackConfig plugin configuration used when a localized string is missing
     * @param logger         logger used to report missing message keys
     */
    public MessageService(FileConfiguration messagesConfig, FileConfiguration fallbackConfig, Logger logger) {
        this.messagesConfig = messagesConfig;
        this.fallbackConfig = fallbackConfig;
        this.logger = logger;
    }

    /**
     * Reloads the backing configuration references so messages reflect the latest files.
     */
    public synchronized void reload(FileConfiguration messagesConfig, FileConfiguration fallbackConfig) {
        this.messagesConfig = messagesConfig;
        this.fallbackConfig = fallbackConfig;
    }

    /**
     * Retrieves a localized message, first from the language file and then from the fallback config.
     *
     * @param key The message key (without the <code>messages.</code> prefix).
     * @return The localized message with color codes translated.
     */
    public String getMessage(String key) {
        String path = "messages." + key;
        String message = messagesConfig.getString(path);
        if (message == null) {
            message = fallbackConfig.getString(path);
        }
        if (message == null) {
            logger.warning("Missing message for key: " + path);
            message = key;
        }
        return ChatColor.translateAlternateColorCodes('&', applyHexColors(message));
    }

    /**
     * Formats a localized message by replacing placeholders of the form {@code {placeholder}}.
     *
     * @param key          The message key (without the <code>messages.</code> prefix).
     * @param placeholders Map of placeholder names to replacement values.
     * @return The formatted message.
     */
    public String format(String key, Map<String, String> placeholders) {
        String message = getMessage(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return message;
    }

    private String applyHexColors(String input) {
        Matcher matcher = HEX_COLOR_PATTERN.matcher(input);
        StringBuilder builder = new StringBuilder();
        int lastEnd = 0;
        while (matcher.find()) {
            builder.append(input, lastEnd, matcher.start());
            String hex = matcher.group(1);
            builder.append(net.md_5.bungee.api.ChatColor.of("#" + hex));
            lastEnd = matcher.end();
        }
        builder.append(input.substring(lastEnd));
        return builder.toString();
    }
}

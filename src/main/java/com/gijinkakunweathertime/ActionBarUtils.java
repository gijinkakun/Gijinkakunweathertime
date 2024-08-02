package com.gijinkakunweathertime;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Utility class for sending action bar messages to players.
 */
public class ActionBarUtils {

    private final FileConfiguration config;

    /**
     * Constructs an ActionBarUtils instance.
     *
     * @param config The configuration file.
     */
    public ActionBarUtils(FileConfiguration config) {
        this.config = config;
    }

    /**
     * Sends an action bar message to all online players.
     *
     * @param message The message to send.
     */
    public void sendActionBarToAllPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }

    /**
     * Sends a cooldown message to a specific player.
     *
     * @param player      The player to send the message to.
     * @param minutesLeft The number of minutes left on cooldown.
     */
    public void sendCooldownMessage(Player player, long minutesLeft) {
        String message = config.getString("messages.cooldown").replace("{minutes}", String.valueOf(minutesLeft));
        TextComponent component = new TextComponent(message);
        component.setColor(ChatColor.RED);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    /**
     * Sends a vote progress message to all online players.
     *
     * @param vote         The type of vote.
     * @param currentVotes The current number of votes.
     * @param requiredVotes The required number of votes.
     */
    public void sendVoteMessage(String vote, int currentVotes, int requiredVotes) {
        String message = config.getString("messages.vote_progress")
            .replace("{current}", String.valueOf(currentVotes))
            .replace("{required}", String.valueOf(requiredVotes))
            .replace("{vote}", vote);
        TextComponent component = new TextComponent(message);
        component.setColor(ChatColor.YELLOW);
        sendActionBarToAllPlayers(component.toLegacyText());
    }

    /**
     * Sends a success message when a vote passes.
     *
     * @param vote The type of vote.
     */
    public void sendPraiseMessage(String vote) {
        String message = config.getString("messages.vote_success").replace("{vote}", vote);
        TextComponent component = new TextComponent(message);
        component.setColor(ChatColor.GREEN);
        sendActionBarToAllPlayers(component.toLegacyText());
    }

    /**
     * Sends a message when a vote expires without passing.
     *
     * @param vote The type of vote.
     */
    public void sendVoteExpiredMessage(String vote) {
        String message = config.getString("messages.vote_expired").replace("{vote}", vote);
        TextComponent component = new TextComponent(message);
        component.setColor(ChatColor.RED);
        sendActionBarToAllPlayers(component.toLegacyText());
    }
}

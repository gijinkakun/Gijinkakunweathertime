package com.gijinkakunweathertime;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionBarUtils {

    public static void sendActionBarToAllPlayers(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
        }
    }

    public static void sendCooldownMessage(Player player, long minutesLeft) {
        String message = ChatColor.RED + "You have already praised. On Cooldown for " + String.valueOf(minutesLeft) + " minutes.";
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
    }

    public static void sendVoteMessage(String vote, int currentVotes, int requiredVotes) {
        String message = ChatColor.YELLOW + String.valueOf(currentVotes) + " / " + String.valueOf(requiredVotes) + " have praised for " + vote.toLowerCase() + ".";
        sendActionBarToAllPlayers(message);
    }

    public static void sendPraiseMessage(String vote) {
        String message = ChatColor.GREEN + vote + " has been praised!";
        sendActionBarToAllPlayers(message);
    }

    public static void sendVoteExpiredMessage(String vote) {
        String message = ChatColor.RED + "Time has expired. Not enough votes for " + vote.toLowerCase();
        sendActionBarToAllPlayers(message);
    }
}

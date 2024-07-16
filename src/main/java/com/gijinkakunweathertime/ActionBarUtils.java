package com.gijinkakunweathertime;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ActionBarUtils {

    public static void sendActionBarToAllPlayers(String message) {
        String coloredMessage = ChatColor.YELLOW + message;
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(coloredMessage));
        }
    }
}

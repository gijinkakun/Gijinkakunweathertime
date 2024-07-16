package com.gijinkakunweathertime;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {

    private final VoteManager voteManager;

    public PlayerChatListener(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage().toLowerCase();
        Player player = event.getPlayer();
        if (message.contains("praise the light")) {
            voteManager.addVote(player, "Praise the light");
        } else if (message.contains("praise the dark")) {
            voteManager.addVote(player, "Praise the dark");
        } else if (message.contains("praise the sun")) {
            voteManager.addVote(player, "Praise the sun");
        } else if (message.contains("praise the rain")) {
            voteManager.addVote(player, "Praise the rain");
        }
    }
}

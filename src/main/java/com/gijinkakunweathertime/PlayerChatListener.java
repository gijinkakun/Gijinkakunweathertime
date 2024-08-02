package com.gijinkakunweathertime;

import com.google.inject.Inject;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Listens to player chat messages to detect praise commands.
 */
public class PlayerChatListener implements Listener {

    private final VoteManager voteManager;

    /**
     * Constructs a PlayerChatListener instance.
     *
     * @param voteManager The VoteManager instance.
     */
    @Inject
    public PlayerChatListener(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    /**
     * Handles player chat events to detect praise commands.
     *
     * @param event The player chat event.
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage().toLowerCase();
        Player player = event.getPlayer();
        if (message.contains("praise the light")) {
            voteManager.addVote(player, "light");
        } else if (message.contains("praise the dark")) {
            voteManager.addVote(player, "dark");
        } else if (message.contains("praise the sun")) {
            voteManager.addVote(player, "sun");
        } else if (message.contains("praise the rain")) {
            voteManager.addVote(player, "rain");
        }
    }
}
package com.gijinkakunweathertime;

import com.google.inject.Inject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Cleans up vote participation when players disconnect so tallies stay accurate.
 */
public class PlayerLifecycleListener implements Listener {

    private final VoteManager voteManager;

    @Inject
    public PlayerLifecycleListener(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        voteManager.removeParticipation(event.getPlayer().getUniqueId());
    }
}

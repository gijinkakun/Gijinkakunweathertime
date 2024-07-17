package com.gijinkakunweathertime;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VoteManager {

    private final Map<String, Integer> votes;
    private final Map<Player, String> playerVotes;
    private final Map<UUID, Long> lastPraiseTime; // Track last praise time for each player
    public static final long COOLDOWN_TIME = 10 * 60 * 1000; // 10 minutes in milliseconds
    private long lastVoteTime;

    public VoteManager() {
        this.votes = new HashMap<>();
        this.playerVotes = new HashMap<>();
        this.lastPraiseTime = new HashMap<>();
        this.lastVoteTime = 0;
        startVoteExpirationTask();
    }

    public void addVote(Player player, String vote) {
        long currentTime = System.currentTimeMillis();

        if (votes.isEmpty()) {
            lastVoteTime = currentTime;
        }

        playerVotes.put(player, vote);
        votes.put(vote, votes.getOrDefault(vote, 0) + 1);
        lastPraiseTime.put(player.getUniqueId(), currentTime); // Update last praise time
        sendActionBarMessage(vote);
        checkVotes(vote);
    }

    public long getLastPraiseTime(UUID playerUUID) {
        return lastPraiseTime.getOrDefault(playerUUID, 0L);
    }

    private void checkVotes(String vote) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int requiredVotes = (int) Math.ceil(onlinePlayers / 2.0);
        if (votes.get(vote) >= requiredVotes) {
            executeVoteAction(vote);
            votes.clear();
            playerVotes.clear();
        }
    }

    private void executeVoteAction(String vote) {
        switch (vote) {
            case "Praise the light":
                Bukkit.getWorlds().get(0).setTime(1000);
                break;
            case "Praise the dark":
                Bukkit.getWorlds().get(0).setTime(13000);
                break;
            case "Praise the sun":
                Bukkit.getWorlds().get(0).setStorm(false);
                break;
            case "Praise the rain":
                Bukkit.getWorlds().get(0).setStorm(true);
                break;
        }
        ActionBarUtils.sendPraiseMessage(vote);
    }

    private void sendActionBarMessage(String vote) {
        int currentVotes = votes.getOrDefault(vote, 0);
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int requiredVotes = (int) Math.ceil(onlinePlayers / 2.0);
        ActionBarUtils.sendVoteMessage(vote, currentVotes, requiredVotes);
    }

    private void resetVotes() {
        votes.clear();
        playerVotes.clear();
        Bukkit.getScheduler().runTask(GijinkakunWeatherTime.getInstance(), ActionBarUtils::sendVoteExpiredMessage);
    }

    private void startVoteExpirationTask() {
        Bukkit.getScheduler().runTaskTimer(GijinkakunWeatherTime.getInstance(), () -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastVoteTime > 60000 && !votes.isEmpty()) {
                resetVotes();
                lastVoteTime = currentTime; // Reset the last vote time after expiration
            }
        }, 0L, 1200L); // Runs every 60 seconds (1200 ticks)
    }
}

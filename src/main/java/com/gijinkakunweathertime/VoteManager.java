package com.gijinkakunweathertime;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class VoteManager {

    private final Map<String, Integer> votes;
    private final Map<Player, String> playerVotes;
    private long lastVoteTime;

    public VoteManager() {
        this.votes = new HashMap<>();
        this.playerVotes = new HashMap<>();
        this.lastVoteTime = 0;
        startVoteExpirationTask();
    }

    public void addVote(Player player, String vote) {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastVoteTime > 60000) {
            resetVotes(ChatColor.RED + "Time has expired not enough votes for " + vote.toLowerCase());
            lastVoteTime = currentTime;
        }

        playerVotes.put(player, vote);
        votes.put(vote, votes.getOrDefault(vote, 0) + 1);
        sendActionBarMessage(vote);
        checkVotes(vote);
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
            case "Praise the night":
                Bukkit.getWorlds().get(0).setTime(13000);
                break;
            case "Praise the sun":
                Bukkit.getWorlds().get(0).setStorm(false);
                break;
            case "Praise the rain":
                Bukkit.getWorlds().get(0).setStorm(true);
                break;
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + vote + " has been praised!");
    }

    private void sendActionBarMessage(String vote) {
        int currentVotes = votes.getOrDefault(vote, 0);
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int requiredVotes = (int) Math.ceil(onlinePlayers / 2.0);
        String message = currentVotes + " / " + requiredVotes + " have " + vote.toLowerCase() + ".";
        ActionBarUtils.sendActionBarToAllPlayers(ChatColor.YELLOW + message);
    }

    private void resetVotes(String message) {
        votes.clear();
        playerVotes.clear();
        Bukkit.getScheduler().runTask(GijinkakunWeatherTime.getInstance(), () -> {
            ActionBarUtils.sendActionBarToAllPlayers(message);
        });
    }

    private void startVoteExpirationTask() {
        Bukkit.getScheduler().runTaskTimer(GijinkakunWeatherTime.getInstance(), () -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastVoteTime > 60000 && !votes.isEmpty()) {
                resetVotes(ChatColor.RED + "Time has expired not enough votes for the current vote");
            }
        }, 0L, 1200L); // Runs every 60 seconds (1200 ticks)
    }
}
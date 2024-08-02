package com.gijinkakunweathertime;

import com.google.inject.Inject;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages the voting process for changing time or weather.
 */
public class VoteManager {

    private final Map<String, Integer> votes;
    private final Map<Player, String> playerVotes;
    private final Map<UUID, Long> lastPraiseTime; // Track last praise time for each player
    private final long cooldownTime; // Cooldown time in milliseconds
    private long lastVoteTime;
    private String currentVote;
    private final ActionBarUtils actionBarUtils;
    private final JavaPlugin plugin;
    private final Map<String, Long> lastSuccessfulPraise; // Track last successful praise time for each type
    private final FileConfiguration config;

    /**
     * Constructs a VoteManager instance.
     *
     * @param actionBarUtils The ActionBarUtils instance.
     * @param plugin         The JavaPlugin instance.
     * @param config         The configuration file.
     */
    @Inject
    public VoteManager(ActionBarUtils actionBarUtils, JavaPlugin plugin, FileConfiguration config) {
        this.votes = new ConcurrentHashMap<>();
        this.playerVotes = new ConcurrentHashMap<>();
        this.lastPraiseTime = new ConcurrentHashMap<>();
        this.lastVoteTime = 0;
        this.currentVote = "";
        this.actionBarUtils = actionBarUtils;
        this.plugin = plugin;
        this.config = config;
        this.cooldownTime = config.getInt("cooldown_time", 10) * 60 * 1000L; // Convert minutes to milliseconds
        this.lastSuccessfulPraise = new ConcurrentHashMap<>(); // Initialize the map to track last successful praise time
        startVoteExpirationTask();
    }

    /**
     * Adds a vote from a player.
     *
     * @param player The player adding the vote.
     * @param vote   The type of vote.
     */
    public synchronized void addVote(Player player, String vote) {
        long currentTime = System.currentTimeMillis();

        // Logging vote attempt
        plugin.getLogger().info(player.getName() + " is attempting to vote for " + vote);

        // Check if the vote type has already been successfully praised recently
        if (lastSuccessfulPraise.containsKey(vote) && (currentTime - lastSuccessfulPraise.get(vote)) < cooldownTime) {
            player.sendMessage(ChatColor.RED + config.getString("messages.already_praised").replace("{vote}", vote));
            return;
        }

        if (votes.isEmpty()) {
            lastVoteTime = currentTime;
            currentVote = vote;
            plugin.getLogger().info("Starting new vote for " + vote);
        }

        playerVotes.put(player, vote);
        votes.put(vote, votes.getOrDefault(vote, 0) + 1);
        lastPraiseTime.put(player.getUniqueId(), currentTime); // Update last praise time
        sendActionBarMessage(vote);
        checkVotes(vote);
    }

    /**
     * Checks if the current vote matches the given vote.
     *
     * @param vote The vote to check.
     * @return true if the current vote matches, false otherwise.
     */
    public synchronized boolean isCurrentVote(String vote) {
        return currentVote.equals(vote) || currentVote.isEmpty();
    }

    /**
     * Gets the last praise time for a player.
     *
     * @param playerUUID The player's UUID.
     * @return The last praise time.
     */
    public long getLastPraiseTime(UUID playerUUID) {
        return lastPraiseTime.getOrDefault(playerUUID, 0L);
    }

    /**
     * Gets the cooldown time.
     *
     * @return The cooldown time in milliseconds.
     */
    public long getCooldownTime() {
        return cooldownTime;
    }

    /**
     * Checks if the required number of votes has been reached and executes the vote action.
     *
     * @param vote The type of vote.
     */
    private synchronized void checkVotes(String vote) {
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int requiredVotes = (int) Math.ceil(onlinePlayers / 2.0);
        if (votes.get(vote) >= requiredVotes) {
            executeVoteAction(vote);
            votes.clear();
            playerVotes.clear();
            currentVote = "";
            plugin.getLogger().info("Vote for " + vote + " passed.");
        } else {
            plugin.getLogger().info("Current votes for " + vote + ": " + votes.get(vote) + "/" + requiredVotes);
        }
    }

    /**
     * Executes the action associated with the vote.
     *
     * @param vote The type of vote.
     */
    private void executeVoteAction(String vote) {
        switch (vote) {
            case "light":
                Bukkit.getWorlds().get(0).setTime(1000);
                break;
            case "dark":
                Bukkit.getWorlds().get(0).setTime(13000);
                break;
            case "sun":
                Bukkit.getWorlds().get(0).setStorm(false);
                break;
            case "rain":
                Bukkit.getWorlds().get(0).setStorm(true);
                break;
        }
        actionBarUtils.sendPraiseMessage(vote);
        lastSuccessfulPraise.put(vote, System.currentTimeMillis()); // Update last successful praise time
    }

    /**
     * Sends an action bar message with the current vote progress.
     *
     * @param vote The type of vote.
     */
    private void sendActionBarMessage(String vote) {
        int currentVotes = votes.getOrDefault(vote, 0);
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int requiredVotes = (int) Math.ceil(onlinePlayers / 2.0);
        actionBarUtils.sendVoteMessage(vote, currentVotes, requiredVotes);
    }

    /**
     * Resets the votes and sends a vote expired message.
     *
     * @param vote The type of vote that expired.
     */
    private void resetVotes(String vote) {
        votes.clear();
        playerVotes.clear();
        currentVote = "";
        actionBarUtils.sendVoteExpiredMessage(vote);
    }

    /**
     * Starts a task to expire votes after a certain period.
     */
    private void startVoteExpirationTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastVoteTime > 60000 && !votes.isEmpty()) {
                resetVotes(currentVote);
                lastVoteTime = currentTime; // Reset the last vote time after expiration
            }
        }, 0L, 1200L); // Runs every 60 seconds (1200 ticks)
    }
}
package com.gijinkakunweathertime;

import com.google.inject.Inject;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Coordinates the player voting workflow for time and weather changes, including
 * cooldown tracking, world updates, and action bar announcements.
 */
public class VoteManager {

    private final Map<PraiseType, Integer> voteCounts;
    private final Map<UUID, PraiseType> playerVoteSelections;
    private final Map<UUID, Long> playerCooldowns; // Track last praise time for each player
    private long cooldownTime; // Cooldown time in milliseconds
    private long voteDurationMillis;
    private long lastVoteTime;
    private PraiseType currentVote;
    private final ActionBarUtils actionBarMessenger;
    private final JavaPlugin plugin;
    private final Map<PraiseType, Long> lastSuccessfulPraise; // Track last successful praise time for each type
    private final MessageService messageService;
    private int requiredPercentage;
    private int minimumPlayers;
    private Set<String> enabledWorlds;
    private Map<String, String> disabledWorlds;

    /**
     * @param actionBarUtils helper that formats action bar updates
     * @param plugin         plugin used for scheduling and logging
     * @param config         plugin configuration containing cooldown durations
     * @param messageService message formatter for chat feedback
     */
    @Inject
    public VoteManager(ActionBarUtils actionBarUtils, JavaPlugin plugin, FileConfiguration config, MessageService messageService) {
        this.voteCounts = new EnumMap<>(PraiseType.class);
        this.playerVoteSelections = new ConcurrentHashMap<>();
        this.playerCooldowns = new ConcurrentHashMap<>();
        this.lastVoteTime = 0;
        this.currentVote = null;
        this.actionBarMessenger = actionBarUtils;
        this.plugin = plugin;
        this.lastSuccessfulPraise = new EnumMap<>(PraiseType.class); // Initialize the map to track last successful praise time
        this.messageService = messageService;
        applyConfiguration(config);
        startVoteExpirationTask();
    }

    private void applyConfiguration(FileConfiguration config) {
        this.cooldownTime = Math.max(0, config.getInt("cooldown_time", 10)) * 60 * 1000L;
        this.voteDurationMillis = Math.max(1, config.getInt("vote_duration", 1)) * 60 * 1000L;
        this.requiredPercentage = Math.max(1, Math.min(100, config.getInt("vote_thresholds.percentage", 50)));
        this.minimumPlayers = Math.max(1, config.getInt("vote_thresholds.minimum_players", 1));
        this.enabledWorlds = parseWorldList(config.getStringList("worlds.enabled"));
        this.disabledWorlds = parseDisabledWorlds(config.getList("worlds.disabled"));
    }

    static Set<String> parseWorldList(List<String> worldNames) {
        return worldNames.stream()
            .filter(name -> name != null && !name.isBlank())
            .map(name -> name.toLowerCase(Locale.ROOT))
            .collect(Collectors.toSet());
    }

    static Map<String, String> parseDisabledWorlds(List<?> entries) {
        Map<String, String> result = new java.util.HashMap<>();
        if (entries == null) {
            return result;
        }
        for (Object entry : entries) {
            if (entry instanceof String) {
                String worldName = ((String) entry).trim();
                if (!worldName.isEmpty()) {
                    String normalizedKey = worldName.toLowerCase(Locale.ROOT);
                    result.put(normalizedKey, worldName);
                }
            } else if (entry instanceof Map) {
                Map<?, ?> section = (Map<?, ?>) entry;
                Object idObj = section.get("id");
                if (idObj instanceof String) {
                    String worldId = ((String) idObj).trim();
                    if (!worldId.isEmpty()) {
                        String label = null;
                        Object nameObj = section.get("name");
                        if (nameObj instanceof String) {
                            String displayName = ((String) nameObj).trim();
                            if (!displayName.isEmpty()) {
                                label = displayName;
                            }
                        }
                        String normalizedKey = worldId.toLowerCase(Locale.ROOT);
                        result.put(normalizedKey, label != null ? label : worldId);
                    }
                }
            }
        }
        return result;
    }

    public synchronized void reloadSettings(FileConfiguration config) {
        applyConfiguration(config);
        plugin.getLogger().info("VoteManager settings reloaded from configuration file.");
    }

    /**
     * Adds or removes a vote from a player, enforcing cooldowns and announcing progress.
     *
     * @param player player casting or retracting their vote
     * @param vote   praise option being voted on
     */
    public synchronized void addVote(Player player, PraiseType vote, boolean bypassCooldowns) {
        long currentTime = System.currentTimeMillis();

        // Logging vote attempt
        plugin.getLogger().info(player.getName() + " is attempting to vote for " + vote.getCommandArgument());

        if (!isWorldEnabled(player.getWorld())) {
            return;
        }

        UUID playerId = player.getUniqueId();
        PraiseType previousVote = playerVoteSelections.get(playerId);
        if (previousVote != null) {
            if (previousVote == vote) {
                removePlayerVote(playerId, vote);
                player.sendMessage(messageService.format("vote_removed",
                    Map.of("vote", vote.getDisplayName())));
                return;
            }
            decrementVote(previousVote);
        }

        // Check if the vote type has already been successfully praised recently
        if (!bypassCooldowns && lastSuccessfulPraise.containsKey(vote)
            && (currentTime - lastSuccessfulPraise.get(vote)) < cooldownTime) {
            player.sendMessage(messageService.format("already_praised",
                Map.of("vote", vote.getDisplayName())));
            return;
        }

        if (voteCounts.isEmpty()) {
            lastVoteTime = currentTime;
            currentVote = vote;
            plugin.getLogger().info("Starting new vote for " + vote.getCommandArgument());
        }

        playerVoteSelections.put(playerId, vote);
        voteCounts.put(vote, voteCounts.getOrDefault(vote, 0) + 1);
        playerCooldowns.put(playerId, currentTime); // Update last praise time
        sendActionBarMessage(vote);
        checkVotes(vote);
    }

    /**
     * Ensures players can only simultaneously vote for a single praise type.
     *
     * @param vote The vote to check.
     * @return true if the current vote matches, false otherwise.
     */
    public synchronized boolean isCurrentVote(PraiseType vote) {
        return currentVote == null || currentVote == vote;
    }

    /**
     * @return {@code true} when a vote is already underway and collecting praises.
     */
    public synchronized boolean hasActiveVote() {
        return currentVote != null && !voteCounts.isEmpty();
    }

    /**
     * Looks up the most recent timestamp when a player praised an element.
     *
     * @param playerUUID The player's UUID.
     * @return epoch milliseconds stamped when the player last praised
     */
    public long getLastPraiseTime(UUID playerUUID) {
        return playerCooldowns.getOrDefault(playerUUID, 0L);
    }

    /**
     * @return The cooldown time in milliseconds configured for subsequent votes.
     */
    public long getCooldownTime() {
        return cooldownTime;
    }

    /**
     * Evaluates the current tally for a praise type and runs its action when the threshold is met.
     *
     * @param vote praise type currently being tallied
     */
    private synchronized void checkVotes(PraiseType vote) {
        int onlinePlayers = countEligiblePlayers();
        int requiredVotes = calculateRequiredVotes(onlinePlayers);
        if (voteCounts.getOrDefault(vote, 0) >= requiredVotes) {
            executeVoteAction(vote);
            clearVotes(false);
            plugin.getLogger().info("Vote for " + vote.getCommandArgument() + " passed.");
        } else {
            plugin.getLogger().info("Current votes for " + vote.getCommandArgument() + ": " + voteCounts.getOrDefault(vote, 0) + "/" + requiredVotes);
        }
    }

    /**
     * Sends an action bar message with the current vote progress.
     *
     * @param vote The type of vote.
     */
    private void sendActionBarMessage(PraiseType vote) {
        int currentVotes = voteCounts.getOrDefault(vote, 0);
        int onlinePlayers = countEligiblePlayers();
        int requiredVotes = calculateRequiredVotes(onlinePlayers);
        actionBarMessenger.sendVoteMessage(vote, currentVotes, requiredVotes);
    }

    /**
     * Resets the votes and sends a vote expired message.
     *
     * @param vote The type of vote that expired.
     */
    private void resetVotes(PraiseType vote) {
        clearVotes(true);
        actionBarMessenger.sendVoteExpiredMessage(vote);
    }

    /**
     * Starts a repeating task that checks every second whether the active vote has timed out.
     */
    private void startVoteExpirationTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            synchronized (this) {
                if (currentVote != null && !voteCounts.isEmpty()) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastVoteTime >= voteDurationMillis) {
                        resetVotes(currentVote);
                    }
                }
            }
        }, 20L, 20L); // Check every second
    }

    /**
     * Clears all tracked votes and optionally prunes cooldown entries when a vote expires.
     *
     * @param resetCooldowns whether to remove player cooldowns when the vote is wiped
     */
    private void clearVotes(boolean resetCooldowns) {
        if (resetCooldowns) {
            Set<UUID> participants = new HashSet<>(playerVoteSelections.keySet());
            for (UUID uuid : participants) {
                playerCooldowns.remove(uuid);
            }
        }
        voteCounts.clear();
        playerVoteSelections.clear();
        currentVote = null;
        lastVoteTime = 0;
    }

    /**
     * Removes an existing vote for a player, undoing the associated tally contribution.
     *
     * @param playerId player whose vote should be withdrawn
     * @param vote     praise type the player previously selected
     */
    private void removePlayerVote(UUID playerId, PraiseType vote) {
        decrementVote(vote);
        playerVoteSelections.remove(playerId);
        if (voteCounts.isEmpty()) {
            currentVote = null;
            lastVoteTime = 0;
        }
    }

    /**
     * Decrements the counter for a specific praise type, removing the entry when it drops to zero.
     *
     * @param vote praise type whose tally should be reduced
     */
    private void decrementVote(PraiseType vote) {
        voteCounts.computeIfPresent(vote, (key, count) -> count <= 1 ? null : count - 1);
    }

    public synchronized void removeParticipation(UUID playerId) {
        PraiseType previousVote = playerVoteSelections.get(playerId);
        if (previousVote != null) {
            removePlayerVote(playerId, previousVote);
        }
        playerCooldowns.remove(playerId);
    }

    public synchronized boolean isWorldEnabled(World world) {
        String name = world.getName().toLowerCase(Locale.ROOT);
        if (disabledWorlds.containsKey(name)) {
            return false;
        }
        if (!enabledWorlds.isEmpty() && !enabledWorlds.contains(name)) {
            return false;
        }
        return true;
    }

    public synchronized String getDisplayWorldName(World world) {
        String key = world.getName().toLowerCase(Locale.ROOT);
        return disabledWorlds.getOrDefault(key, world.getName());
    }

    public synchronized void forceApply(PraiseType vote) {
        executeVoteAction(vote);
        clearVotes(false);
        plugin.getLogger().info("Vote forcibly applied: " + vote.getCommandArgument());
    }

    private int calculateRequiredVotes(int eligiblePlayers) {
        int percentRequirement = (int) Math.ceil(eligiblePlayers * (requiredPercentage / 100.0));
        return Math.max(minimumPlayers, Math.max(1, percentRequirement));
    }

    private int countEligiblePlayers() {
        return (int) Bukkit.getOnlinePlayers().stream()
            .filter(player -> isWorldEnabled(player.getWorld()))
            .count();
    }

    private List<World> resolveTargetWorlds() {
        List<World> worlds = Bukkit.getWorlds().stream()
            .filter(this::isWorldEnabled)
            .collect(Collectors.toList());
        if (worlds.isEmpty()) {
            return new ArrayList<>(Bukkit.getWorlds());
        }
        return worlds;
    }

    private void executeVoteAction(PraiseType vote) {
        vote.applyToWorlds(resolveTargetWorlds());
        actionBarMessenger.sendPraiseMessage(vote);
        lastSuccessfulPraise.put(vote, System.currentTimeMillis());
    }
}

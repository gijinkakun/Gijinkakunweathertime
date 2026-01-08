package com.gijinkakunweathertime;

import com.google.inject.Inject;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Centralizes how action bar notifications are formatted and delivered to players.
 * Every helper method keeps the wording consistent with the templates supplied by {@link MessageService}.
 */
public class ActionBarUtils {

    private final MessageService messageService;
    private volatile Method cachedStringActionBar;
    private volatile Predicate<World> recipientFilter = world -> true;

    /**
     * Creates a messenger tied to the shared {@link MessageService} so all outgoing text
     * can be formatted with the configured templates and locale settings.
     *
     * @param messageService service used to expand the action bar message templates
     */
    @Inject
    public ActionBarUtils(MessageService messageService) {
        this.messageService = messageService;
    }

    /**
     * Limits which worlds receive action bar broadcasts.
     *
     * @param filter predicate determining if a player's world should receive messages
     */
    public void setRecipientFilter(Predicate<World> filter) {
        this.recipientFilter = filter != null ? filter : world -> true;
    }

    /**
     * Broadcasts a raw action bar line to every online player using the most modern API available,
     * falling back to legacy behavior when needed for compatibility.
     *
     * @param actionBarMessage fully formatted message text using Spigot legacy color codes
     */
    public void sendActionBarToAllPlayers(String actionBarMessage) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (recipientFilter.test(player.getWorld())) {
                sendActionBarToPlayer(player, actionBarMessage);
            }
        }
    }

    /**
     * Informs a single player how many minutes remain before they can attempt another vote.
     *
     * @param player      player waiting on cooldown
     * @param minutesLeft number of minutes remaining before the command is available
     */
    public void sendCooldownMessage(Player player, long minutesLeft) {
        String cooldownMessage = messageService.format("cooldown", Map.of("minutes", String.valueOf(minutesLeft)));
        sendActionBarToPlayer(player, cooldownMessage);
    }

    /**
     * Provides a live tally of the current vote so players know how close it is to completion.
     *
     * @param vote          vote category being tracked
     * @param currentVotes  number of supportive votes already submitted
     * @param requiredVotes votes required for success
     */
    public void sendVoteMessage(PraiseType vote, int currentVotes, int requiredVotes) {
        String voteProgressMessage = messageService.format("vote_progress", Map.of(
            "current", String.valueOf(currentVotes),
            "required", String.valueOf(requiredVotes),
            "vote", vote.getDisplayName()
        ));
        sendActionBarToAllPlayers(voteProgressMessage);
    }

    /**
     * Announces that a vote has succeeded and the reward is about to be applied.
     *
     * @param vote vote category that reached the required threshold
     */
    public void sendPraiseMessage(PraiseType vote) {
        String praiseMessage = messageService.format("vote_success", Map.of("vote", vote.getDisplayName()));
        sendActionBarToAllPlayers(praiseMessage);
    }

    /**
     * Notifies players that the vote failed to gather enough support before expiring.
     *
     * @param vote vote category that timed out
     */
    public void sendVoteExpiredMessage(PraiseType vote) {
        String expiredMessage = messageService.format("vote_expired", Map.of("vote", vote.getDisplayName()));
        sendActionBarToAllPlayers(expiredMessage);
    }

    private void sendActionBarToPlayer(Player player, String message) {
        Method stringActionBar = resolveStringActionBar(player);
        if (stringActionBar != null) {
            try {
                stringActionBar.invoke(player, message);
                return;
            } catch (ReflectiveOperationException ignored) {
                // fall through to legacy fallback
            }
        }
        sendLegacyActionBar(player, message);
    }

    private Method resolveStringActionBar(Player player) {
        Method existing = cachedStringActionBar;
        if (existing != null) {
            return existing;
        }
        try {
            Method method = player.getClass().getMethod("sendActionBar", String.class);
            cachedStringActionBar = method;
            return method;
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    private void sendLegacyActionBar(Player player, String message) {
        BaseComponent[] components = TextComponent.fromLegacyText(message);
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
    }
}

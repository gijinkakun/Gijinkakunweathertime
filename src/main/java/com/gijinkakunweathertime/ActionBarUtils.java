package com.gijinkakunweathertime;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.google.inject.Inject;
import java.util.Map;

/**
 * Centralizes how action bar notifications are formatted and delivered to players.
 * Every helper method keeps the wording consistent with the templates supplied by {@link MessageService}.
 */
public class ActionBarUtils {

    private final MessageService messageService;

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
     * Broadcasts a raw action bar line to every online player using the Spigot API.
     *
     * @param actionBarMessage fully formatted message text using Spigot legacy color codes
     */
    public void sendActionBarToAllPlayers(String actionBarMessage) {
        BaseComponent[] components = TextComponent.fromLegacyText(actionBarMessage);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, components);
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
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(cooldownMessage));
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
}

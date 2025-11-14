package com.gijinkakunweathertime;

import com.google.inject.Inject;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Locale;
import java.util.Map;

/**
 * Executes the {@code /praise} command, validating the syntax, permissions, cooldown
 * state, and whether the requested praise matches the active vote.
 */
public class PraiseCommandHandler implements CommandExecutor {

    private static final String BASE_PERMISSION = "gijinkakunweathertime.praise";
    private static final String BYPASS_COOLDOWN_PERMISSION = "gijinkakunweathertime.bypasscooldown";
    private static final String FORCE_PERMISSION = "gijinkakunweathertime.force";
    private static final String RELOAD_PERMISSION = "gijinkakunweathertime.reload";

    private final VoteManager voteManager;
    private final ActionBarUtils actionBarUtils;
    private final MessageService messageService;
    private final GijinkakunWeatherTime plugin;

    /**
     * @param voteManager     coordinator that tracks active votes and cooldown timers
     * @param actionBarUtils  action bar helper for communicating cooldown notices
     * @param messageService  message formatter used for localized responses
     */
    @Inject
    public PraiseCommandHandler(VoteManager voteManager, ActionBarUtils actionBarUtils,
                               MessageService messageService, GijinkakunWeatherTime plugin) {
        this.voteManager = voteManager;
        this.actionBarUtils = actionBarUtils;
        this.messageService = messageService;
        this.plugin = plugin;
    }

    /**
     * Validates player usage of {@code /praise the <element>} and routes valid invocations
     * to the {@link VoteManager}. Sends context-aware feedback for permission failures,
     * cooldowns, or invalid arguments.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(getColoredMessage("usage"));
            return true;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        if ("reload".equals(subCommand)) {
            handleReload(sender);
            return true;
        }

        if ("force".equals(subCommand)) {
            handleForce(sender, args);
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can praise the elements.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 2 || !"the".equalsIgnoreCase(args[0])) {
            player.sendMessage(getColoredMessage("usage"));
            return true;
        }
        PraiseType praiseType = PraiseType.fromCommandArgument(args[1]).orElse(null);
        if (praiseType == null) {
            player.sendMessage(getColoredMessage("invalid_vote"));
            return true;
        }

        if (!player.hasPermission(BASE_PERMISSION)) {
            player.sendMessage(messageService.format("no_permission",
                Map.of("vote", praiseType.getDisplayName())));
            return true;
        }

        if (!voteManager.isWorldEnabled(player.getWorld())) {
            player.sendMessage(messageService.format("world_disabled",
                Map.of("world", voteManager.getDisplayWorldName(player.getWorld()))));
            return true;
        }

        long currentTime = System.currentTimeMillis();
        long lastPraiseTime = voteManager.getLastPraiseTime(player.getUniqueId());
        long timeSinceLastPraise = currentTime - lastPraiseTime;
        boolean bypassCooldown = player.hasPermission(BYPASS_COOLDOWN_PERMISSION);
        if (!bypassCooldown && timeSinceLastPraise < voteManager.getCooldownTime()) {
            long timeLeft = (voteManager.getCooldownTime() - timeSinceLastPraise) / 1000;
            long minutesLeft = (timeLeft + 59) / 60;
            actionBarUtils.sendCooldownMessage(player, minutesLeft);
            return true;
        }

        if (!voteManager.isCurrentVote(praiseType)) {
            player.sendMessage(getColoredMessage("vote_in_progress"));
            return true;
        }

        voteManager.addVote(player, praiseType, bypassCooldown);
        return true;
    }

    /**
     * Fetches a localized message and applies Minecraft color codes.
     *
     * @param messageKey configuration key inside {@code messages.*}
     * @return message ready for display in chat
     */
    private String getColoredMessage(String messageKey) {
        return messageService.getMessage(messageKey);
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(RELOAD_PERMISSION)) {
            sender.sendMessage(getColoredMessage("no_permission_command"));
            return;
        }
        boolean success = plugin.reloadPluginSettings();
        sender.sendMessage(getColoredMessage(success ? "reload_success" : "reload_failed"));
    }

    private void handleForce(CommandSender sender, String[] args) {
        if (!sender.hasPermission(FORCE_PERMISSION)) {
            sender.sendMessage(getColoredMessage("no_permission_command"));
            return;
        }
        if (args.length < 2) {
            sender.sendMessage(getColoredMessage("force_usage"));
            return;
        }
        PraiseType praiseType = PraiseType.fromCommandArgument(args[1]).orElse(null);
        if (praiseType == null) {
            sender.sendMessage(getColoredMessage("force_usage"));
            return;
        }
        voteManager.forceApply(praiseType);
        sender.sendMessage(messageService.format("force_applied", Map.of("vote", praiseType.getDisplayName())));
    }
}

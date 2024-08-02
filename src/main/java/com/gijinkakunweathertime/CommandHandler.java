package com.gijinkakunweathertime;

import com.google.inject.Inject;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/**
 * Handles the /praise command execution.
 */
public class CommandHandler implements CommandExecutor {

    private final VoteManager voteManager;
    private final ActionBarUtils actionBarUtils;
    private final FileConfiguration config;

    /**
     * Constructs a CommandHandler instance.
     *
     * @param voteManager    The VoteManager instance.
     * @param actionBarUtils The ActionBarUtils instance.
     * @param config         The configuration file.
     */
    @Inject
    public CommandHandler(VoteManager voteManager, ActionBarUtils actionBarUtils, FileConfiguration config) {
        this.voteManager = voteManager;
        this.actionBarUtils = actionBarUtils;
        this.config = config;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length < 2 || !"the".equalsIgnoreCase(args[0])) {
                player.sendMessage(getColoredMessage("usage"));
                return true;
            }
            String action = args[1].toLowerCase();

            if (!player.hasPermission("gijinkakunweathertime.praise") && !player.hasPermission("gijinkakunweathertime.praise." + action)) {
                player.sendMessage(ChatColor.RED + "You do not have permission to praise the " + action + ".");
                return true;
            }

            long currentTime = System.currentTimeMillis();
            long lastPraiseTime = voteManager.getLastPraiseTime(player.getUniqueId());
            long timeSinceLastPraise = currentTime - lastPraiseTime;
            if (timeSinceLastPraise < voteManager.getCooldownTime()) {
                long timeLeft = (voteManager.getCooldownTime() - timeSinceLastPraise) / 1000;
                long minutesLeft = (timeLeft + 59) / 60;
                actionBarUtils.sendCooldownMessage(player, minutesLeft);
                return true;
            }

            if (!voteManager.isCurrentVote(action)) {
                player.sendMessage(getColoredMessage("vote_in_progress"));
                return true;
            }

            voteManager.addVote(player, action);
            return true;
        }
        return false;
    }

    /**
     * Retrieves a colored message from the configuration.
     *
     * @param messageKey The key of the message to retrieve.
     * @return The colored message.
     */
    private String getColoredMessage(String messageKey) {
        String message = config.getString("messages." + messageKey);
        return ChatColor.RED + message;
    }
}
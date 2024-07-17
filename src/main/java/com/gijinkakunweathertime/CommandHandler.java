package com.gijinkakunweathertime;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

    private final VoteManager voteManager;

    public CommandHandler(VoteManager voteManager) {
        this.voteManager = voteManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (args.length < 2 || !"the".equalsIgnoreCase(args[0])) {
                player.sendMessage(ChatColor.RED + "Please use one of the following commands: /praise the light, /praise the dark, /praise the sun, /praise the rain.");
                return true;
            }
            String action = args[1].toLowerCase();
            
            // Check cooldown
            long currentTime = System.currentTimeMillis();
            long lastPraiseTime = voteManager.getLastPraiseTime(player.getUniqueId());
            long timeSinceLastPraise = currentTime - lastPraiseTime;
            if (timeSinceLastPraise < VoteManager.COOLDOWN_TIME) {
                long timeLeft = (VoteManager.COOLDOWN_TIME - timeSinceLastPraise) / 1000;
                long minutesLeft = timeLeft / 60;
                ActionBarUtils.sendCooldownMessage(player, minutesLeft);
                return true; // Exit early to prevent vote from being added
            }

            switch (action) {
                case "light":
                    voteManager.addVote(player, "Praise the light");
                    break;
                case "night":
                    voteManager.addVote(player, "Praise the dark");
                    break;
                case "sun":
                    voteManager.addVote(player, "Praise the sun");
                    break;
                case "rain":
                    voteManager.addVote(player, "Praise the rain");
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Invalid command. Please use: /praise the light, /praise the dark, /praise the sun, /praise the rain.");
                    return true;
            }
            return true;
        }
        return false;
    }
}

package com.gijinkakunweathertime;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class GijinkakunWeatherTime extends JavaPlugin {
    
    private static GijinkakunWeatherTime instance;
    private VoteManager voteManager;

    @Override
    public void onEnable() {
        instance = this;
        voteManager = new VoteManager();
        CommandHandler commandHandler = new CommandHandler(voteManager);
        this.getCommand("praise").setExecutor(commandHandler);
        this.getCommand("praise").setTabCompleter(new PraiseCommandCompleter());
        getServer().getPluginManager().registerEvents(new PlayerChatListener(voteManager), this);

        logToConsole("Gijinkakun Weather and Time has been enabled!", ChatColor.GREEN);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        logToConsole("Gijinkakun Weather and Time has been disabled!", ChatColor.RED);
    }

    private void logToConsole(String message, ChatColor color) {
        getServer().getConsoleSender().sendMessage(color + message);
    }

    public static GijinkakunWeatherTime getInstance() {
        return instance;
    }

    public VoteManager getVoteManager() {
        return voteManager;
    }
}

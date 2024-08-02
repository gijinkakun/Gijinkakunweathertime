package com.gijinkakunweathertime;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Main plugin class for Gijinkakun Weather and Time.
 */
public class GijinkakunWeatherTime extends JavaPlugin {

    private Injector injector;

    @Override
    public void onEnable() {
        // Save the default configuration file if it does not exist
        saveDefaultConfig();
        FileConfiguration config = getConfig();  // Load the config
        String language = config.getString("language", "en");
        File languageFile = new File(getDataFolder(), "messages_" + language + ".yml");
        if (!languageFile.exists()) {
            saveResource("messages_" + language + ".yml", false);
        }

        // Logging configuration load
        getLogger().info("Configuration loaded.");

        // Pass the config to the DependencyInjectionModule
        injector = Guice.createInjector(new DependencyInjectionModule(this, config));
        CommandHandler commandHandler = injector.getInstance(CommandHandler.class);
        PlayerChatListener playerChatListener = injector.getInstance(PlayerChatListener.class);

        this.getCommand("praise").setExecutor(commandHandler);
        this.getCommand("praise").setTabCompleter(new PraiseCommandCompleter());
        getServer().getPluginManager().registerEvents(playerChatListener, this);

        logToConsole("Gijinkakun Weather and Time has been enabled!", ChatColor.GREEN);
        getLogger().info("Gijinkakun Weather and Time plugin enabled.");
    }

    @Override
    public void onDisable() {
        logToConsole("Gijinkakun Weather and Time has been disabled!", ChatColor.RED);
        getLogger().info("Gijinkakun Weather and Time plugin disabled.");
    }

    /**
     * Logs a message to the console with the specified color.
     *
     * @param message The message to log.
     * @param color   The color of the message.
     */
    private void logToConsole(String message, ChatColor color) {
        getServer().getConsoleSender().sendMessage(color + message);
    }
}
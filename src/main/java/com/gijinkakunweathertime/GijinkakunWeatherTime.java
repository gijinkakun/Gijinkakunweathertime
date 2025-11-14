package com.gijinkakunweathertime;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

/**
 * Entry point for the Gijinkakun Weather and Time plugin. Responsible for wiring dependencies,
 * registering commands/listeners, and loading localized message bundles.
 */
public class GijinkakunWeatherTime extends JavaPlugin {

    private Injector injector;
    private VoteManager voteManager;
    private MessageService messageService;

    /**
     * Saves configuration defaults, wires up dependencies, and registers the praise command.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();
        FileConfiguration pluginConfig = getConfig();
        FileConfiguration messagesConfig = loadMessages(pluginConfig);

        getLogger().info("Configuration loaded.");

        injector = Guice.createInjector(new DependencyInjectionModule(this, pluginConfig, messagesConfig));
        this.voteManager = injector.getInstance(VoteManager.class);
        this.messageService = injector.getInstance(MessageService.class);
        PraiseCommandHandler praiseCommandHandler = injector.getInstance(PraiseCommandHandler.class);
        PlayerLifecycleListener lifecycleListener = injector.getInstance(PlayerLifecycleListener.class);

        PluginCommand praiseCommand = this.getCommand("praise");
        if (praiseCommand == null) {
            getLogger().severe("Failed to register /praise command. Check plugin.yml configuration.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        praiseCommand.setExecutor(praiseCommandHandler);
        praiseCommand.setTabCompleter(new PraiseCommandCompleter());
        getServer().getPluginManager().registerEvents(lifecycleListener, this);

        logToConsole("Gijinkakun Weather and Time has been enabled!", ChatColor.GREEN);
        getLogger().info("Gijinkakun Weather and Time plugin enabled.");
    }

    /**
     * Announces plugin shutdown to both the console and in-game operators.
     */
    @Override
    public void onDisable() {
        logToConsole("Gijinkakun Weather and Time has been disabled!", ChatColor.RED);
        getLogger().info("Gijinkakun Weather and Time plugin disabled.");
    }

    /**
     * Sends a console message with the provided color while preserving vanilla color codes.
     *
     * @param message text to display in the server console
     * @param color   chat color prefix to include with the message
     */
    private void logToConsole(String message, ChatColor color) {
        getServer().getConsoleSender().sendMessage(color + message);
    }

    /**
     * Loads the localized message file indicated in {@code config.yml}, falling back to English
     * if the file does not exist in either the data folder or plugin resources.
     *
     * @param pluginConfig primary configuration containing the {@code language} setting
     * @return resolved {@link FileConfiguration} with message strings
     */
    private FileConfiguration loadMessages(FileConfiguration pluginConfig) {
        String language = pluginConfig.getString("language", "en");
        File languageFile = new File(getDataFolder(), "messages_" + language + ".yml");
        if (!languageFile.exists()) {
            String resourcePath = "messages_" + language + ".yml";
            if (getResource(resourcePath) != null) {
                saveResource(resourcePath, false);
            } else {
                getLogger().warning("Language file " + resourcePath + " not found. Falling back to English.");
                languageFile = new File(getDataFolder(), "messages_en.yml");
                if (!languageFile.exists() && getResource("messages_en.yml") != null) {
                    saveResource("messages_en.yml", false);
                }
            }
        }
        return YamlConfiguration.loadConfiguration(languageFile);
    }

    /**
     * Reloads both plugin and message configurations so runtime changes take effect.
     *
     * @return {@code true} when reload succeeds
     */
    public boolean reloadPluginSettings() {
        try {
            reloadConfig();
            FileConfiguration pluginConfig = getConfig();
            FileConfiguration messagesConfig = loadMessages(pluginConfig);
            messageService.reload(messagesConfig, pluginConfig);
            voteManager.reloadSettings(pluginConfig);
            return true;
        } catch (Exception e) {
            getLogger().severe("Failed to reload GijinkakunWeatherTime settings: " + e.getMessage());
            return false;
        }
    }
}

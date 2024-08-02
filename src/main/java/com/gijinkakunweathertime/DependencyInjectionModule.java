package com.gijinkakunweathertime;

import com.google.inject.AbstractModule;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Guice module for dependency injection.
 */
public class DependencyInjectionModule extends AbstractModule {
    private final JavaPlugin plugin;
    private final FileConfiguration config;

    /**
     * Constructs a DependencyInjectionModule instance.
     *
     * @param plugin The JavaPlugin instance.
     * @param config The configuration file.
     */
    public DependencyInjectionModule(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
    }

    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);
        bind(FileConfiguration.class).toInstance(config);
        bind(VoteManager.class).toInstance(new VoteManager(new ActionBarUtils(config), plugin, config));
        bind(CommandHandler.class);
        bind(PlayerChatListener.class);
        bind(ActionBarUtils.class).toInstance(new ActionBarUtils(config));
    }
}

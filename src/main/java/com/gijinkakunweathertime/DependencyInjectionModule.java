package com.gijinkakunweathertime;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Guice module that wires plugin-specific services so shared instances can be injected everywhere.
 */
public class DependencyInjectionModule extends AbstractModule {
    private final JavaPlugin plugin;
    private final FileConfiguration config;
    private final FileConfiguration messageConfig;

    /**
     * @param plugin        active plugin reference passed into injected services
     * @param config        primary plugin configuration
     * @param messageConfig localized message configuration used by {@link MessageService}
     */
    public DependencyInjectionModule(JavaPlugin plugin, FileConfiguration config, FileConfiguration messageConfig) {
        this.plugin = plugin;
        this.config = config;
        this.messageConfig = messageConfig;
    }

    /**
     * Registers bindings for core utilities, ensuring singletons for stateful services.
     */
    @Override
    protected void configure() {
        bind(JavaPlugin.class).toInstance(plugin);
        if (plugin instanceof GijinkakunWeatherTime) {
            bind(GijinkakunWeatherTime.class).toInstance((GijinkakunWeatherTime) plugin);
        }
        bind(FileConfiguration.class).toInstance(config);
        bind(MessageService.class).toInstance(new MessageService(messageConfig, config, plugin.getLogger()));
        bind(ActionBarUtils.class).in(Scopes.SINGLETON);
        bind(VoteManager.class).in(Scopes.SINGLETON);
        bind(PraiseCommandHandler.class).in(Scopes.SINGLETON);
        bind(PlayerLifecycleListener.class).in(Scopes.SINGLETON);
    }
}

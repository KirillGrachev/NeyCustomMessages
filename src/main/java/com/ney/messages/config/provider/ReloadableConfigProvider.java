package com.ney.messages.config.provider;

import com.ney.messages.config.MainConfig;
import com.ney.messages.config.loader.ConfigLoader;
import com.ney.messages.config.parser.ConfigParser;
import com.ney.messages.config.validation.ConfigValidator;
import com.ney.messages.config.validation.exceptions.ConfigValidationException;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

public class ReloadableConfigProvider implements ConfigProvider {

    private final ConfigLoader configLoader;
    private final ConfigParser configParser;
    private final ConfigValidator configValidator;
    private final JavaPlugin plugin;

    private final AtomicReference<MainConfig> currentConfig = new AtomicReference<>();

    public ReloadableConfigProvider(ConfigLoader configLoader,
                                    ConfigParser configParser,
                                    JavaPlugin plugin) {

        this.configLoader = configLoader;
        this.configParser = configParser;
        this.configValidator = new ConfigValidator();
        this.plugin = plugin;

    }

    @Override
    public MainConfig getConfig() {
        return currentConfig.get();
    }

    @Override
    public synchronized void reload() throws Exception {

        try {

            var rawConfig = configLoader.load();

            MainConfig parsedConfig = configParser.parse(rawConfig);

            configValidator.validate(parsedConfig);
            currentConfig.set(parsedConfig);

            plugin.getLogger().info("Configuration successfully reloaded and validated.");

        } catch (ConfigValidationException e) {

            plugin.getLogger().log(Level.SEVERE, "Configuration validation failed:");
            for (String error : e.getErrors()) plugin.getLogger().severe(" - " + error);

            throw e;

        } catch (Exception e) {

            plugin.getLogger().log(Level.SEVERE, "Failed to reload configuration", e);
            throw e;

        }
    }
}
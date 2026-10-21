package eu.neydev.custommessages.config.provider;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.config.MainConfig;
import eu.neydev.custommessages.config.loader.ConfigLoader;
import eu.neydev.custommessages.config.parser.ConfigParser;
import eu.neydev.custommessages.config.validation.ConfigValidator;
import eu.neydev.custommessages.config.validation.exceptions.ConfigValidationException;
import eu.neydev.custommessages.util.LoggerHelper;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicReference;

public class ReloadableConfigProvider implements ConfigProvider {

    private final ConfigLoader configLoader;
    private final ConfigParser configParser;
    private final ConfigValidator configValidator;

    private final LoggerHelper loggerHelper;

    private final AtomicReference<MainConfig> currentConfig = new AtomicReference<>();

    public ReloadableConfigProvider(ConfigLoader configLoader,
                                    ConfigParser configParser,
                                    @NotNull NeyCustomMessages plugin) {
        this.configLoader = configLoader;
        this.configParser = configParser;
        this.configValidator = new ConfigValidator();
        this.loggerHelper = plugin.getLoggerHelper();
    }

    @Override
    public MainConfig getConfig() {
        return currentConfig.get();
    }

    @Override
    public synchronized void reload() {

        try {

            FileConfiguration rawConfig = configLoader.load();
            MainConfig parsedConfig = configParser.parse(rawConfig);

            configValidator.validate(parsedConfig);
            currentConfig.set(parsedConfig);

            loggerHelper.info("Config", "Configuration successfully reloaded and validated.");

        } catch (ConfigValidationException e) {

            loggerHelper.error("Config", e, "Configuration validation failed:");
            for (String error : e.getErrors()) {
                loggerHelper.error("Config", e, " - %s", error);
            }

        } catch (Exception e) {
            loggerHelper.error("Config", e,"Failed to reload configuration");
        }
    }
}
package com.ney.messages;

import com.ney.messages.command.CommandManager;
import com.ney.messages.config.loader.yaml.YamlConfigLoader;
import com.ney.messages.config.parser.MainConfigParser;
import com.ney.messages.config.provider.ReloadableConfigProvider;
import com.ney.messages.lifecycle.PluginLifecycleManager;
import com.ney.messages.util.LoggerHelper;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeyCustomMessages extends JavaPlugin {

    private PluginLifecycleManager lifecycleManager;
    private LoggerHelper loggerHelper;
    private ReloadableConfigProvider configProvider;
    private CommandManager commandManager;

    @Override
    public void onEnable() {

        this.loggerHelper = new LoggerHelper(this);
        this.configProvider = new ReloadableConfigProvider(
                new YamlConfigLoader(this), new MainConfigParser(), this
        );

        try {

            this.saveDefaultConfig();
            this.configProvider.reload();

        } catch (Exception e) {

            this.loggerHelper.error("Config", e, "Failed to load configuration. Disabling plugin.");

            this.getServer().getPluginManager().disablePlugin(this);
            return;

        }

        this.lifecycleManager = new PluginLifecycleManager(this,
                loggerHelper, configProvider);

        this.lifecycleManager.enable();

        this.commandManager = new CommandManager(this);

        this.getCommand("ncm").setExecutor(commandManager);
        this.getCommand("ncm").setTabCompleter(commandManager);

    }

    @Override
    public void onDisable() {
        if (this.lifecycleManager != null) {
            this.lifecycleManager.disable();
        }
    }

    public void reloadPlugin() {
        if (this.lifecycleManager != null) {
            this.lifecycleManager.reload();
        }
    }


    public LoggerHelper getLoggerHelper() {
        return loggerHelper;
    }

    public PluginLifecycleManager getLifecycleManager() {
        return lifecycleManager;
    }

    public ReloadableConfigProvider getConfigProvider() {
        return configProvider;
    }
}
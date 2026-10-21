package eu.neydev.custommessages;

import eu.neydev.custommessages.command.CommandManager;
import eu.neydev.custommessages.config.loader.yaml.YamlConfigLoader;
import eu.neydev.custommessages.config.parser.MainConfigParser;
import eu.neydev.custommessages.config.provider.ReloadableConfigProvider;
import eu.neydev.custommessages.lifecycle.PluginLifecycleManager;
import eu.neydev.custommessages.util.LoggerHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class NeyCustomMessages extends JavaPlugin {

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
package eu.neydev.custommessages;

import eu.neydev.custommessages.command.CommandManager;
import eu.neydev.custommessages.config.ConfigManager;
import eu.neydev.custommessages.lifecycle.PluginLifecycleManager;
import eu.neydev.custommessages.service.ConsoleService;
import eu.neydev.custommessages.util.LoggerHelper;
import org.bukkit.plugin.java.JavaPlugin;

public class NeyCustomMessages extends JavaPlugin {

    private PluginLifecycleManager lifecycleManager;
    private LoggerHelper loggerHelper;
    private ConsoleService consoleService;
    private ConfigManager configManager;
    private CommandManager commandManager;

    @Override
    public void onEnable() {

        this.loggerHelper = new LoggerHelper(this);
        this.consoleService = new ConsoleService(this);
        this.configManager = new ConfigManager(this, consoleService);

        this.lifecycleManager = new PluginLifecycleManager(this,
                loggerHelper, consoleService, configManager);

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

    public ConsoleService getConsoleService() {
        return consoleService;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }
}

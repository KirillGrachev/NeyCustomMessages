package eu.neydev.custommessages;

import eu.neydev.custommessages.command.CommandDispatcher;
import eu.neydev.custommessages.command.CustomMessagesCommand;
import eu.neydev.custommessages.command.CustomMessagesSubCommand;
import eu.neydev.custommessages.command.sub.InfoSubCommand;
import eu.neydev.custommessages.command.sub.ReloadSubCommand;
import eu.neydev.custommessages.config.ConfigManager;
import eu.neydev.custommessages.event.EventDispatcher;
import eu.neydev.custommessages.listener.PlayerDeathListener;
import eu.neydev.custommessages.listener.PlayerJoinListener;
import eu.neydev.custommessages.listener.PlayerQuitListener;
import eu.neydev.custommessages.service.ConsoleService;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Точка входа плагина.
 *
 * Класс не final осознанно (в 1.x он был final): интеграционные тесты
 * MockBukkit грузят плагин через прокси-подкласс, а наследование JavaPlugin
 * в продакшене никто не использует - подклассы создаются только тестовой
 * средой.
 */
public class NeyCustomMessages extends JavaPlugin {

    private static final String COMMAND_CONTROL = "ncm";

    private ConsoleService consoleService;
    private ConfigManager configManager;
    private ServiceContainer services;
    private @Nullable CommandDispatcher commandDispatcher;

    @Override
    public void onEnable() {

        this.consoleService = new ConsoleService(this);
        this.configManager = new ConfigManager(this, consoleService);
        this.services = new ServiceContainer(this, configManager, consoleService);

        new EventDispatcher(this).registerEvents(
                new PlayerJoinListener(configManager, services.getNotifier()),
                new PlayerQuitListener(configManager, services.getNotifier()),
                new PlayerDeathListener(configManager, services.getNotifier()));

        services.getAnnouncementService().start();

        this.commandDispatcher = new CommandDispatcher(this, consoleService);
        commandDispatcher.registerCommand(COMMAND_CONTROL, new CustomMessagesCommand(
                services.getMessageService(),
                services.getPermissionService(),
                buildSubCommands()
        ));

        getLogger().info("NeyCustomMessages started successfully.");

    }

    @Override
    public void onDisable() {

        services.getAnnouncementService().stop();
        services.getBossBarService().removeAll();

        if (commandDispatcher != null) {
            commandDispatcher.unregisterCommand(COMMAND_CONTROL);
        }

        getLogger().info("NeyCustomMessages stopped.");

    }

    /**
     * Собирает подкоманды: команда получает зависимости из контейнера,
     * а не тянет их из плагина сама.
     */
    private @NotNull List<CustomMessagesSubCommand> buildSubCommands() {
        return List.of(
                new ReloadSubCommand(configManager, services.getMessageService()),
                new InfoSubCommand(this, configManager, services.getMessageService())
        );
    }

    public ConsoleService getConsoleService() {
        return consoleService;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ServiceContainer getServices() {
        return services;
    }

}

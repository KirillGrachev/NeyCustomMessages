package eu.neydev.custommessages.lifecycle;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.announcement.AnnouncementSelector;
import eu.neydev.custommessages.announcement.AnnouncementService;
import eu.neydev.custommessages.config.ConfigManager;
import eu.neydev.custommessages.di.ServiceContainer;
import eu.neydev.custommessages.event.EventDispatcher;
import eu.neydev.custommessages.listener.PlayerDeathListener;
import eu.neydev.custommessages.listener.PlayerJoinListener;
import eu.neydev.custommessages.listener.PlayerQuitListener;
import eu.neydev.custommessages.service.BossBarService;
import eu.neydev.custommessages.service.ConsoleService;
import eu.neydev.custommessages.service.Notifier;
import eu.neydev.custommessages.util.LoggerHelper;
import org.jetbrains.annotations.NotNull;

public class PluginLifecycleManager {

    private final NeyCustomMessages plugin;
    private final LoggerHelper loggerHelper;
    private final ConsoleService consoleService;
    private final ConfigManager configManager;

    private final ServiceContainer container;

    public PluginLifecycleManager(NeyCustomMessages plugin,
                                  LoggerHelper loggerHelper,
                                  ConsoleService consoleService,
                                  ConfigManager configManager) {
        this.plugin = plugin;
        this.loggerHelper = loggerHelper;
        this.consoleService = consoleService;
        this.configManager = configManager;
        this.container = new ServiceContainer();
    }

    public void enable() {

        try {

            buildContainer();
            loggerHelper.info("DI", "ServiceContainer built successfully.");

            registerEventListeners();
            container.get(AnnouncementService.class).start();

            loggerHelper.info("Main", "NeyCustomMessages started successfully.");

        } catch (Exception e) {

            loggerHelper.error("Lifecycle", e, "Failed to enable plugin. Disabling...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);

        }

    }

    public void disable() {

        loggerHelper.info("Main", "Starting plugin shutdown...");

        container.get(AnnouncementService.class).stop();

        BossBarService bossBarService = container.get(BossBarService.class);
        if (bossBarService != null) {
            bossBarService.removeAll();
        }

        loggerHelper.info("Main", "NeyCustomMessages stopped.");

    }

    public void reload() {

        loggerHelper.info("Config", "Starting plugin reload...");

        configManager.reload();

        BossBarService bossBarService = container.get(BossBarService.class);
        if (bossBarService != null) {
            bossBarService.removeAll();
        }

        container.get(AnnouncementService.class).restart();

        loggerHelper.info("Config", "Plugin reload completed successfully.");

    }

    private void buildContainer() {

        container.clear();

        BossBarService bossBarService = new BossBarService(plugin);
        Notifier notifier = new Notifier(plugin, bossBarService);

        container.bind(BossBarService.class, bossBarService);
        container.bind(Notifier.class, notifier);
        container.bind(AnnouncementService.class, new AnnouncementService(
                plugin, configManager, consoleService, new AnnouncementSelector(), notifier));

    }

    private void registerEventListeners() {

        Notifier notifier = container.get(Notifier.class);

        EventDispatcher eventDispatcher = new EventDispatcher(plugin);
        eventDispatcher.registerEvents(
                new PlayerDeathListener(plugin, configManager, notifier),
                new PlayerJoinListener(configManager, notifier),
                new PlayerQuitListener(configManager, notifier)
        );

        loggerHelper.info("Events", "Event listeners registered.");

    }

    public @NotNull ServiceContainer getContainer() {
        return container;
    }

}

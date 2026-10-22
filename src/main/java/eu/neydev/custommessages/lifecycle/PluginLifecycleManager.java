package eu.neydev.custommessages.lifecycle;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.config.ConfigManager;
import eu.neydev.custommessages.config.type.ConsoleMessage;
import eu.neydev.custommessages.di.ServiceContainer;
import eu.neydev.custommessages.event.EventDispatcher;
import eu.neydev.custommessages.listener.PlayerDeathListener;
import eu.neydev.custommessages.listener.PlayerJoinListener;
import eu.neydev.custommessages.listener.PlayerQuitListener;
import eu.neydev.custommessages.service.AnnouncementService;
import eu.neydev.custommessages.service.BossBarService;
import eu.neydev.custommessages.service.ConsoleService;
import eu.neydev.custommessages.service.Notifier;
import eu.neydev.custommessages.service.impl.announcement.scheduler.AnnouncementScheduler;
import eu.neydev.custommessages.task.AnnounceTask;
import eu.neydev.custommessages.util.LoggerHelper;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

public class PluginLifecycleManager {

    private final NeyCustomMessages plugin;
    private final LoggerHelper loggerHelper;
    private final ConsoleService consoleService;
    private final ConfigManager configManager;

    private final ServiceContainer container;
    private BukkitTask announceTask;

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
            startAnnouncementTask();

            loggerHelper.info("Main", "NeyCustomMessages started successfully.");

        } catch (Exception e) {

            loggerHelper.error("Lifecycle", e, "Failed to enable plugin. Disabling...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);

        }

    }

    public void disable() {

        loggerHelper.info("Main", "Starting plugin shutdown...");

        stopAnnouncementTask();
        removeAllBossBars();

        loggerHelper.info("Main", "NeyCustomMessages stopped.");

    }

    public void reload() {

        loggerHelper.info("Config", "Starting plugin reload...");

        stopAnnouncementTask();
        removeAllBossBars();

        configManager.reload();

        startAnnouncementTask();
        loggerHelper.info("Config", "Plugin reload completed successfully.");

    }

    private void stopAnnouncementTask() {

        if (announceTask != null) {

            announceTask.cancel();
            announceTask = null;
            loggerHelper.info("Announcements", "Announcement task cancelled.");

        }

    }

    private void removeAllBossBars() {

        BossBarService bossBarService = container.get(BossBarService.class);

        if (bossBarService != null) {
            bossBarService.removeAll();
        }

    }

    private void buildContainer() {

        container.clear();

        BossBarService bossBarService = new BossBarService(plugin);
        Notifier notifier = new Notifier(plugin, bossBarService);

        container.bind(BossBarService.class, bossBarService);
        container.bind(Notifier.class, notifier);
        container.bind(AnnouncementScheduler.class, new AnnouncementScheduler());
        container.bind(AnnouncementService.class, new AnnouncementService(
                configManager, container.get(AnnouncementScheduler.class), notifier));

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

    private void startAnnouncementTask() {

        if (!configManager.isPluginEnabled() || !configManager.areAnnouncementsEnabled()) {
            return;
        }

        if (configManager.getAnnouncements().isEmpty()) {

            consoleService.log(ConsoleMessage.NO_ANNOUNCEMENTS);
            return;

        }

        try {

            AnnouncementService announcementService = container.get(AnnouncementService.class);
            int intervalTicks = configManager.getAnnouncementInterval() * 20;

            announceTask = new AnnounceTask(plugin, announcementService)
                    .runTaskTimer(plugin, 0L, intervalTicks);
            loggerHelper.info("Announcements", "Announcement task started with interval %d seconds.",
                    configManager.getAnnouncementInterval());

        } catch (Exception e) {

            loggerHelper.error("Announcements", e, "Failed to start announcement task.");

        }

    }

    public @NotNull ServiceContainer getContainer() {
        return container;
    }

}

package eu.neydev.custommessages.lifecycle;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.config.ConfigManager;
import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.ConsoleMessage;
import eu.neydev.custommessages.di.ServiceContainer;
import eu.neydev.custommessages.event.EventDispatcher;
import eu.neydev.custommessages.listener.PlayerDeathListener;
import eu.neydev.custommessages.listener.PlayerJoinListener;
import eu.neydev.custommessages.listener.PlayerQuitListener;
import eu.neydev.custommessages.service.ConsoleService;
import eu.neydev.custommessages.service.impl.AnnouncementServiceImpl;
import eu.neydev.custommessages.service.impl.BossBarServiceImpl;
import eu.neydev.custommessages.service.impl.DeathMessageServiceImpl;
import eu.neydev.custommessages.service.impl.JoinMessageServiceImpl;
import eu.neydev.custommessages.service.impl.QuitMessageServiceImpl;
import eu.neydev.custommessages.service.impl.SoundServiceImpl;
import eu.neydev.custommessages.service.impl.TitleServiceImpl;
import eu.neydev.custommessages.service.impl.announcement.broadcast.AnnouncementBroadcaster;
import eu.neydev.custommessages.service.impl.announcement.scheduler.AnnouncementScheduler;
import eu.neydev.custommessages.service.interfaces.IAnnouncementService;
import eu.neydev.custommessages.service.interfaces.IBossBarService;
import eu.neydev.custommessages.service.interfaces.IDeathMessageService;
import eu.neydev.custommessages.service.interfaces.IJoinMessageService;
import eu.neydev.custommessages.service.interfaces.IQuitMessageService;
import eu.neydev.custommessages.service.interfaces.ISoundService;
import eu.neydev.custommessages.service.interfaces.ITitleService;
import eu.neydev.custommessages.service.strategy.BossBarStrategy;
import eu.neydev.custommessages.service.strategy.ChatMessageStrategy;
import eu.neydev.custommessages.service.strategy.NotificationStrategy;
import eu.neydev.custommessages.service.strategy.SoundStrategy;
import eu.neydev.custommessages.service.strategy.TitleStrategy;
import eu.neydev.custommessages.task.AnnounceTask;
import eu.neydev.custommessages.util.LoggerHelper;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

            buildContainer(configManager);
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

        buildContainer(configManager);
        loggerHelper.info("DI", "New ServiceContainer built with updated configuration.");

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

        if (container == null) {
            return;
        }

        try {

            IBossBarService bossBarService = container.get(IBossBarService.class);

            if (bossBarService != null) {

                bossBarService.removeAllBossBars();
                loggerHelper.info("BossBar", "All boss bars removed.");

            }

        } catch (Exception e) {
            loggerHelper.warn("BossBar", "Error removing boss bars during shutdown: %s", e.getMessage());
        }

    }

    private void buildContainer(@NotNull CustomMessagesConfig config) {

        container.clear();

        container.bind(ITitleService.class, new TitleServiceImpl());
        container.bind(ISoundService.class, new SoundServiceImpl());
        container.bind(IBossBarService.class, new BossBarServiceImpl(plugin));

        container.bindMulti(NotificationStrategy.class, new ChatMessageStrategy());
        container.bindMulti(NotificationStrategy.class, new TitleStrategy(container.get(ITitleService.class)));
        container.bindMulti(NotificationStrategy.class, new SoundStrategy(container.get(ISoundService.class)));
        container.bindMulti(NotificationStrategy.class, new BossBarStrategy(container.get(IBossBarService.class)));

        List<NotificationStrategy> strategies = container.getAll(NotificationStrategy.class);

        AnnouncementBroadcaster broadcaster = new AnnouncementBroadcaster(strategies);
        AnnouncementScheduler scheduler = new AnnouncementScheduler();

        container.bind(AnnouncementBroadcaster.class, broadcaster);
        container.bind(AnnouncementScheduler.class, scheduler);

        container.bind(IAnnouncementService.class,
                new AnnouncementServiceImpl(config, scheduler, broadcaster));

        container.bind(IDeathMessageService.class, new DeathMessageServiceImpl(config, strategies,
                plugin));

        container.bind(IJoinMessageService.class, new JoinMessageServiceImpl(config, strategies));
        container.bind(IQuitMessageService.class, new QuitMessageServiceImpl(config, strategies));

    }

    private void registerEventListeners() {

        IDeathMessageService deathService = container.get(IDeathMessageService.class);
        IJoinMessageService joinService = container.get(IJoinMessageService.class);
        IQuitMessageService quitService = container.get(IQuitMessageService.class);

        EventDispatcher eventDispatcher = new EventDispatcher(plugin);
        eventDispatcher.registerEvents(
                new PlayerDeathListener(deathService),
                new PlayerJoinListener(joinService),
                new PlayerQuitListener(quitService)
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

            IAnnouncementService announcementService = container.get(IAnnouncementService.class);
            int intervalTicks = configManager.getAnnouncementInterval() * 20;

            announceTask = new AnnounceTask(plugin, announcementService)
                    .runTaskTimer(plugin, 0L, intervalTicks);
            loggerHelper.info("Announcements", "Announcement task started with interval %d seconds.",
                    configManager.getAnnouncementInterval());

        } catch (Exception e) {

            loggerHelper.error("Announcements", e, "Failed to start announcement task.");

        }

    }

    public ServiceContainer getContainer() {
        return container;
    }

}

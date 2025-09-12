package com.ney.messages.lifecycle;

import com.ney.messages.NeyCustomMessages;
import com.ney.messages.config.MainConfig;
import com.ney.messages.config.provider.ReloadableConfigProvider;
import com.ney.messages.di.ServiceContainer;
import com.ney.messages.event.EventDispatcher;
import com.ney.messages.listener.PlayerDeathListener;
import com.ney.messages.listener.PlayerJoinListener;
import com.ney.messages.listener.PlayerQuitListener;
import com.ney.messages.service.impl.*;
import com.ney.messages.service.impl.announcement.broadcast.AnnouncementBroadcaster;
import com.ney.messages.service.impl.announcement.scheduler.AnnouncementScheduler;
import com.ney.messages.service.interfaces.*;
import com.ney.messages.service.strategy.*;
import com.ney.messages.task.AnnounceTask;
import com.ney.messages.util.LoggerHelper;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginLifecycleManager {

    private final NeyCustomMessages plugin;
    private final LoggerHelper loggerHelper;
    private final ReloadableConfigProvider configProvider;

    private final ServiceContainer container;
    private BukkitTask announceTask;

    public PluginLifecycleManager(NeyCustomMessages plugin,
                                  LoggerHelper loggerHelper,
                                  ReloadableConfigProvider configProvider) {
        this.plugin = plugin;
        this.loggerHelper = loggerHelper;
        this.configProvider = configProvider;
        this.container = new ServiceContainer();
    }

    public void enable() {

        try {

            buildContainer(configProvider.getConfig());
            loggerHelper.info("DI", "ServiceContainer built successfully.");

            registerEventListeners();
            startAnnouncementTask();

            loggerHelper.info("Main", "Plugin enabled successfully!");

        } catch (Exception e) {

            loggerHelper.error("Lifecycle", e, "Failed to enable plugin. Disabling...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);

        }
    }

    public void disable() {

        loggerHelper.info("Main", "Starting plugin shutdown...");

        if (announceTask != null) {

            announceTask.cancel();
            loggerHelper.info("Announcements", "Announcement task cancelled.");

        }

        if (container != null) {

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

        loggerHelper.info("Main", "Plugin disabled.");

    }

    public void reload() {

        loggerHelper.info("Config", "Starting plugin reload...");

        if (announceTask != null) {

            announceTask.cancel();
            announceTask = null;

            loggerHelper.info("Announcements", "Old announcement task cancelled.");

        }

        if (container != null) {

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

        buildContainer(configProvider.getConfig());
        loggerHelper.info("DI", "New ServiceContainer built with updated configuration.");

        startAnnouncementTask();
        loggerHelper.info("Config", "Plugin reload completed successfully.");

    }

    private void buildContainer(@NotNull MainConfig config) {

        container.clear();

        container.bind(ITitleService.class, new TitleServiceImpl());
        container.bind(ISoundService.class, new SoundServiceImpl(plugin));
        container.bind(IBossBarService.class, new BossBarServiceImpl(plugin));

        container.bindMulti(NotificationStrategy.class, new ChatMessageStrategy(loggerHelper));
        container.bindMulti(NotificationStrategy.class, new TitleStrategy(container.get(ITitleService.class)));
        container.bindMulti(NotificationStrategy.class, new SoundStrategy(container.get(ISoundService.class)));
        container.bindMulti(NotificationStrategy.class, new BossBarStrategy(container.get(IBossBarService.class)));

        List<NotificationStrategy> strategies = container.getAll(NotificationStrategy.class);

        AnnouncementBroadcaster broadcaster = new AnnouncementBroadcaster(strategies);
        AnnouncementScheduler scheduler = new AnnouncementScheduler();

        container.bind(AnnouncementBroadcaster.class, broadcaster);
        container.bind(AnnouncementScheduler.class, scheduler);

        container.bind(IAnnouncementService.class,
                new AnnouncementServiceImpl(config.announcements(), scheduler, broadcaster, loggerHelper));

        container.bind(IDeathMessageService.class, new DeathMessageServiceImpl(config.death(), strategies,
                plugin));

        container.bind(IJoinMessageService.class, new JoinMessageServiceImpl(config.join(), strategies));
        container.bind(IQuitMessageService.class, new QuitMessageServiceImpl(config.quit(), strategies));

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

        MainConfig config = configProvider.getConfig();
        if (!config.announcements().enabled()) return;

        try {

            IAnnouncementService announcementService = container.get(IAnnouncementService.class);
            int intervalTicks = config.announcements().interval() * 20;

            announceTask = new AnnounceTask(plugin, announcementService).runTaskTimer(plugin, 0L, intervalTicks);
            loggerHelper.info("Announcements", "Announcement task started with interval %d seconds.",
                    config.announcements().interval());

        } catch (Exception e) {
            loggerHelper.error("Announcements", e, "Failed to start announcement task.");
        }
    }

    public ServiceContainer getContainer() {
        return container;
    }
}
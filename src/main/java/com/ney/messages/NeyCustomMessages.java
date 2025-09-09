package com.ney.messages;

import com.ney.messages.command.CommandManager;
import com.ney.messages.config.MainConfig;
import com.ney.messages.config.loader.yaml.YamlConfigLoader;
import com.ney.messages.config.parser.MainConfigParser;
import com.ney.messages.config.provider.ReloadableConfigProvider;
import com.ney.messages.di.ServiceContainer;
import com.ney.messages.event.EventDispatcher;
import com.ney.messages.listener.PlayerDeathListener;
import com.ney.messages.listener.PlayerJoinListener;
import com.ney.messages.listener.PlayerQuitListener;
import com.ney.messages.service.interfaces.*;
import com.ney.messages.task.AnnounceTask;
import com.ney.messages.util.LoggerHelper;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class NeyCustomMessages extends JavaPlugin {

    private ServiceContainer serviceContainer;
    private BukkitTask announceTask;
    private LoggerHelper loggerHelper;
    private ReloadableConfigProvider configProvider;
    private CommandManager commandManager;

    @Override
    public void onEnable() {

        this.loggerHelper = new LoggerHelper(this);
        this.loggerHelper.info("Main", "Starting plugin initialization...");

        try {

            this.saveDefaultConfig();

            this.configProvider = new ReloadableConfigProvider(new YamlConfigLoader(this),
                    new MainConfigParser(), this);

            this.configProvider.reload();

        } catch (Exception e) {

            this.loggerHelper.error("Config", e, "Failed to load or validate configuration. Disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);

            return;

        }

        try {

            this.serviceContainer = ServiceContainer.build(this, configProvider.getConfig());
            this.loggerHelper.info("DI", "Service container built successfully.");

        } catch (Exception e) {

            this.loggerHelper.error("DI", e, "Failed to initialize services. Disabling plugin.");
            this.getServer().getPluginManager().disablePlugin(this);

            return;

        }

        EventDispatcher eventDispatcher = new EventDispatcher(this);
        eventDispatcher.registerEvents(

                new PlayerDeathListener(serviceContainer.get(IDeathMessageService.class)),
                new PlayerJoinListener(serviceContainer.get(IJoinMessageService.class)),
                new PlayerQuitListener(serviceContainer.get(IQuitMessageService.class))

        );

        this.loggerHelper.info("Events", "Event listeners registered.");

        MainConfig config = configProvider.getConfig();
        if (config.announcements().enabled()) {

            try {

                IAnnouncementService announcementService = serviceContainer.get(IAnnouncementService.class);

                int intervalTicks = config.announcements().interval() * 20;

                this.announceTask = new AnnounceTask(announcementService).runTaskTimer(this, 0L, intervalTicks);
                this.loggerHelper.info("Announcements", "Announcement task started with interval %d seconds.",
                        config.announcements().interval());

            } catch (Exception e) {
                this.loggerHelper.error("Announcements", e, "Failed to start announcement task.");
            }
        }

        this.commandManager = new CommandManager(this);

        this.getCommand("ncm").setExecutor(commandManager);
        this.getCommand("ncm").setTabCompleter(commandManager);

        this.loggerHelper.info("Main", "Plugin enabled successfully!");

    }

    @Override
    public void onDisable() {

        this.loggerHelper.info("Main", "Starting plugin shutdown...");

        if (this.announceTask != null) {

            this.announceTask.cancel();
            this.loggerHelper.info("Announcements", "Announcement task cancelled.");

        }

        if (this.serviceContainer != null) {

            try {

                IBossBarService bossBarService = this.serviceContainer.get(IBossBarService.class);
                if (bossBarService != null) {

                    bossBarService.removeAllBossBars();
                    this.loggerHelper.info("BossBar", "All boss bars removed.");

                }

            } catch (Exception e) {
                this.loggerHelper.warn("BossBar", "Error removing boss bars during shutdown: %s", e.getMessage());
            }

            try {

                this.serviceContainer.close();
                this.loggerHelper.info("DI", "Service container closed.");

            } catch (Exception e) {
                this.loggerHelper.warn("DI", "Error closing service container: %s", e.getMessage());
            }

        }

        this.loggerHelper.info("Main", "Plugin disabled.");

    }

    public void reloadPlugin() {

        try {

            this.configProvider.reload();
            this.loggerHelper.info("Config", "Configuration reloaded and validated.");


            if (this.announceTask != null) {

                this.announceTask.cancel();
                this.announceTask = null;

                this.loggerHelper.info("Announcements", "Old announcement task cancelled.");

            }

            if (this.serviceContainer != null) {

                try {

                    this.serviceContainer.close();
                    this.loggerHelper.info("DI", "Old service container closed.");

                } catch (Exception e) {
                    this.loggerHelper.warn("DI", "Error closing old service container: %s", e.getMessage());
                }

            }

            this.serviceContainer = ServiceContainer.build(this, this.configProvider.getConfig());
            this.loggerHelper.info("DI", "New service container built with updated configuration.");

            MainConfig newConfig = this.configProvider.getConfig();
            if (newConfig.announcements().enabled()) {

                try {

                    IAnnouncementService announcementService = this.serviceContainer.get(IAnnouncementService.class);
                    int intervalTicks = newConfig.announcements().interval() * 20;

                    this.announceTask = new AnnounceTask(announcementService).runTaskTimer(this, 0L, intervalTicks);
                    this.loggerHelper.info("Announcements", "Announcement task restarted with new interval %d seconds.",
                            newConfig.announcements().interval());

                } catch (Exception e) {
                    this.loggerHelper.error("Announcements", e, "Failed to restart announcement task after reload.");
                }
            }

            EventDispatcher eventDispatcher = new EventDispatcher(this);
            eventDispatcher.registerEvents(

                    new PlayerDeathListener(serviceContainer.get(IDeathMessageService.class)),
                    new PlayerJoinListener(serviceContainer.get(IJoinMessageService.class)),
                    new PlayerQuitListener(serviceContainer.get(IQuitMessageService.class))

            );

        } catch (Exception e) {
            this.loggerHelper.error("Config", e, "Failed to reload configuration. Plugin may be in an inconsistent state.");
        }
    }

    public LoggerHelper getLoggerHelper() {
        return loggerHelper;
    }

    public ServiceContainer getServiceContainer() {
        return serviceContainer;
    }

    public ReloadableConfigProvider getConfigProvider() {
        return configProvider;
    }
}
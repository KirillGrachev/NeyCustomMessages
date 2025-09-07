package com.ney.messages;

import com.ney.messages.config.ConfigKeys;
import com.ney.messages.config.ConfigManager;
import com.ney.messages.event.EventDispatcher;
import com.ney.messages.listener.PlayerDeathListener;
import com.ney.messages.listener.PlayerJoinListener;
import com.ney.messages.listener.PlayerQuitListener;
import com.ney.messages.service.ServiceManager;
import com.ney.messages.task.AnnounceTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeyCustomMessages extends JavaPlugin {

    private ServiceManager serviceManager;

    private AnnounceTask announceTask;

    @Override
    public void onEnable() {

        ConfigManager configManager = new ConfigManager(this);

        this.serviceManager = new ServiceManager(this, configManager);
        EventDispatcher eventDispatcher = new EventDispatcher(this);

        eventDispatcher.registerEvents(
                new PlayerDeathListener(serviceManager.getDeathMessageService()),
                new PlayerJoinListener(serviceManager.getJoinMessageService()),
                new PlayerQuitListener(serviceManager.getQuitMessageService())
        );

        if (configManager.getBooleanWithValidation(ConfigKeys.ANNOUNCES_ENABLED, false)) {

            int interval = configManager.getIntWithValidation(ConfigKeys.ANNOUNCES_INTERVAL,
                    60, 0, 100) * 20;

            announceTask = new AnnounceTask(serviceManager.getAnnouncementService());
            announceTask.runTaskTimer(this, 0L, interval);

        }

        getLogger().info("Плагин NeyCustomMessages успешно загружен!");

    }

    @Override
    public void onDisable() {

        if (announceTask != null) {
            announceTask.cancel();
        }

        if (serviceManager != null && serviceManager.getBossBarService() != null) {
            serviceManager.getBossBarService().removeAllBossBars();
        }

        getLogger().info("Плагин NeyCustomMessages выключен!");

    }

    public ServiceManager getServiceManager() {
        return serviceManager;
    }
}
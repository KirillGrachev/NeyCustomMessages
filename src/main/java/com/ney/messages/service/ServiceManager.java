package com.ney.messages.service;

import com.ney.messages.config.ConfigManager;
import com.ney.messages.NeyCustomMessages;
import com.ney.messages.service.announcement.AnnouncementService;
import com.ney.messages.service.event.DeathMessageService;
import com.ney.messages.service.event.JoinMessageService;
import com.ney.messages.service.event.QuitMessageService;

public class ServiceManager {

    private final NeyCustomMessages plugin;
    private final ConfigManager configManager;

    private TitleService titleService;
    private SoundService soundService;
    private BossBarService bossBarService;
    private DeathMessageService deathMessageService;
    private JoinMessageService joinMessageService;
    private QuitMessageService quitMessageService;
    private AnnouncementService announcementService;

    public ServiceManager(NeyCustomMessages plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public TitleService getTitleService() {
        if (titleService == null) {
            titleService = new TitleService();
        }
        return titleService;
    }

    public SoundService getSoundService() {
        if (soundService == null) {
            soundService = new SoundService(plugin);
        }
        return soundService;
    }

    public BossBarService getBossBarService() {
        if (bossBarService == null) {
            bossBarService = new BossBarService(plugin);
        }
        return bossBarService;
    }

    public DeathMessageService getDeathMessageService() {
        if (deathMessageService == null) {
            deathMessageService = new DeathMessageService(plugin,
                    configManager, getTitleService(), getSoundService());
        }
        return deathMessageService;
    }

    public JoinMessageService getJoinMessageService() {
        if (joinMessageService == null) {
            joinMessageService = new JoinMessageService(configManager,
                    getTitleService(), getSoundService());
        }
        return joinMessageService;
    }

    public QuitMessageService getQuitMessageService() {
        if (quitMessageService == null) {
            quitMessageService = new QuitMessageService(configManager,
                    getTitleService(), getSoundService());
        }
        return quitMessageService;
    }

    public AnnouncementService getAnnouncementService() {
        if (announcementService == null) {
            announcementService = new AnnouncementService(configManager,
                    getTitleService(), getSoundService(), getBossBarService());
        }
        return announcementService;
    }
}
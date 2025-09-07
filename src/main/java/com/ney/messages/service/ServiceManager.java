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

    public ServiceManager(NeyCustomMessages plugin,
                          ConfigManager configManager) {

        this.plugin = plugin;
        this.configManager = configManager;

        initializeServices();

    }

    private void initializeServices() {

        this.titleService = new TitleService();
        this.soundService = new SoundService(plugin);
        this.bossBarService = new BossBarService(plugin);

        this.deathMessageService = new DeathMessageService(plugin, configManager, titleService, soundService);
        this.joinMessageService = new JoinMessageService(configManager, titleService, soundService);
        this.quitMessageService = new QuitMessageService(configManager, titleService, soundService);
        this.announcementService = new AnnouncementService(configManager, titleService, soundService, bossBarService);

    }

    public TitleService getTitleService() {
        return titleService;
    }

    public SoundService getSoundService() {
        return soundService;
    }

    public BossBarService getBossBarService() {
        return bossBarService;
    }

    public DeathMessageService getDeathMessageService() {
        return deathMessageService;
    }

    public JoinMessageService getJoinMessageService() {
        return joinMessageService;
    }

    public QuitMessageService getQuitMessageService() {
        return quitMessageService;
    }

    public AnnouncementService getAnnouncementService() {
        return announcementService;
    }
}
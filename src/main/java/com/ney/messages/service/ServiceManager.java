package com.ney.messages.service;

import com.ney.messages.config.ConfigManager;
import com.ney.messages.NeyCustomMessages;
import com.ney.messages.service.announcement.AnnouncementService;
import com.ney.messages.service.event.DeathMessageService;
import com.ney.messages.service.event.JoinMessageService;
import com.ney.messages.service.event.QuitMessageService;

import com.ney.messages.util.Lazy;

public class ServiceManager {

    private final Lazy<TitleService> titleService;
    private final Lazy<SoundService> soundService;
    private final Lazy<BossBarService> bossBarService;
    private final Lazy<DeathMessageService> deathMessageService;
    private final Lazy<JoinMessageService> joinMessageService;
    private final Lazy<QuitMessageService> quitMessageService;
    private final Lazy<AnnouncementService> announcementService;

    public ServiceManager(NeyCustomMessages plugin, ConfigManager configManager) {

        this.titleService = new Lazy<>(TitleService::new);
        this.soundService = new Lazy<>(() -> new SoundService(plugin));
        this.bossBarService = new Lazy<>(() -> new BossBarService(plugin));

        this.deathMessageService = new Lazy<>(() ->
                new DeathMessageService(plugin, configManager, getTitleService(), getSoundService())
        );

        this.joinMessageService = new Lazy<>(() ->
                new JoinMessageService(configManager, getTitleService(), getSoundService())
        );

        this.quitMessageService = new Lazy<>(() ->
                new QuitMessageService(configManager, getTitleService(), getSoundService())
        );

        this.announcementService = new Lazy<>(() ->
                new AnnouncementService(configManager, getTitleService(), getSoundService(),
                        getBossBarService())
        );

    }

    public TitleService getTitleService() {
        return titleService.get();
    }

    public SoundService getSoundService() {
        return soundService.get();
    }

    public BossBarService getBossBarService() {
        return bossBarService.get();
    }

    public DeathMessageService getDeathMessageService() {
        return deathMessageService.get();
    }

    public JoinMessageService getJoinMessageService() {
        return joinMessageService.get();
    }

    public QuitMessageService getQuitMessageService() {
        return quitMessageService.get();
    }

    public AnnouncementService getAnnouncementService() {
        return announcementService.get();
    }
}
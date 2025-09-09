package com.ney.messages.di;

import com.ney.messages.config.MainConfig;
import com.ney.messages.service.impl.*;
import com.ney.messages.service.interfaces.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceContainer implements AutoCloseable {

    private final Map<Class<?>, Object> services = new HashMap<>();
    private final List<Closeable> closeables = new ArrayList<>();

    private ServiceContainer() {}

    public <T> void register(Class<T> type, T instance) {

        if (services.containsKey(type)) {
            throw new IllegalStateException("Service already registered for type: " + type.getName());
        }

        services.put(type, instance);

        if (instance instanceof Closeable) {
            closeables.add((Closeable) instance);
        }

    }

    @SuppressWarnings("unchecked")
    public <T> T get(Class<T> type) {

        Object service = services.get(type);

        if (service == null) {
            throw new IllegalStateException("Service not registered: " + type.getName());
        }

        return (T) service;

    }

    @Override
    public void close() throws IOException {

        IOException lastException = null;

        for (Closeable closeable : closeables) {

            try {
                closeable.close();
            } catch (IOException e) {

                if (lastException == null) {
                    lastException = e;
                } else {
                    lastException.addSuppressed(e);
                }

            }
        }

        if (lastException != null) {
            throw lastException;
        }

    }

    public static @NotNull ServiceContainer build(JavaPlugin plugin, @NotNull MainConfig config) {

        ServiceContainer container = new ServiceContainer();

        ITitleService titleService = new TitleServiceImpl();
        container.register(ITitleService.class, titleService);

        ISoundService soundService = new SoundServiceImpl(plugin);
        container.register(ISoundService.class, soundService);

        IBossBarService bossBarService = new BossBarServiceImpl(plugin);
        container.register(IBossBarService.class, bossBarService);

        IDeathMessageService deathMessageService = new DeathMessageServiceImpl(
                config.death(), titleService, soundService);
        container.register(IDeathMessageService.class, deathMessageService);

        IJoinMessageService joinMessageService = new JoinMessageServiceImpl(
                config.join(), titleService, soundService);
        container.register(IJoinMessageService.class, joinMessageService);

        IQuitMessageService quitMessageService = new QuitMessageServiceImpl(
                config.quit(), titleService, soundService);
        container.register(IQuitMessageService.class, quitMessageService);

        IAnnouncementService announcementService = new AnnouncementServiceImpl(
                config.announcements(), titleService, soundService, bossBarService, plugin);
        container.register(IAnnouncementService.class, announcementService);

        return container;

    }
}
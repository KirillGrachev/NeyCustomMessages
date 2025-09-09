package com.ney.messages.event;

import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import org.bukkit.plugin.java.JavaPlugin;

public class EventDispatcher {

    private final JavaPlugin plugin;

    public EventDispatcher(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(@NotNull Listener @NotNull ... listeners) {

        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }

    }
}
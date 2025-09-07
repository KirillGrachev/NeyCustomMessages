package com.ney.messages.event;

import com.ney.messages.NeyCustomMessages;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class EventDispatcher {
    private final NeyCustomMessages plugin;

    public EventDispatcher(NeyCustomMessages plugin) {
        this.plugin = plugin;
    }

    public void registerEvents(@NotNull Listener @NotNull ... listeners) {
        for (Listener listener : listeners) {
            plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }
}
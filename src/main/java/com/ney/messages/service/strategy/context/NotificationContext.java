package com.ney.messages.service.strategy.context;

import com.ney.messages.config.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class NotificationContext {

    private final Player player;
    private final Map<String, String> placeholders;
    private final Object config;

    private NotificationContext(@NotNull Builder builder) {
        this.player = builder.player;
        this.placeholders = builder.placeholders;
        this.config = builder.config;
    }

    public Player getPlayer() { return player; }
    public Map<String, String> getPlaceholders() { return placeholders; }
    public Object getConfig() { return config; }

    public String resolvePlaceholder(String key) {
        return placeholders.getOrDefault(key, key);
    }

    public AnnouncementConfig getAnnouncementConfig() {
        return config instanceof AnnouncementConfig ? (AnnouncementConfig) config : null;
    }

    public DeathConfig getDeathConfig() {
        return config instanceof DeathConfig ? (DeathConfig) config : null;
    }

    public JoinConfig getJoinConfig() {
        return config instanceof JoinConfig ? (JoinConfig) config : null;
    }

    public QuitConfig getQuitConfig() {
        return config instanceof QuitConfig ? (QuitConfig) config : null;
    }

    public static class Builder {

        private Player player;

        private final Map<String, String> placeholders = new HashMap<>();
        private Object config;

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder placeholder(String key, String value) {
            this.placeholders.put(key, value);
            return this;
        }

        public Builder config(Object config) {
            this.config = config;
            return this;
        }

        public NotificationContext build() {
            return new NotificationContext(this);
        }

    }
}
package com.ney.messages.service.impl.announcement.broadcast;

import com.ney.messages.config.AnnouncementConfig;
import com.ney.messages.service.strategy.NotificationStrategy;
import com.ney.messages.service.strategy.context.NotificationContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public class AnnouncementBroadcaster {

    private final List<NotificationStrategy> strategies;

    public AnnouncementBroadcaster(List<NotificationStrategy> strategies) {
        this.strategies = strategies;
    }

    public void broadcastToAllPlayers(AnnouncementConfig announcement) {

        for (Player player : Bukkit.getOnlinePlayers()) {

            NotificationContext context = new NotificationContext.Builder()
                    .player(player)
                    .config(announcement)
                    .build();

            for (NotificationStrategy strategy : strategies) {

                if (strategy.isEnabled(context)) {
                    strategy.execute(context);
                }

            }
        }
    }
}
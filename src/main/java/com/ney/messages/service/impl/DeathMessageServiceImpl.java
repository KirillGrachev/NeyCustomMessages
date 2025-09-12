package com.ney.messages.service.impl;

import com.ney.messages.NeyCustomMessages;
import com.ney.messages.config.DeathConfig;
import com.ney.messages.service.interfaces.IDeathMessageService;
import com.ney.messages.service.strategy.context.NotificationContext;
import com.ney.messages.service.strategy.NotificationStrategy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.List;

public class DeathMessageServiceImpl implements IDeathMessageService {

    private final DeathConfig config;
    private final List<NotificationStrategy> strategies;
    private final NeyCustomMessages plugin;

    public DeathMessageServiceImpl(DeathConfig config,
                                   List<NotificationStrategy> strategies,
                                   NeyCustomMessages plugin) {
        this.config = config;
        this.strategies = strategies;
        this.plugin = plugin;
    }

    @Override
    public void handleDeath(PlayerDeathEvent event) {

        if (!config.enabled()) return;

        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        event.setDeathMessage(null);

        NotificationContext context = new NotificationContext.Builder()
                .player(player)
                .placeholder("{player}", player.getName())
                .placeholder("{death_cause}", deathMessage != null ? deathMessage : "unknown")
                .config(config)
                .build();

        for (NotificationStrategy strategy : strategies) strategy.execute(context);

        Bukkit.getScheduler().runTaskLater(plugin,
                () -> {
                    if (player.isDead()) {
                        player.spigot().respawn();
                    }
                }, 1L);

    }
}
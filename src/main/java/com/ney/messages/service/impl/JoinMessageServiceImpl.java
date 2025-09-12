package com.ney.messages.service.impl;

import com.ney.messages.config.JoinConfig;
import com.ney.messages.service.interfaces.IJoinMessageService;
import com.ney.messages.service.strategy.NotificationStrategy;
import com.ney.messages.service.strategy.context.NotificationContext;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class JoinMessageServiceImpl implements IJoinMessageService {

    private final JoinConfig config;
    private final List<NotificationStrategy> strategies;

    public JoinMessageServiceImpl(JoinConfig config,
                                  List<NotificationStrategy> strategies) {
        this.config = config;
        this.strategies = strategies;
    }

    @Override
    public void handleJoin(PlayerJoinEvent event) {

        if (!config.enabled()) return;

        Player player = event.getPlayer();
        event.setJoinMessage(null);

        NotificationContext context = new NotificationContext.Builder()
                .player(player)
                .placeholder("{player}", player.getName())
                .config(config)
                .build();

        for (NotificationStrategy strategy : strategies) strategy.execute(context);

    }
}
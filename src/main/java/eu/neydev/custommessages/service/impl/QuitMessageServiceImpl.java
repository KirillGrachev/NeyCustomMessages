package eu.neydev.custommessages.service.impl;

import eu.neydev.custommessages.config.QuitConfig;
import eu.neydev.custommessages.service.interfaces.IQuitMessageService;
import eu.neydev.custommessages.service.strategy.NotificationStrategy;
import eu.neydev.custommessages.service.strategy.context.NotificationContext;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class QuitMessageServiceImpl implements IQuitMessageService {

    private final QuitConfig config;
    private final List<NotificationStrategy> strategies;

    public QuitMessageServiceImpl(QuitConfig config,
                                  List<NotificationStrategy> strategies) {
        this.config = config;
        this.strategies = strategies;
    }

    @Override
    public void handleQuit(PlayerQuitEvent event) {

        if (!config.enabled()) return;

        Player player = event.getPlayer();
        event.setQuitMessage(null);

        NotificationContext context = new NotificationContext.Builder()
                .player(player)
                .placeholder("{player}", player.getName())
                .config(config)
                .build();

        for (NotificationStrategy strategy : strategies) strategy.execute(context);

    }
}
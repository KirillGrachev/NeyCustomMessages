package eu.neydev.custommessages.service.impl;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.config.DeathConfig;
import eu.neydev.custommessages.service.interfaces.IDeathMessageService;
import eu.neydev.custommessages.service.strategy.context.NotificationContext;
import eu.neydev.custommessages.service.strategy.NotificationStrategy;
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
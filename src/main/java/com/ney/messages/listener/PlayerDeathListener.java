package com.ney.messages.listener;

import com.ney.messages.service.event.DeathMessageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class PlayerDeathListener implements Listener {

    private final DeathMessageService deathMessageService;

    public PlayerDeathListener(DeathMessageService deathMessageService) {
        this.deathMessageService = deathMessageService;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        deathMessageService.handleDeath(event);
    }
}
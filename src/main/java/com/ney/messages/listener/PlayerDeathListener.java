package com.ney.messages.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.ney.messages.service.interfaces.IDeathMessageService;

public class PlayerDeathListener implements Listener {

    private final IDeathMessageService deathMessageService;

    public PlayerDeathListener(IDeathMessageService deathMessageService) {
        this.deathMessageService = deathMessageService;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        deathMessageService.handleDeath(event);
    }
}
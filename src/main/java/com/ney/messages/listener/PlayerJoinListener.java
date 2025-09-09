package com.ney.messages.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import com.ney.messages.service.interfaces.IJoinMessageService;

public class PlayerJoinListener implements Listener {

    private final IJoinMessageService joinMessageService;

    public PlayerJoinListener(IJoinMessageService joinMessageService) {
        this.joinMessageService = joinMessageService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        joinMessageService.handleJoin(event);
    }
}
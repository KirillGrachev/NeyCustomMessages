package com.ney.messages.listener;

import com.ney.messages.service.event.JoinMessageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final JoinMessageService joinMessageService;

    public PlayerJoinListener(JoinMessageService joinMessageService) {
        this.joinMessageService = joinMessageService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        joinMessageService.handleJoin(event);
    }
}
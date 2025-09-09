package com.ney.messages.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import com.ney.messages.service.interfaces.IQuitMessageService;

public class PlayerQuitListener implements Listener {

    private final IQuitMessageService quitMessageService;

    public PlayerQuitListener(IQuitMessageService quitMessageService) {
        this.quitMessageService = quitMessageService;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        quitMessageService.handleQuit(event);
    }
}
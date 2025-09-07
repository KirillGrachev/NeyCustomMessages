package com.ney.messages.listener;

import com.ney.messages.service.event.QuitMessageService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final QuitMessageService quitMessageService;

    public PlayerQuitListener(QuitMessageService quitMessageService) {
        this.quitMessageService = quitMessageService;
    }

    @EventHandler
    public void onJoin(PlayerQuitEvent event) {
        quitMessageService.handleQuit(event);
    }
}
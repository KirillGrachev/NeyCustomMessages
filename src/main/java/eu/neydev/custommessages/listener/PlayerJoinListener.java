package eu.neydev.custommessages.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import eu.neydev.custommessages.service.interfaces.IJoinMessageService;

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
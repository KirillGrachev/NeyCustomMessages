package eu.neydev.custommessages.listener;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import eu.neydev.custommessages.service.interfaces.IDeathMessageService;

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
package com.ney.messages.service.interfaces;

import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Сервис для обработки сообщений о смерти игроков.
 */
public interface IDeathMessageService {

    /**
     * Обрабатывает событие смерти игрока.
     *
     * @param event Событие смерти игрока.
     */
    void handleDeath(PlayerDeathEvent event);

}
package com.ney.messages.service.interfaces;

import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Сервис для обработки сообщений о входе игроков на сервер.
 */
public interface IJoinMessageService {

    /**
     * Обрабатывает событие входа игрока на сервер.
     *
     * @param event Событие входа игрока.
     */
    void handleJoin(PlayerJoinEvent event);

}
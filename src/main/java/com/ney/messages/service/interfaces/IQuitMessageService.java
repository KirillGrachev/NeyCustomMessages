package com.ney.messages.service.interfaces;

import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Сервис для обработки сообщений о выходе игроков с сервера.
 */
public interface IQuitMessageService {

    /**
     * Обрабатывает событие выхода игрока с сервера.
     *
     * @param event Событие выхода игрока.
     */
    void handleQuit(PlayerQuitEvent event);

}
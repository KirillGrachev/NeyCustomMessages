package com.ney.messages.service.interfaces;

import org.bukkit.entity.Player;

/**
 * Сервис для отправки титульных сообщений игрокам.
 */
public interface ITitleService {

    /**
     * Отправляет титульное сообщение игроку.
     *
     * @param player   Игрок, которому отправляется сообщение.
     * @param title    Основной заголовок. Может быть null или пустой строкой.
     * @param subtitle Подзаголовок. Может быть null или пустой строкой.
     * @param fadeIn   Время появления в тиках (ticks).
     * @param stay     Время отображения в тиках.
     * @param fadeOut  Время исчезновения в тиках.
     */
    void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut);

}
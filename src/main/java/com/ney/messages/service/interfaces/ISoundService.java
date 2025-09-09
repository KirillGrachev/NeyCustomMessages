package com.ney.messages.service.interfaces;

import org.bukkit.entity.Player;

/**
 * Сервис для воспроизведения звуков игрокам.
 */
public interface ISoundService {

    /**
     * Воспроизводит звук для игрока.
     *
     * @param player  Игрок, для которого воспроизводится звук.
     * @param soundName Название звука (например, "ENTITY_PLAYER_LEVELUP").
     * @param volume  Громкость звука (0.0 - 2.0).
     * @param pitch   Высота звука (0.0 - 2.0).
     */
    void playSound(Player player, String soundName, float volume, float pitch);

}
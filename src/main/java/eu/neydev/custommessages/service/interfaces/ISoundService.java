package eu.neydev.custommessages.service.interfaces;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

public interface ISoundService {

    /**
     * Проигрывает звук игроку.
     *
     * @param player получатель (null или оффлайн - тихо пропускается)
     * @param sound  разрешенный звук
     * @param volume громкость
     * @param pitch  высота
     */
    void playSound(Player player, Sound sound, float volume, float pitch);
}

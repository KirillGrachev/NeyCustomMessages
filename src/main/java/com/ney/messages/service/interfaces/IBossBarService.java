package com.ney.messages.service.interfaces;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import java.io.Closeable;

/**
 * Сервис для управления босс-барами.
 */
public interface IBossBarService extends Closeable {

    /**
     * Отображает босс-бар игроку.
     *
     * @param player       Игрок, которому отображается босс-бар.
     * @param text         Текст босс-бара.
     * @param color        Цвет босс-бара.
     * @param style        Стиль босс-бара.
     * @param duration     Длительность отображения в секундах.
     * @param progressDecay Если true, босс-бар будет уменьшаться со временем.
     */
    void showBossBar(Player player, String text, BarColor color, BarStyle style,
                     int duration, boolean progressDecay);

    /**
     * Удаляет босс-бар у конкретного игрока.
     *
     * @param player Игрок, у которого нужно удалить босс-бар.
     */
    void removeBossBar(Player player);

    /**
     * Удаляет все активные босс-бары.
     */
    void removeAllBossBars();

    /**
     * Закрывает все ресурсы, связанные с босс-барами.
     * Реализация {@link AutoCloseable}.
     */
    @Override
    void close();

}
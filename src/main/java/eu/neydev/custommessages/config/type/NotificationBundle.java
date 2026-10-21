package eu.neydev.custommessages.config.type;

import org.jetbrains.annotations.NotNull;

/**
 * Комплект выводов одного уведомления: чат, титр, звук и босс-бар.
 *
 * Одинаковая форма у модулей (join, quit, death) и у анонсов: в 1.x четыре
 * стратегии разбирали Object-конфиг цепочкой instanceof по четырем разным
 * типам, теперь они читают один и тот же набор настроек.
 *
 * @param chat    текстовое сообщение
 * @param title   титр
 * @param sound   звук
 * @param bossBar босс-бар
 */
public record NotificationBundle(@NotNull ChatSettings chat,
                                 @NotNull TitleSettings title,
                                 @NotNull SoundSettings sound,
                                 @NotNull BossBarSettings bossBar) {

    /** Пустой комплект: все четыре вывода выключены. */
    public static final NotificationBundle EMPTY =
            new NotificationBundle(ChatSettings.EMPTY, TitleSettings.EMPTY,
                    SoundSettings.EMPTY, BossBarSettings.EMPTY);

}

package eu.neydev.custommessages.config.type;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.NotNull;

/**
 * Настройки босс-бара.
 *
 * Цвет и стиль разрешаются из строк конфигурации при загрузке снапшота:
 * неизвестное значение дает дефолт с предупреждением в консоль вместо
 * падения в valueOf на каждой доставке. Длительность проверена на
 * положительность: ноль или отрицательное число в 1.x означало
 * runTaskLater с неположительной задержкой - бар исчезал в тот же тик.
 *
 * @param enabled       включен ли босс-бар
 * @param text          текст, неокрашенный, с плейсхолдерами
 * @param color         цвет полосы
 * @param style         стиль деления полосы
 * @param duration      длительность показа, секунды (1 и больше)
 * @param progressDecay уменьшать ли заполнение полосы со временем
 */
public record BossBarSettings(boolean enabled,
                              @NotNull String text,
                              @NotNull BarColor color,
                              @NotNull BarStyle style,
                              int duration,
                              boolean progressDecay) {

    /** Настройки по умолчанию: бар выключен. */
    public static final BossBarSettings EMPTY =
            new BossBarSettings(false, "", BarColor.YELLOW, BarStyle.SOLID, 5, false);

}

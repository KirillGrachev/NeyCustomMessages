package eu.neydev.custommessages.config.type;

import org.bukkit.Sound;
import org.jetbrains.annotations.Nullable;

/**
 * Настройки звука.
 *
 * {@link Sound} разрешается из строки конфигурации ОДИН раз при загрузке
 * снапшота: в 1.x Sound.valueOf вызывался на каждую доставку и неизвестное
 * имя звука роняло IllegalArgumentException в обработчик события - опечатка
 * в config.yml стоила исключения на каждом входе игрока. Здесь недопустимое
 * имя дает sound == null с предупреждением в консоль, а доставка такой
 * настройки просто пропускается.
 *
 * @param enabled включен ли звук
 * @param sound   разрешенный звук или null, если имя в конфигурации неизвестно
 * @param volume  громкость (0.0 и выше)
 * @param pitch   высота (0.0 и выше)
 */
public record SoundSettings(boolean enabled,
                            @Nullable Sound sound,
                            float volume,
                            float pitch) {

    /** Настройки по умолчанию: звук выключен. */
    public static final SoundSettings EMPTY = new SoundSettings(false, null, 1.0f, 1.0f);

    /**
     * Можно ли воспроизвести звук: настройка включена и имя разрешено.
     */
    public boolean isPlayable() {
        return enabled && sound != null;
    }

}

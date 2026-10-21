package eu.neydev.custommessages.config.type;

import org.jetbrains.annotations.NotNull;

/**
 * Настройки титра (текст в центре экрана).
 *
 * Тексты хранятся неокрашенными: окраска выполняется при доставке после
 * подстановки плейсхолдеров. Времена - в тиках (20 тиков = 1 секунда),
 * проверены при загрузке на неотрицательность.
 *
 * @param enabled  включен ли титр
 * @param title    основной текст
 * @param subtitle второстепенный текст
 * @param fadeIn   время появления, тики
 * @param stay     время отображения, тики
 * @param fadeOut  время исчезновения, тики
 */
public record TitleSettings(boolean enabled,
                            @NotNull String title,
                            @NotNull String subtitle,
                            int fadeIn,
                            int stay,
                            int fadeOut) {

    /** Настройки по умолчанию: титр выключен. */
    public static final TitleSettings EMPTY =
            new TitleSettings(false, "", "", 10, 70, 20);

    /**
     * Есть ли что показывать: пустой титр из обоих текстов не отправляется.
     */
    public boolean hasContent() {
        return !title.isEmpty() || !subtitle.isEmpty();
    }

}

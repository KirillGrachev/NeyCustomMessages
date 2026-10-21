package eu.neydev.custommessages.config.type;

import net.md_5.bungee.api.chat.ClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Настройки текстового сообщения (чат).
 *
 * {@code format} хранится неокрашенным: плейсхолдеры подставляются при
 * доставке, а окрашивание выполняется после подстановки - так значение
 * плейсхолдера не может внести в текст сырой цветовой код. Hover и click
 * работают только у анонсов: сообщения модулей уходят обычным
 * broadcastMessage, как в 1.x. Значения проверены при загрузке:
 * clickAction равен null, если действие неизвестно или OPEN_URL получил
 * не-URL (менеджер конфигурации печатает предупреждение).
 *
 * @param enabled     включено ли сообщение
 * @param format      текст с плейсхолдерами, неокрашенный
 * @param hover       текст при наведении или null
 * @param clickAction действие клика или null
 * @param clickValue  аргумент действия (URL или команда) или null
 */
public record ChatSettings(boolean enabled,
                           @NotNull String format,
                           @Nullable String hover,
                           @Nullable ClickEvent.Action clickAction,
                           @Nullable String clickValue) {

    /** Настройки по умолчанию: сообщение выключено, текст пуст. */
    public static final ChatSettings EMPTY = new ChatSettings(false, "", null, null, null);

    /**
     * Есть ли интерактив: наведение или клик.
     */
    public boolean isInteractive() {
        return hover != null || clickAction != null;
    }

}

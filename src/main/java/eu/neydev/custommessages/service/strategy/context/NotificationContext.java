package eu.neydev.custommessages.service.strategy.context;

import eu.neydev.custommessages.config.type.NotificationBundle;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Контекст одной доставки уведомления.
 *
 * В 1.x контекст нес Object-конфиг, и каждая из четырех стратегий
 * разбирала его цепочкой instanceof по четырем типам. Теперь контекст
 * несет готовый {@link NotificationBundle} и признак анонса: анонсы
 * доставляются компонентным сообщением каждому игроку лично (hover и
 * click живут только в компонентах), а сообщения модулей уходят обычным
 * broadcast для всех, включая консоль.
 */
public class NotificationContext {

    private final Player player;
    private final Map<String, String> placeholders;
    private final NotificationBundle bundle;
    private final boolean announcement;

    private NotificationContext(@NotNull Builder builder) {
        this.player = builder.player;
        this.placeholders = Map.copyOf(builder.placeholders);
        this.bundle = builder.bundle;
        this.announcement = builder.announcement;
    }

    public Player getPlayer() {
        return player;
    }

    public Map<String, String> getPlaceholders() {
        return placeholders;
    }

    public NotificationBundle getBundle() {
        return bundle;
    }

    /**
     * Анонс ли это: определяет маршрут доставки чат-сообщения.
     */
    public boolean isAnnouncement() {
        return announcement;
    }

    /**
     * Подставляет плейсхолдеры в текст.
     *
     * @param text текст с плейсхолдерами вида {player}
     * @return текст с подставленными значениями
     */
    public @NotNull String apply(@NotNull String text) {

        String result = text;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;

    }

    public static class Builder {

        private Player player;
        private final Map<String, String> placeholders = new HashMap<>();
        private NotificationBundle bundle = NotificationBundle.EMPTY;
        private boolean announcement;

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder placeholder(String key, String value) {
            this.placeholders.put(key, value);
            return this;
        }

        public Builder bundle(NotificationBundle bundle) {
            this.bundle = bundle;
            return this;
        }

        public Builder announcement(boolean announcement) {
            this.announcement = announcement;
            return this;
        }

        public NotificationContext build() {
            return new NotificationContext(this);
        }

    }

}

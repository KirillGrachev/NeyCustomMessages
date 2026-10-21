package eu.neydev.custommessages.config.type;

import org.jetbrains.annotations.NotNull;

/**
 * Одно периодическое объявление.
 *
 * @param id     ключ из config.yml (announcements.list.&lt;id&gt;), используется
 *               в диагностике и в порядке ротации
 * @param bundle комплект выводов объявления
 */
public record Announcement(@NotNull String id, @NotNull NotificationBundle bundle) {
}

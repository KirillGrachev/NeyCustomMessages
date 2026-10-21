package eu.neydev.custommessages.config;

import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.config.type.MessageKey;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.config.type.PermissionNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Контракт конфигурации NeyCustomMessages.
 * Все остальные компоненты зависят от интерфейса, а не от реализации:
 * слушатели, сервисы и команды не знают, откуда приехали значения (YAML,
 * дефолты кода или подставленный в тесте стаб).
 */
public interface CustomMessagesConfig {

    /**
     * Мастер-выключатель плагина: при false модули молчат, анонсы не ходят.
     */
    boolean isPluginEnabled();

    boolean areMessagesEnabled();

    /**
     * Префикс, подставляемый в плейсхолдер {prefix}. Уже окрашен.
     */
    @NotNull String getMessagePrefix();

    /**
     * Строки служебного сообщения по ключу.
     *
     * @param key ключ сообщения
     * @return окрашенные строки; пустой список, если сообщение выключено
     */
    @NotNull List<String> getMessages(@NotNull MessageKey key);

    boolean arePermissionsEnabled();

    /**
     * Строка узла права из конфигурации (или дефолт узла).
     *
     * @param node узел права
     * @return название права для Bukkit
     */
    @NotNull String getPermission(@NotNull PermissionNode node);

    boolean isModuleEnabled(@NotNull MessageModule module);

    /**
     * Комплект выводов модуля (чат, титр, звук, босс-бар).
     *
     * @param module модуль событий игрока
     * @return набор настроек доставки
     */
    @NotNull NotificationBundle getModuleBundle(@NotNull MessageModule module);

    boolean areAnnouncementsEnabled();

    /**
     * Интервал ротации анонсов в секундах (не меньше единицы).
     */
    int getAnnouncementInterval();

    /**
     * Порядок ротации: true - случайный, false - по кругу.
     */
    boolean isRandomAnnouncementOrder();

    /**
     * Список анонсов в порядке объявления в config.yml.
     */
    @NotNull List<Announcement> getAnnouncements();

}

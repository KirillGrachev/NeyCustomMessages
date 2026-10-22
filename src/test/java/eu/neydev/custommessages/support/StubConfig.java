package eu.neydev.custommessages.support;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.config.type.ChatSettings;
import eu.neydev.custommessages.config.type.MessageKey;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.config.type.PermissionNode;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Изменяемый стаб конфигурации для юнит-тестов.
 *
 * Сервисы, слушатели и команды проверяются без Bukkit и без YAML: тест
 * задает ровно те значения, которые нужны сценарию, и не зависит от
 * порядка ключей в config.yml. Дефолты повторяют поставляемый файл:
 * все модули включены, анонсы включены с интервалом 15 секунд в
 * случайном порядке, права включены.
 */
public final class StubConfig implements CustomMessagesConfig {

    private boolean pluginEnabled = true;
    private boolean messagesEnabled = true;
    private String prefix = "";
    private boolean permissionsEnabled = true;
    private boolean announcementsEnabled = true;
    private int interval = 15;
    private boolean random = true;
    private List<Announcement> announcements = List.of();
    private final Map<MessageKey, List<String>> messages = new EnumMap<>(MessageKey.class);
    private final Map<MessageModule, Boolean> moduleEnabled = new EnumMap<>(MessageModule.class);
    private final Map<MessageModule, NotificationBundle> bundles = new EnumMap<>(MessageModule.class);

    public @NotNull StubConfig withPluginEnabled(boolean enabled) {
        this.pluginEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withMessagesEnabled(boolean enabled) {
        this.messagesEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withPrefix(@NotNull String value) {
        this.prefix = value;
        return this;
    }

    public @NotNull StubConfig withPermissionsEnabled(boolean enabled) {
        this.permissionsEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withAnnouncementsEnabled(boolean enabled) {
        this.announcementsEnabled = enabled;
        return this;
    }

    public @NotNull StubConfig withInterval(int seconds) {
        this.interval = seconds;
        return this;
    }

    public @NotNull StubConfig withRandomOrder(boolean randomOrder) {
        this.random = randomOrder;
        return this;
    }

    public @NotNull StubConfig withAnnouncements(@NotNull Announcement... values) {
        this.announcements = List.of(values);
        return this;
    }

    public @NotNull StubConfig withMessage(@NotNull MessageKey key, @NotNull String... lines) {
        messages.put(key, Arrays.asList(lines));
        return this;
    }

    public @NotNull StubConfig withModuleEnabled(@NotNull MessageModule module, boolean enabled) {
        moduleEnabled.put(module, enabled);
        return this;
    }

    public @NotNull StubConfig withBundle(@NotNull MessageModule module,
                                          @NotNull NotificationBundle bundle) {
        bundles.put(module, bundle);
        return this;
    }

    public @NotNull StubConfig withModuleChat(@NotNull MessageModule module, @NotNull String format) {
        return withBundle(module, new NotificationBundle(
                new ChatSettings(true, format, null, null, null),
                bundleOf(module).title(),
                bundleOf(module).sound(),
                bundleOf(module).bossBar()));
    }

    private @NotNull NotificationBundle bundleOf(@NotNull MessageModule module) {
        return bundles.getOrDefault(module, NotificationBundle.EMPTY);
    }

    @Override
    public boolean isPluginEnabled() {
        return pluginEnabled;
    }

    @Override
    public boolean areMessagesEnabled() {
        return messagesEnabled;
    }

    @Override
    public @NotNull String getMessagePrefix() {
        return prefix;
    }

    @Override
    public @NotNull List<String> getMessages(@NotNull MessageKey key) {
        return messages.getOrDefault(key, List.of());
    }

    @Override
    public boolean arePermissionsEnabled() {
        return permissionsEnabled;
    }

    @Override
    public @NotNull String getPermission(@NotNull PermissionNode node) {
        return node.getDefaultPermission();
    }

    @Override
    public boolean isModuleEnabled(@NotNull MessageModule module) {
        return moduleEnabled.getOrDefault(module, true);
    }

    @Override
    public @NotNull NotificationBundle getModuleBundle(@NotNull MessageModule module) {
        return bundles.getOrDefault(module, NotificationBundle.EMPTY);
    }

    @Override
    public boolean areAnnouncementsEnabled() {
        return announcementsEnabled;
    }

    @Override
    public int getAnnouncementInterval() {
        return interval;
    }

    @Override
    public boolean isRandomAnnouncementOrder() {
        return random;
    }

    @Override
    public @NotNull List<Announcement> getAnnouncements() {
        return new ArrayList<>(announcements);
    }

}

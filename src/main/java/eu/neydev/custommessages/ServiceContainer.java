package eu.neydev.custommessages;

import eu.neydev.custommessages.announcement.AnnouncementSelector;
import eu.neydev.custommessages.announcement.AnnouncementService;
import eu.neydev.custommessages.config.ConfigManager;
import eu.neydev.custommessages.service.BossBarService;
import eu.neydev.custommessages.service.ConsoleService;
import eu.neydev.custommessages.service.MessageService;
import eu.neydev.custommessages.service.Notifier;
import eu.neydev.custommessages.service.PermissionService;
import org.jetbrains.annotations.NotNull;

/**
 * Контейнер компонентов плагина.
 *
 * Порядок создания зафиксирован: каждый сервис получает уже готовые
 * зависимости и никто не создает их себе сам. Версия 1.x собирала граф
 * в собственном DI-контейнере с bind/get по Class-ключам и пересобирала
 * его целиком на каждый reload; здесь граф создается один раз, а reload
 * подменяет снапшот конфигурации, который все сервисы читают живьем.
 *
 * Контейнер владеет подпиской на reload: снятие босс-баров и перезапуск
 * ротации анонсов происходят по единственному триггеру -
 * configManager.reload(), - поэтому порядок шагов не зависит от того,
 * кто вызвал перезагрузку (команда, другой плагин, тест).
 */
public final class ServiceContainer {

    private final NeyCustomMessages plugin;
    private final ConfigManager configManager;
    private final ConsoleService consoleService;

    private final MessageService messageService;
    private final PermissionService permissionService;
    private final BossBarService bossBarService;
    private final Notifier notifier;
    private final AnnouncementService announcementService;

    public ServiceContainer(@NotNull NeyCustomMessages plugin,
                            @NotNull ConfigManager configManager,
                            @NotNull ConsoleService consoleService) {

        this.plugin = plugin;
        this.configManager = configManager;
        this.consoleService = consoleService;

        // Базовый слой: сообщения и права читают только конфигурацию
        this.messageService = new MessageService(configManager);
        this.permissionService = new PermissionService(configManager);

        // Слой доставки: босс-бары - единственная состоятельная часть
        this.bossBarService = new BossBarService(plugin);
        this.notifier = new Notifier(plugin, bossBarService);

        // Ротация анонсов: читает снапшот живьем, задача принадлежит сервису
        this.announcementService = new AnnouncementService(plugin, configManager,
                consoleService, new AnnouncementSelector(), notifier);

        // Подписка на reload - здесь, а не в команде: единственный триггер
        // для всех последствий перезагрузки конфигурации
        configManager.onReload(() -> {
            bossBarService.removeAll();
            announcementService.restart();
        });

    }

    public NeyCustomMessages getPlugin() {
        return plugin;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ConsoleService getConsoleService() {
        return consoleService;
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public PermissionService getPermissionService() {
        return permissionService;
    }

    public BossBarService getBossBarService() {
        return bossBarService;
    }

    public Notifier getNotifier() {
        return notifier;
    }

    public AnnouncementService getAnnouncementService() {
        return announcementService;
    }

}

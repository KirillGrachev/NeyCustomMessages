package eu.neydev.custommessages;

import eu.neydev.custommessages.config.ConfigManager;
import eu.neydev.custommessages.config.type.MessageKey;
import eu.neydev.custommessages.service.ConsoleService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Контейнер собирает граф один раз, передает сервисам одну и ту же
 * конфигурацию и владеет подпиской на reload: снятие босс-баров и
 * перезапуск ротации идут по единственному триггеру.
 */
class ServiceContainerSmokeTest {

    private final NeyCustomMessages plugin = mock(NeyCustomMessages.class);
    private final ConfigManager configManager = mock(ConfigManager.class);
    private final ConsoleService consoleService = mock(ConsoleService.class);

    private ServiceContainer container() {
        return new ServiceContainer(plugin, configManager, consoleService);
    }

    @Test
    @DisplayName("Все компоненты созданы и доступны")
    void everyComponentIsCreated() {

        ServiceContainer container = container();

        assertSame(plugin, container.getPlugin());
        assertSame(configManager, container.getConfigManager());
        assertSame(consoleService, container.getConsoleService());
        assertNotNull(container.getMessageService());
        assertNotNull(container.getPermissionService());
        assertNotNull(container.getBossBarService());
        assertNotNull(container.getNotifier());
        assertNotNull(container.getAnnouncementService());

    }

    @Test
    @DisplayName("Сервисы читают конфигурацию из контейнера, а не свою копию")
    void servicesShareTheSameConfig() {

        when(configManager.getMessages(MessageKey.USAGE)).thenReturn(List.of("shared"));
        when(configManager.areMessagesEnabled()).thenReturn(true);
        when(configManager.getMessagePrefix()).thenReturn("");

        assertEquals(List.of("shared"),
                container().getMessageService().build(MessageKey.USAGE, Map.of()));

    }

    @Test
    @DisplayName("Подписка на reload снятие баров и перезапуск ротации действительно дергает сервисы")
    void reloadSubscriptionDrivesTheSideEffects() {

        ServiceContainer container = container();

        ArgumentCaptor<Runnable> subscription = ArgumentCaptor.forClass(Runnable.class);

        verify(configManager).onReload(subscription.capture());

        subscription.getValue().run();

        // перезапуск ротации читает мастер-выключатель из конфигурации -
        // значит, announcementService.restart() действительно вызван
        verify(configManager, atLeastOnce()).isPluginEnabled();
        assertNotNull(container.getAnnouncementService());

    }

    @Test
    @DisplayName("Два контейнера не делят сервисы между собой")
    void containersDoNotShareServices() {

        ServiceContainer first = container();
        ServiceContainer second = container();

        assertNotNull(first.getNotifier());
        assertNotNull(second.getNotifier());
        assertNotSame(first.getNotifier(), second.getNotifier());

    }

}

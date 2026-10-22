package eu.neydev.custommessages.listener;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.service.Notifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Слушатель входа игрока.
 *
 * Включенный модуль ЗАМЕНЯет ванильное сообщение входа (setJoinMessage(null)),
 * выключенный оставляет его нетронутым - поэтому проверка модуля стоит до
 * отмены. Мастер-выключатель плагина проверяется здесь же: settings.enabled
 * false означает «вести себя так, будто плагина нет», включая ванильные
 * сообщения.
 */
public class PlayerJoinListener implements Listener {

    private final CustomMessagesConfig config;
    private final Notifier notifier;

    public PlayerJoinListener(@NotNull CustomMessagesConfig config, @NotNull Notifier notifier) {
        this.config = config;
        this.notifier = notifier;
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {

        if (!config.isPluginEnabled() || !config.isModuleEnabled(MessageModule.JOIN)) {
            return;
        }

        event.setJoinMessage(null);

        Player player = event.getPlayer();

        notifier.notifyModule(player,
                config.getModuleBundle(MessageModule.JOIN),
                Map.of("{player}", player.getName()));

    }

}

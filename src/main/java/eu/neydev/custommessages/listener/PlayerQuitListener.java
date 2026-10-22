package eu.neydev.custommessages.listener;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.service.Notifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Слушатель выхода игрока.
 *
 * Включенный модуль ЗАМЕНЯет ванильное сообщение выхода, выключенный
 * оставляет его нетронутым. Босс-бар выходящего игрока снимает
 * BossBarService: задача затухания проверяет isOnline и убирает бар.
 */
public class PlayerQuitListener implements Listener {

    private final CustomMessagesConfig config;
    private final Notifier notifier;

    public PlayerQuitListener(@NotNull CustomMessagesConfig config, @NotNull Notifier notifier) {
        this.config = config;
        this.notifier = notifier;
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {

        if (!config.isPluginEnabled() || !config.isModuleEnabled(MessageModule.QUIT)) {
            return;
        }

        event.setQuitMessage(null);

        Player player = event.getPlayer();

        notifier.notifyModule(player,
                config.getModuleBundle(MessageModule.QUIT),
                Map.of("{player}", player.getName()));

    }

}

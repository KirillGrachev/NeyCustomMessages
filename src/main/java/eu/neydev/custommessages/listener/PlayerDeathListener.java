package eu.neydev.custommessages.listener;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.service.Notifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Слушатель смерти игрока.
 *
 * Включенный модуль ЗАМЕНЯет ванильное сообщение о смерти; его текст
 * уходит в плейсхолдер {death_cause} - серверовладелец получает готовую
 * причину словами ваниллы и оборачивает ее в свой формат.
 */
public class PlayerDeathListener implements Listener {

    private final Plugin plugin;
    private final CustomMessagesConfig config;
    private final Notifier notifier;

    public PlayerDeathListener(@NotNull Plugin plugin,
                               @NotNull CustomMessagesConfig config,
                               @NotNull Notifier notifier) {
        this.plugin = plugin;
        this.config = config;
        this.notifier = notifier;
    }

    @EventHandler
    public void onDeath(@NotNull PlayerDeathEvent event) {

        if (!config.isPluginEnabled() || !config.isModuleEnabled(MessageModule.DEATH)) {
            return;
        }

        Player player = event.getEntity();
        String deathMessage = event.getDeathMessage();
        event.setDeathMessage(null);

        notifier.notifyModule(player,
                config.getModuleBundle(MessageModule.DEATH),
                Map.of("{player}", player.getName(),
                        "{death_cause}", deathMessage != null ? deathMessage : "unknown"));

        plugin.getServer().getScheduler().runTaskLater(plugin,
                () -> {
                    if (player.isDead()) {
                        player.spigot().respawn();
                    }
                }, 1L);

    }

}

package eu.neydev.custommessages.service.impl;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.service.interfaces.IQuitMessageService;
import eu.neydev.custommessages.service.strategy.NotificationStrategy;
import eu.neydev.custommessages.service.strategy.context.NotificationContext;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.List;

public class QuitMessageServiceImpl implements IQuitMessageService {

    private final CustomMessagesConfig config;
    private final List<NotificationStrategy> strategies;

    public QuitMessageServiceImpl(CustomMessagesConfig config,
                                  List<NotificationStrategy> strategies) {
        this.config = config;
        this.strategies = strategies;
    }

    @Override
    public void handleQuit(PlayerQuitEvent event) {

        if (!config.isPluginEnabled() || !config.isModuleEnabled(MessageModule.QUIT)) {
            return;
        }

        Player player = event.getPlayer();
        event.setQuitMessage(null);

        NotificationContext context = new NotificationContext.Builder()
                .player(player)
                .placeholder("{player}", player.getName())
                .bundle(config.getModuleBundle(MessageModule.QUIT))
                .build();

        for (NotificationStrategy strategy : strategies) {
            strategy.execute(context);
        }

    }

}

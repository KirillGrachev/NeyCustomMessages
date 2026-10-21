package eu.neydev.custommessages.service.impl;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.service.interfaces.IJoinMessageService;
import eu.neydev.custommessages.service.strategy.NotificationStrategy;
import eu.neydev.custommessages.service.strategy.context.NotificationContext;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;

public class JoinMessageServiceImpl implements IJoinMessageService {

    private final CustomMessagesConfig config;
    private final List<NotificationStrategy> strategies;

    public JoinMessageServiceImpl(CustomMessagesConfig config,
                                  List<NotificationStrategy> strategies) {
        this.config = config;
        this.strategies = strategies;
    }

    @Override
    public void handleJoin(PlayerJoinEvent event) {

        if (!config.isPluginEnabled() || !config.isModuleEnabled(MessageModule.JOIN)) {
            return;
        }

        Player player = event.getPlayer();
        event.setJoinMessage(null);

        NotificationContext context = new NotificationContext.Builder()
                .player(player)
                .placeholder("{player}", player.getName())
                .bundle(config.getModuleBundle(MessageModule.JOIN))
                .build();

        for (NotificationStrategy strategy : strategies) {
            strategy.execute(context);
        }

    }

}

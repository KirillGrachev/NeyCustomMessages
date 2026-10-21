package eu.neydev.custommessages.service.strategy;

import eu.neydev.custommessages.config.type.BossBarSettings;
import eu.neydev.custommessages.service.interfaces.IBossBarService;
import eu.neydev.custommessages.service.strategy.context.NotificationContext;
import org.jetbrains.annotations.NotNull;

/**
 * Стратегия доставки босс-бара.
 *
 * Цвет и стиль уже разрешены при загрузке конфигурации; текст передается
 * неокрашенным - сервис красит его один раз перед показом.
 */
public class BossBarStrategy implements NotificationStrategy {

    private final IBossBarService bossBarService;

    public BossBarStrategy(IBossBarService bossBarService) {
        this.bossBarService = bossBarService;
    }

    @Override
    public void execute(@NotNull NotificationContext context) {

        BossBarSettings bossBar = context.getBundle().bossBar();

        if (!isEnabled(context)) {
            return;
        }

        bossBarService.showBossBar(
                context.getPlayer(),
                context.apply(bossBar.text()),
                bossBar.color(),
                bossBar.style(),
                bossBar.duration(),
                bossBar.progressDecay()
        );

    }

    @Override
    public boolean isEnabled(@NotNull NotificationContext context) {

        BossBarSettings bossBar = context.getBundle().bossBar();

        return bossBar.enabled() && !bossBar.text().isEmpty();

    }

}

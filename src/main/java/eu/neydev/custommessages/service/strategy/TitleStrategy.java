package eu.neydev.custommessages.service.strategy;

import eu.neydev.custommessages.config.type.TitleSettings;
import eu.neydev.custommessages.service.interfaces.ITitleService;
import eu.neydev.custommessages.service.strategy.context.NotificationContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Стратегия доставки титра (текст в центре экрана).
 */
public class TitleStrategy implements NotificationStrategy {

    private final ITitleService titleService;

    public TitleStrategy(ITitleService titleService) {
        this.titleService = titleService;
    }

    @Override
    public void execute(@NotNull NotificationContext context) {

        TitleSettings title = context.getBundle().title();

        if (!isEnabled(context)) {
            return;
        }

        Player player = context.getPlayer();

        titleService.sendTitle(
                player,
                context.apply(title.title()),
                context.apply(title.subtitle()),
                title.fadeIn(),
                title.stay(),
                title.fadeOut()
        );

    }

    @Override
    public boolean isEnabled(@NotNull NotificationContext context) {

        TitleSettings title = context.getBundle().title();

        return title.enabled() && title.hasContent();

    }

}

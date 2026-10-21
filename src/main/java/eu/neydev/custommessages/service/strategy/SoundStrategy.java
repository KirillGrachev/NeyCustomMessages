package eu.neydev.custommessages.service.strategy;

import eu.neydev.custommessages.config.type.SoundSettings;
import eu.neydev.custommessages.service.interfaces.ISoundService;
import eu.neydev.custommessages.service.strategy.context.NotificationContext;
import org.jetbrains.annotations.NotNull;

/**
 * Стратегия доставки звука.
 *
 * Имя звука уже разрешено в {@link org.bukkit.Sound} при загрузке
 * конфигурации: в 1.x valueOf падал здесь на каждую доставку при
 * опечатке в config.yml.
 */
public class SoundStrategy implements NotificationStrategy {

    private final ISoundService soundService;

    public SoundStrategy(ISoundService soundService) {
        this.soundService = soundService;
    }

    @Override
    public void execute(@NotNull NotificationContext context) {

        SoundSettings sound = context.getBundle().sound();

        if (!isEnabled(context)) {
            return;
        }

        soundService.playSound(context.getPlayer(), sound.sound(), sound.volume(), sound.pitch());

    }

    @Override
    public boolean isEnabled(@NotNull NotificationContext context) {
        return context.getBundle().sound().isPlayable();
    }

}

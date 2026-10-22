package eu.neydev.custommessages.command.sub;

import eu.neydev.custommessages.command.CommandContext;
import eu.neydev.custommessages.command.CustomMessagesSubCommand;
import eu.neydev.custommessages.config.ConfigManager;
import eu.neydev.custommessages.config.type.MessageKey;
import eu.neydev.custommessages.config.type.PermissionNode;
import eu.neydev.custommessages.service.MessageService;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Подкоманда /ncm reload.
 *
 * Перечитывает config.yml; снятие босс-баров и перезапуск ротации анонсов
 * выполняются подпиской контейнера на reload конфигурации, а не отдельными
 * вызовами здесь: единственный триггер - configManager.reload(), и порядок
 * шагов не зависит от того, кто и откуда его дернул. В подтверждение
 * уходит время, число анонсов и интервал: по ним сразу видно, что секция
 * announcements не потерялась при правке файла.
 */
public class ReloadSubCommand implements CustomMessagesSubCommand {

    private final ConfigManager configManager;
    private final MessageService messageService;

    public ReloadSubCommand(@NotNull ConfigManager configManager,
                            @NotNull MessageService messageService) {
        this.configManager = configManager;
        this.messageService = messageService;
    }

    @Override
    public @NotNull String getName() {
        return "reload";
    }

    @Override
    public @NotNull PermissionNode getPermission() {
        return PermissionNode.RELOAD;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        long startedAt = System.nanoTime();
        configManager.reload();
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startedAt);

        messageService.send(context.sender(), MessageKey.RELOAD, Map.of(
                "time", String.valueOf(elapsedMillis),
                "announcements", String.valueOf(configManager.getAnnouncements().size()),
                "interval", String.valueOf(configManager.getAnnouncementInterval())
        ));

        return true;

    }

}

package eu.neydev.custommessages.command.sub;

import eu.neydev.custommessages.command.CommandContext;
import eu.neydev.custommessages.command.CustomMessagesSubCommand;
import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.MessageKey;
import eu.neydev.custommessages.config.type.MessageModule;
import eu.neydev.custommessages.config.type.PermissionNode;
import eu.neydev.custommessages.service.MessageService;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * Подкоманда /ncm info.
 *
 * Диагностика читается из снапшота конфигурации, а не из файла: команда
 * показывает ровно то, что сейчас применяют слушатели и ротация анонсов.
 * Все булевы значения подставляются словами state_on/state_off из той же
 * конфигурации, поэтому перевод диагностики не расходится с переводом
 * сообщений.
 */
public class InfoSubCommand implements CustomMessagesSubCommand {

    private final Plugin plugin;
    private final CustomMessagesConfig config;
    private final MessageService messageService;

    public InfoSubCommand(@NotNull Plugin plugin,
                          @NotNull CustomMessagesConfig config,
                          @NotNull MessageService messageService) {
        this.plugin = plugin;
        this.config = config;
        this.messageService = messageService;
    }

    @Override
    public @NotNull String getName() {
        return "info";
    }

    @Override
    public @NotNull PermissionNode getPermission() {
        return PermissionNode.RELOAD;
    }

    @Override
    public boolean execute(@NotNull CommandContext context, String @NotNull [] args) {

        messageService.send(context.sender(), MessageKey.INFO, Map.of(
                "version", plugin.getDescription().getVersion(),
                "join", state(config.isModuleEnabled(MessageModule.JOIN)),
                "quit", state(config.isModuleEnabled(MessageModule.QUIT)),
                "death", state(config.isModuleEnabled(MessageModule.DEATH)),
                "announcements_state", state(config.areAnnouncementsEnabled()),
                "count", String.valueOf(config.getAnnouncements().size()),
                "interval", String.valueOf(config.getAnnouncementInterval()),
                "mode", config.isRandomAnnouncementOrder() ? "random" : "round-robin",
                "permissions", state(config.arePermissionsEnabled())
        ));

        return true;

    }

    /**
     * Возвращает слово-фрагмент для булева значения.
     *
     * Выключенный фрагмент дает пустую строку, а не "null": диагностика
     * остается читаемой даже тогда, когда серверовладелец убрал слова.
     */
    private @NotNull String state(boolean enabled) {
        return String.join("", config.getMessages(enabled ? MessageKey.STATE_ON : MessageKey.STATE_OFF));
    }

}

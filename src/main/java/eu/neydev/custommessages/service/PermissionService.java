package eu.neydev.custommessages.service;

import eu.neydev.custommessages.config.CustomMessagesConfig;
import eu.neydev.custommessages.config.type.PermissionNode;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Сервис прав доступа.
 *
 * Узел плагина один и он функциональный (служебная команда), поэтому
 * правило одно: выключенная система прав разрешает всем. Модули и анонсы
 * работают для всех игроков без прав - они не действия, а оформление
 * событий, и gating здесь не имеет смысла.
 */
public class PermissionService {

    private final CustomMessagesConfig config;

    public PermissionService(@NotNull CustomMessagesConfig config) {
        this.config = config;
    }

    /**
     * Проверяет право служебной команды.
     *
     * @param sender проверяемый отправитель (игрок или консоль)
     * @param node   узел права
     * @return true если действие разрешено
     */
    public boolean has(@Nullable CommandSender sender, @NotNull PermissionNode node) {

        if (!config.arePermissionsEnabled()) {
            return true;
        }

        return sender != null && sender.hasPermission(config.getPermission(node));

    }

}

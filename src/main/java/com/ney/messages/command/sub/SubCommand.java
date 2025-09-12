package com.ney.messages.command.sub;

import com.ney.messages.config.parser.CommandConfigParser;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * Интерфейс для динамической подкоманды.
 * Реализации этого интерфейса создаются на основе данных из конфигурации.
 */
public interface SubCommand {

    /**
     * Выполняет подкоманду.
     * @param sender Отправитель команды.
     * @param subCommandConfig Конфигурация подкоманды из YAML-файла.
     * @return true, если команда выполнена успешно.
     */
    boolean execute(@NotNull CommandSender sender,
                    @NotNull CommandConfigParser.SubCommandConfig subCommandConfig);

    /**
     * Возвращает ключ действия (например, "reload"), на которое реагирует эта подкоманда.
     */
    String getActionKey();

}
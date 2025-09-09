package com.ney.messages.command;

import com.ney.messages.NeyCustomMessages;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final NeyCustomMessages plugin;

    public CommandManager(NeyCustomMessages plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command command,
                             @NotNull String label,
                             @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("ncm")) {

            if (args.length == 0) {

                sender.sendMessage("§6NeyCustomMessages §7| §fИспользование: /ncm [reload]");
                return true;

            }

            if (args[0].equalsIgnoreCase("reload")) {

                if (sender.hasPermission("neycustommessages.reload")) {

                    plugin.reloadPlugin();

                    sender.sendMessage("§aКонфигурация плагина успешно перезагружена!");
                    plugin.getLoggerHelper().info("Command", "Configuration reloaded by %s", sender.getName());

                } else sender.sendMessage("§cУ вас нет прав для выполнения этой команды.");
                return true;

            }

            sender.sendMessage("§cНеизвестный аргумент. Использование: /ncm [reload]");
            return true;

        }

        return false;

    }

    @Nullable @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {

        if (command.getName().equalsIgnoreCase("ncm")) {

            if (args.length == 1) {

                List<String> completions = new ArrayList<>();
                List<String> commands = List.of("reload");

                StringUtil.copyPartialMatches(args[0], commands, completions);
                Collections.sort(completions);

                return completions;

            }
        }

        return Collections.emptyList();

    }
}
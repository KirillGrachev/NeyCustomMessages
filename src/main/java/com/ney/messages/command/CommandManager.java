package com.ney.messages.command;

import com.ney.messages.NeyCustomMessages;
import com.ney.messages.command.sub.SubCommand;
import com.ney.messages.command.sub.SubCommandFactory;
import com.ney.messages.config.parser.CommandConfigParser;
import com.ney.messages.util.HexColorUtil;
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
import java.util.Map;

public class CommandManager implements CommandExecutor, TabCompleter {

    private final Map<String, CommandConfigParser.CommandConfig> commands;
    private final SubCommandFactory subCommandFactory;

    public CommandManager(@NotNull NeyCustomMessages plugin) {
        this.commands = CommandConfigParser.parse(plugin.getConfig());
        this.subCommandFactory = new SubCommandFactory(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        CommandConfigParser.CommandConfig cmdConfig = commands.get(command.getName().toLowerCase());
        if (cmdConfig == null) return false;

        if (args.length == 0) {

            sender.sendMessage(HexColorUtil.color(cmdConfig.usage()));
            return true;

        }

        String subCommandName = args[0].toLowerCase();
        CommandConfigParser.SubCommandConfig subConfig = cmdConfig.subcommands().get(subCommandName);
        if (subConfig == null) {

            sender.sendMessage(HexColorUtil.color(cmdConfig.usage()));
            return true;

        }

        if (!subConfig.permission().isEmpty() && !sender.hasPermission(subConfig.permission())) {

            sender.sendMessage(HexColorUtil.color(subConfig.messageNoPermission()));
            return true;

        }

        SubCommand subCommand = subCommandFactory.getCommand(subCommandName);
        return subCommand.execute(sender, subConfig);

    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {

        CommandConfigParser.CommandConfig cmdConfig = commands.get(command.getName().toLowerCase());
        if (cmdConfig == null) return Collections.emptyList();

        if (args.length == 1) {

            List<String> completions = new ArrayList<>();

            for (Map.Entry<String, CommandConfigParser.SubCommandConfig> entry : cmdConfig.subcommands().entrySet()) {

                String subCmdName = entry.getKey();
                CommandConfigParser.SubCommandConfig subCmd = entry.getValue();

                String permission = subCmd.permission();
                if (!permission.isEmpty() && !sender.hasPermission(permission)) {
                    continue;
                }

                completions.add(subCmdName);

            }

            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());

        }

        return Collections.emptyList();

    }
}
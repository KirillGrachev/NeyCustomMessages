package com.ney.messages.command;

import com.ney.messages.NeyCustomMessages;
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

    private final NeyCustomMessages plugin;
    private final Map<String, CommandConfigParser.CommandConfig> commands;

    public CommandManager(@NotNull NeyCustomMessages plugin) {
        this.plugin = plugin;
        this.commands = CommandConfigParser.parse(plugin.getConfig());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        CommandConfigParser.CommandConfig cmdConfig = commands.get(command.getName()
                .toLowerCase());

        if (cmdConfig == null) return false;

        if (args.length == 0) {

            sender.sendMessage(HexColorUtil.color(cmdConfig.usage()));
            return true;

        }

        String sub = args[0].toLowerCase();
        CommandConfigParser.SubCommandConfig subConfig = cmdConfig.subcommands().get(sub);
        if (subConfig == null) {

            sender.sendMessage(HexColorUtil.color(cmdConfig.usage()));
            return true;

        }

        if (!subConfig.permission().isEmpty() && !sender.hasPermission(subConfig.permission())) {

            sender.sendMessage(HexColorUtil.color(subConfig.messageNoPermission()));
            return true;

        }

        if (sub.equals("reload")) {

            plugin.reloadPlugin();

            sender.sendMessage(HexColorUtil.color(subConfig.messageSuccess()));
            plugin.getLoggerHelper().info("Command", "Configuration reloaded by %s",
                    sender.getName());

            return true;

        }

        if (!subConfig.message().isEmpty()) {
            sender.sendMessage(HexColorUtil.color(subConfig.message()));
        }

        return true;

    }

    @Nullable @Override
    public List<String> onTabComplete(@NotNull CommandSender sender,
                                      @NotNull Command command,
                                      @NotNull String alias,
                                      @NotNull String[] args) {

        CommandConfigParser.CommandConfig cmdConfig = commands.get(command.getName().toLowerCase());
        if (cmdConfig == null) return Collections.emptyList();

        if (args.length == 1) {

            List<String> completions = new ArrayList<>();

            for (Map.Entry<String, CommandConfigParser.SubCommandConfig> entry
                    : cmdConfig.subcommands().entrySet()) {

                String sub = entry.getKey();
                CommandConfigParser.SubCommandConfig subCfg = entry.getValue();

                if (!subCfg.permission().isEmpty()
                        && !sender.hasPermission(subCfg.permission())) continue;

                completions.add(sub);

            }

            return StringUtil.copyPartialMatches(args[0], completions, new ArrayList<>());

        }

        return Collections.emptyList();

    }
 }
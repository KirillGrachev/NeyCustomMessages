package com.ney.messages.config.parser;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandConfigParser {

    public static @NotNull Map<String, CommandConfig> parse(@NotNull FileConfiguration config) {

        Map<String, CommandConfig> commands = new HashMap<>();

        ConfigurationSection section = config.getConfigurationSection("commands");
        if (section == null) return commands;

        for (String cmd : section.getKeys(false)) {

            ConfigurationSection cmdSec = section.getConfigurationSection(cmd);
            if (cmdSec == null) continue;

            String description = cmdSec.getString("description", "");
            String usage = cmdSec.getString("usage", "");
            String permission = cmdSec.getString("permission", "");

            Map<String, SubCommandConfig> subcommands = new HashMap<>();
            ConfigurationSection subSec = cmdSec.getConfigurationSection("subcommands");

            if (subSec != null) {

                for (String sub : subSec.getKeys(false)) {

                    ConfigurationSection sc = subSec.getConfigurationSection(sub);
                    if (sc == null) continue;

                    subcommands.put(sub, new SubCommandConfig(
                            sc.getString("description", ""),
                            sc.getString("permission", ""),
                            sc.getString("message-success", ""),
                            sc.getString("message-no-permission", ""),
                            sc.getString("message", "")
                    ));

                }

            }

            commands.put(cmd, new CommandConfig(cmd, description, usage, permission, subcommands));

        }

        return commands;

    }

    public record CommandConfig(String name, String description, String usage, String permission,
                                Map<String, SubCommandConfig> subcommands) {}

    public record SubCommandConfig(String description, String permission,
                                   String messageSuccess, String messageNoPermission, String message) {}

}
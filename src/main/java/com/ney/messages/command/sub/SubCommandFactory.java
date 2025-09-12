package com.ney.messages.command.sub;

import com.ney.messages.NeyCustomMessages;
import com.ney.messages.util.LoggerHelper;

import java.util.HashMap;
import java.util.Map;

public class SubCommandFactory {

    private final Map<String, SubCommand> commandRegistry = new HashMap<>();

    public SubCommandFactory(NeyCustomMessages plugin) {

        registerCommand(new ReloadSubCommand(plugin));
        registerCommand(new MessageSubCommand());

    }

    private void registerCommand(SubCommand command) {
        commandRegistry.put(command.getActionKey(), command);
    }

    public SubCommand getCommand(String actionKey) {
        return commandRegistry.get(actionKey);
    }
}
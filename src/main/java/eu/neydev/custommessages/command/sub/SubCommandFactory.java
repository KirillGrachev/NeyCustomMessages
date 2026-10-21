package eu.neydev.custommessages.command.sub;

import eu.neydev.custommessages.NeyCustomMessages;

import java.util.HashMap;
import java.util.Map;

public class SubCommandFactory {

    private final Map<String, SubCommand> commandRegistry = new HashMap<>();
    private final SubCommand fallbackCommand;

    public SubCommandFactory(NeyCustomMessages plugin) {

        registerCommand(new ReloadSubCommand(plugin));
        registerCommand(new MessageSubCommand());

        this.fallbackCommand = new MessageSubCommand();

    }

    private void registerCommand(SubCommand command) {
        commandRegistry.put(command.getActionKey(), command);
    }

    public SubCommand getCommand(String actionKey) {
        return commandRegistry.getOrDefault(actionKey, fallbackCommand);
    }
}
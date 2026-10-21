package eu.neydev.custommessages.command.sub;

import eu.neydev.custommessages.config.parser.CommandConfigParser;
import eu.neydev.custommessages.util.HexColorUtil;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class MessageSubCommand implements SubCommand {

    @Override
    public boolean execute(@NotNull CommandSender sender,
                           @NotNull CommandConfigParser.SubCommandConfig subCommandConfig) {

        if (!subCommandConfig.message().isEmpty()) {
            sender.sendMessage(HexColorUtil.color(subCommandConfig.message()));
        }

        return true;

    }

    @Override
    public String getActionKey() {
        return "message";
    }
}
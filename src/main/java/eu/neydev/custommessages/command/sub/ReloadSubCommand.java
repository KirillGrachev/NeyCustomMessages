package eu.neydev.custommessages.command.sub;

import eu.neydev.custommessages.NeyCustomMessages;
import eu.neydev.custommessages.config.parser.CommandConfigParser;
import eu.neydev.custommessages.util.HexColorUtil;
import eu.neydev.custommessages.util.LoggerHelper;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadSubCommand implements SubCommand {

    private final NeyCustomMessages plugin;
    private final LoggerHelper loggerHelper;

    public ReloadSubCommand(@NotNull NeyCustomMessages plugin) {
        this.loggerHelper = plugin.getLoggerHelper();
        this.plugin = plugin;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender,
                           @NotNull CommandConfigParser.SubCommandConfig subCommandConfig) {

        plugin.reloadPlugin();

        sender.sendMessage(HexColorUtil.color(subCommandConfig.messageSuccess()));
        loggerHelper.info("Command", "Configuration reloaded by %s", sender.getName());

        return true;

    }

    @Override
    public String getActionKey() {
        return "reload";
    }
}
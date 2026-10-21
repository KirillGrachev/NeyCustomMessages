package eu.neydev.custommessages.config.parser;

import eu.neydev.custommessages.config.MainConfig;
import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigParser {
    MainConfig parse(FileConfiguration config);
}
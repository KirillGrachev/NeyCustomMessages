package eu.neydev.custommessages.config.loader;

import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigLoader {
    FileConfiguration load();
}
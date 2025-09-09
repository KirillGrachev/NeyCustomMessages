package com.ney.messages.config.parser;

import com.ney.messages.config.MainConfig;
import org.bukkit.configuration.file.FileConfiguration;

public interface ConfigParser {
    MainConfig parse(FileConfiguration config);
}
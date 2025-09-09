package com.ney.messages.config.loader.yaml;

import com.ney.messages.config.loader.ConfigLoader;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class YamlConfigLoader implements ConfigLoader {

    private final JavaPlugin plugin;

    public YamlConfigLoader(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public FileConfiguration load() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        return YamlConfiguration.loadConfiguration(configFile);
    }
}
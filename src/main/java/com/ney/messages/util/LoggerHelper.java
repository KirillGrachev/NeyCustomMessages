package com.ney.messages.util;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LoggerHelper {

    private final Logger logger;
    private final String pluginName;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    public LoggerHelper(@NotNull JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.pluginName = "[" + plugin.getDescription().getName() + "]";
    }

    public void info(String component, String message, Object... args) {
        log(Level.INFO, component, message, args);
    }

    public void warn(String component, String message, Object... args) {
        log(Level.WARNING, component, message, args);
    }

    public void error(String component, Throwable throwable, String message, Object... args) {

        String formattedMessage = String.format(message, args);
        String timestamp = dateFormat.format(new Date());

        logger.log(Level.SEVERE, String.format("[%s] %s [%s] %s [ERROR]", timestamp, pluginName,
                component, formattedMessage), throwable);

    }

    private void log(Level level, String component, String message, Object... args) {

        String formattedMessage = String.format(message, args);
        String timestamp = dateFormat.format(new Date());

        String logMessage = String.format("[%s] %s [%s] %s", timestamp,
                pluginName, component, formattedMessage);

        logger.log(level, logMessage);

    }
}
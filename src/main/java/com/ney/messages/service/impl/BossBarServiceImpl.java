package com.ney.messages.service.impl;

import com.ney.messages.service.interfaces.IBossBarService;
import com.ney.messages.util.HexColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BossBarServiceImpl implements IBossBarService {

    private final JavaPlugin plugin;
    private final Logger logger;
    private final Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> bossBarTasks = new ConcurrentHashMap<>();

    public BossBarServiceImpl(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    @Override
    public void showBossBar(Player player, String text, BarColor color, BarStyle style, int duration, boolean progressDecay) {

        if (player == null || !player.isOnline()) return;

        UUID playerId = player.getUniqueId();
        removeBossBar(player);

        BossBar bar = Bukkit.createBossBar(HexColorUtil.color(text), color, style);
        bar.addPlayer(player);
        activeBossBars.put(playerId, bar);

        if (progressDecay) {
            startDecayTask(player, bar, duration);
        } else {

            BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin,
                    () -> removeBossBar(player),
                    duration * 20L);

            bossBarTasks.put(playerId, task);

        }
    }

    private void startDecayTask(@NotNull Player player, BossBar bar, int duration) {

        UUID playerId = player.getUniqueId();
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

            if (!player.isOnline()) {

                removeBossBar(player);
                return;

            }

            double progress = bar.getProgress() - (1.0 / (duration * 20));

            if (progress <= 0.0) {
                removeBossBar(player);
            } else {
                bar.setProgress(progress);
            }

        }, 0L, 1L);

        bossBarTasks.put(playerId, task);

    }

    @Override
    public void removeBossBar(Player player) {

        if (player == null) return;

        UUID playerId = player.getUniqueId();
        BossBar bar = activeBossBars.remove(playerId);
        if (bar != null) {
            bar.removeAll();
        }

        BukkitTask task = bossBarTasks.remove(playerId);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

    }

    @Override
    public void removeAllBossBars() {

        activeBossBars.values().forEach(BossBar::removeAll);
        activeBossBars.clear();

        bossBarTasks.values().forEach(task -> {

            if (!task.isCancelled()) {
                task.cancel();
            }

        });

        bossBarTasks.clear();

    }

    @Override
    public void close() {

        try {
            removeAllBossBars();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error closing BossBarService", e);
        }

    }
}
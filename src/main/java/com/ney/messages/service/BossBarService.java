package com.ney.messages.service;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarService {

    private final JavaPlugin plugin;

    private final Map<UUID, BossBar> activeBossBars = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> bossBarTasks = new ConcurrentHashMap<>();

    public BossBarService(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void showBossBar(Player player, String text,
                            BarColor color, BarStyle style,
                            int duration, boolean decay) {

        if (player == null || !player.isOnline()) return;

        UUID uuid = player.getUniqueId();

        // Удаляем старый боссбар, если он есть
        removeBossBar(player);

        BossBar bar = Bukkit.createBossBar(text, color, style);
        bar.addPlayer(player);
        activeBossBars.put(uuid, bar);

        if (decay) {
            startDecayTask(player, bar, duration);
        } else {

            int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                removeBossBar(player);
            }, duration * 20L).getTaskId();

            bossBarTasks.put(uuid, taskId);

        }
    }

    private void startDecayTask(@NotNull Player player, BossBar bar, int duration) {

        UUID uuid = player.getUniqueId();

        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {

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

        }, 0L, 1L).getTaskId();

        bossBarTasks.put(uuid, taskId);

    }

    public void removeBossBar(Player player) {

        if (player == null) return;

        UUID uuid = player.getUniqueId();
        BossBar bar = activeBossBars.remove(uuid);

        if (bar != null) {
            bar.removeAll();
        }

        Integer taskId = bossBarTasks.remove(uuid);

        if (taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId);
        }

    }

    public void removeAllBossBars() {

        activeBossBars.keySet().forEach(playerId -> {

            Player player = Bukkit.getPlayer(playerId);
            if (player != null) {
                removeBossBar(player);
            }

        });

        activeBossBars.clear();

        bossBarTasks.values().forEach(Bukkit.getScheduler()::cancelTask);
        bossBarTasks.clear();

    }
}
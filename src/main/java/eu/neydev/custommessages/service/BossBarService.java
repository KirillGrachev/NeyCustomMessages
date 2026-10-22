package eu.neydev.custommessages.service;

import eu.neydev.custommessages.config.type.BossBarSettings;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Босс-бары плагина: единственная состоятельная часть доставки.
 *
 * На игрока хранится ровно один бар - новый показ заменяет предыдущий
 * (в 1.x так же), иначе модуль и анонс накладывались бы друг на друга.
 * Задача затухания уменьшает заполнение на 1/(duration*20) каждый тик,
 * поэтому полоса опустевает ровно к концу показа. Все бары и задачи
 * снимаются при выключении плагина и при reload: в 1.x забытые бары
 * переживали перезагрузку и висели до конца своей длительности.
 */
public class BossBarService {

    private final Plugin plugin;
    private final Map<UUID, BossBar> activeBars = new ConcurrentHashMap<>();
    private final Map<UUID, BukkitTask> barTasks = new ConcurrentHashMap<>();

    public BossBarService(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Показывает игроку босс-бар, заменяя предыдущий.
     *
     * @param player   получатель (null или оффлайн - тихо пропускается)
     * @param text     готовый (окрашенный) текст полосы
     * @param settings настройки показа
     */
    public void show(@Nullable Player player, @NotNull String text,
                     @NotNull BossBarSettings settings) {

        if (player == null || !player.isOnline()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        remove(player);

        BossBar bar = plugin.getServer().createBossBar(text, settings.color(), settings.style());
        bar.addPlayer(player);
        activeBars.put(playerId, bar);

        if (settings.progressDecay()) {
            startDecayTask(player, bar, settings.duration());
        } else {
            barTasks.put(playerId, plugin.getServer().getScheduler().runTaskLater(plugin,
                    () -> remove(player), settings.duration() * 20L));
        }

    }

    /**
     * Убирает бар игрока и отменяет его задачу.
     *
     * @param player игрок
     */
    public void remove(@Nullable Player player) {

        if (player == null) {
            return;
        }

        UUID playerId = player.getUniqueId();

        BossBar bar = activeBars.remove(playerId);
        if (bar != null) {
            bar.removeAll();
        }

        BukkitTask task = barTasks.remove(playerId);
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }

    }

    /**
     * Убирает все бары и отменяет все задачи: вызывается на reload
     * и на выключении плагина.
     */
    public void removeAll() {

        activeBars.values().forEach(BossBar::removeAll);
        activeBars.clear();

        barTasks.values().forEach(task -> {
            if (!task.isCancelled()) {
                task.cancel();
            }
        });
        barTasks.clear();

    }

    private void startDecayTask(@NotNull Player player, @NotNull BossBar bar, int duration) {

        UUID playerId = player.getUniqueId();

        BukkitTask task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {

            if (!player.isOnline()) {

                remove(player);
                return;

            }

            double progress = bar.getProgress() - (1.0 / (duration * 20));

            if (progress <= 0.0) {
                remove(player);
            } else {
                bar.setProgress(progress);
            }

        }, 0L, 1L);

        barTasks.put(playerId, task);

    }

}

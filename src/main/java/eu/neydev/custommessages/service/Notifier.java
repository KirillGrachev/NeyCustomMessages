package eu.neydev.custommessages.service;

import eu.neydev.custommessages.config.type.BossBarSettings;
import eu.neydev.custommessages.config.type.ChatSettings;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.config.type.SoundSettings;
import eu.neydev.custommessages.config.type.TitleSettings;
import eu.neydev.custommessages.util.HexColorUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Единственная точка доставки уведомлений.
 *
 * В 1.x доставку одного уведомления обслуживали четыре стратегии, три
 * мини-сервиса и контекст, который каждая стратегия разбирала цепочкой
 * instanceof. Здесь весь конвейер в одном классе: чат модуля уходит
 * broadcastMessage всем (включая консоль), чат анонса строится в
 * компоненты ОДИН раз на рассылку и уходит каждому игроку лично (hover и
 * click живут только в компонентах), а титр, звук и босс-бар доставляются
 * адресно. Тексты окрашиваются после подстановки плейсхолдеров - так
 * значение плейсхолдера не может внести в сообщение сырой цветовой код,
 * порядок тот же, что в 1.x.
 */
public class Notifier {

    private final Plugin plugin;
    private final BossBarService bossBars;

    public Notifier(@NotNull Plugin plugin, @NotNull BossBarService bossBars) {
        this.plugin = plugin;
        this.bossBars = bossBars;
    }

    /**
     * Доставляет уведомление модуля: чат - всем, личные выводы - игроку события.
     *
     * @param player       игрок, о котором событие (получатель титра, звука и бара)
     * @param bundle       комплект выводов модуля
     * @param placeholders подстановки вида {player} -> имя
     */
    public void notifyModule(@Nullable Player player,
                             @NotNull NotificationBundle bundle,
                             @NotNull Map<String, String> placeholders) {

        broadcastModuleChat(bundle.chat(), placeholders);

        if (player == null || !player.isOnline()) {
            return;
        }

        deliverTitle(player, bundle.title(), placeholders);
        deliverSound(player, bundle.sound());
        deliverBossBar(player, bundle.bossBar(), placeholders);

    }

    /**
     * Доставляет анонс каждому игроку в сети.
     *
     * Плейсхолдеров у анонсов нет: они рассылаются как есть, контекста
     * игрока у объявления не существует.
     *
     * @param bundle комплект выводов анонса
     */
    public void broadcast(@NotNull NotificationBundle bundle) {

        BaseComponent[] components = buildComponents(bundle.chat());

        for (Player player : plugin.getServer().getOnlinePlayers()) {

            if (components != null) {
                player.spigot().sendMessage(components);
            }

            deliverTitle(player, bundle.title(), Map.of());
            deliverSound(player, bundle.sound());
            deliverBossBar(player, bundle.bossBar(), Map.of());

        }

    }

    private void broadcastModuleChat(@NotNull ChatSettings chat,
                                     @NotNull Map<String, String> placeholders) {

        if (!chat.enabled() || chat.format().isEmpty()) {
            return;
        }

        plugin.getServer().broadcastMessage(
                HexColorUtil.color(apply(chat.format(), placeholders)));

    }

    /**
     * Собирает компоненты чата анонса или возвращает null, если чат выключен.
     *
     * Hover и click уже проверены при загрузке конфигурации: неизвестное
     * действие или не-URL оттуда не доезжают.
     */
    private @Nullable BaseComponent[] buildComponents(@NotNull ChatSettings chat) {

        if (!chat.enabled() || chat.format().isEmpty()) {
            return null;
        }

        TextComponent component = new TextComponent(HexColorUtil.color(chat.format()));

        if (chat.hover() != null) {

            component.setHoverEvent(new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(HexColorUtil.color(chat.hover())).create()));

        }

        if (chat.clickAction() != null && chat.clickValue() != null) {
            component.setClickEvent(new ClickEvent(chat.clickAction(), chat.clickValue()));
        }

        return new BaseComponent[]{component};

    }

    private void deliverTitle(@NotNull Player player, @NotNull TitleSettings title,
                              @NotNull Map<String, String> placeholders) {

        if (!title.enabled() || !title.hasContent()) {
            return;
        }

        player.sendTitle(
                HexColorUtil.color(apply(title.title(), placeholders)),
                HexColorUtil.color(apply(title.subtitle(), placeholders)),
                title.fadeIn(),
                title.stay(),
                title.fadeOut()
        );

    }

    private void deliverSound(@NotNull Player player, @NotNull SoundSettings sound) {

        if (!sound.isPlayable()) {
            return;
        }

        player.playSound(player.getLocation(), sound.sound(), sound.volume(), sound.pitch());

    }

    private void deliverBossBar(@NotNull Player player, @NotNull BossBarSettings bar,
                                @NotNull Map<String, String> placeholders) {

        if (!bar.enabled() || bar.text().isEmpty()) {
            return;
        }

        bossBars.show(player, HexColorUtil.color(apply(bar.text(), placeholders)), bar);

    }

    private @NotNull String apply(@NotNull String text, @NotNull Map<String, String> placeholders) {

        String result = text;

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace(entry.getKey(), entry.getValue());
        }

        return result;

    }

}

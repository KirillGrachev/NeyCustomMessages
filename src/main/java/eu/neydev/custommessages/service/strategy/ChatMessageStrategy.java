package eu.neydev.custommessages.service.strategy;

import eu.neydev.custommessages.config.type.ChatSettings;
import eu.neydev.custommessages.service.strategy.context.NotificationContext;
import eu.neydev.custommessages.util.HexColorUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Стратегия доставки сообщения в чат.
 *
 * Два маршрута, как в 1.x: сообщения модулей (join, quit, death) уходят
 * broadcastMessage всем, включая консоль, а анонсы - компонентным
 * сообщением лично каждому игроку, потому что hover и click существуют
 * только в компонентах. Значения hover/click проверены при загрузке
 * конфигурации, поэтому здесь они просто применяются.
 */
public class ChatMessageStrategy implements NotificationStrategy {

    @Override
    public void execute(@NotNull NotificationContext context) {

        ChatSettings chat = context.getBundle().chat();

        if (!chat.enabled() || chat.format().isEmpty()) {
            return;
        }

        String formatted = context.apply(chat.format());

        if (!context.isAnnouncement()) {

            Bukkit.broadcastMessage(HexColorUtil.color(formatted));
            return;

        }

        Player player = context.getPlayer();

        if (player == null || !player.isOnline()) {
            return;
        }

        player.spigot().sendMessage(buildComponent(formatted, chat));

    }

    @Override
    public boolean isEnabled(@NotNull NotificationContext context) {

        ChatSettings chat = context.getBundle().chat();

        return chat.enabled() && !chat.format().isEmpty();

    }

    private BaseComponent @NotNull [] buildComponent(@NotNull String formatted,
                                                     @NotNull ChatSettings chat) {

        TextComponent component = new TextComponent(HexColorUtil.color(formatted));

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

}

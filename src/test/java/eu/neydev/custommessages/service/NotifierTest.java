package eu.neydev.custommessages.service;

import eu.neydev.custommessages.config.type.BossBarSettings;
import eu.neydev.custommessages.config.type.ChatSettings;
import eu.neydev.custommessages.config.type.NotificationBundle;
import eu.neydev.custommessages.config.type.SoundSettings;
import eu.neydev.custommessages.config.type.TitleSettings;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Доставка уведомлений: маршрут чата (broadcast для модулей, компоненты для
 * анонсов), адресные титр/звук/босс-бар, подстановка плейсхолдеров до
 * окраски и отказы на выключенных выводах.
 */
class NotifierTest {

    private final Plugin plugin = mock(Plugin.class);
    private final Server server = mock(Server.class);
    private final Player player = mock(Player.class);
    private final Player.Spigot spigot = mock(Player.Spigot.class);
    private final BossBarService bossBars = mock(BossBarService.class);
    private final org.bukkit.Location location = mock(org.bukkit.Location.class);

    private Notifier notifier;

    @BeforeEach
    void setUp() {

        when(plugin.getServer()).thenReturn(server);
        when(player.getName()).thenReturn("Steve");
        when(player.isOnline()).thenReturn(true);
        when(player.spigot()).thenReturn(spigot);
        when(player.getLocation()).thenReturn(location);
        doReturn(List.of(player)).when(server).getOnlinePlayers();

        notifier = new Notifier(plugin, bossBars);

    }

    private NotificationBundle bundle(ChatSettings chat, TitleSettings title,
                                      SoundSettings sound, BossBarSettings bar) {
        return new NotificationBundle(chat, title, sound, bar);
    }

    @Test
    @DisplayName("Чат модуля уходит broadcastMessage всем с подстановкой и окраской")
    void moduleChatIsBroadcastColored() {

        ChatSettings chat = new ChatSettings(true, "&a[+] &e{player}", null, null, null);

        notifier.notifyModule(player,
                bundle(chat, TitleSettings.EMPTY, SoundSettings.EMPTY, BossBarSettings.EMPTY),
                Map.of("{player}", "Steve"));

        verify(server).broadcastMessage("§a[+] §eSteve");

    }

    @Test
    @DisplayName("Выключенный или пустой чат не рассылается")
    void disabledOrEmptyChatIsNotSent() {

        notifier.notifyModule(player,
                bundle(new ChatSettings(false, "text", null, null, null),
                        TitleSettings.EMPTY, SoundSettings.EMPTY, BossBarSettings.EMPTY),
                Map.of());
        notifier.notifyModule(player,
                bundle(new ChatSettings(true, "", null, null, null),
                        TitleSettings.EMPTY, SoundSettings.EMPTY, BossBarSettings.EMPTY),
                Map.of());

        verify(server, never()).broadcastMessage(anyString());

    }

    @Test
    @DisplayName("Титр доставляется адресно с подстановкой в оба текста")
    void titleIsDeliveredWithPlaceholders() {

        TitleSettings title = new TitleSettings(true, "&c{player} died", "&7bye", 5, 60, 15);

        notifier.notifyModule(player,
                bundle(ChatSettings.EMPTY, title, SoundSettings.EMPTY, BossBarSettings.EMPTY),
                Map.of("{player}", "Steve"));

        verify(player).sendTitle("§cSteve died", "§7bye", 5, 60, 15);

    }

    @Test
    @DisplayName("Пустой титр из обоих строк не отправляется")
    void emptyTitleIsSkipped() {

        notifier.notifyModule(player,
                bundle(ChatSettings.EMPTY, new TitleSettings(true, "", "", 1, 1, 1),
                        SoundSettings.EMPTY, BossBarSettings.EMPTY),
                Map.of());

        verify(player, never()).sendTitle(anyString(), anyString(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt());

    }

    @Test
    @DisplayName("Звук играет только при разрешенном имени")
    void soundPlaysOnlyWhenResolved() {

        SoundSettings playable = new SoundSettings(true, Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);

        notifier.notifyModule(player,
                bundle(ChatSettings.EMPTY, TitleSettings.EMPTY, playable, BossBarSettings.EMPTY),
                Map.of());

        verify(player).playSound(eq(location), eq(Sound.ENTITY_PLAYER_LEVELUP), eq(0.8f), eq(1.2f));

        notifier.notifyModule(player,
                bundle(ChatSettings.EMPTY, TitleSettings.EMPTY,
                        new SoundSettings(true, null, 1.0f, 1.0f), BossBarSettings.EMPTY),
                Map.of());

        verify(player, never()).playSound(eq(location), eq(Sound.BLOCK_NOTE_BLOCK_PLING), anyFloat(), anyFloat());

    }

    @Test
    @DisplayName("Босс-бар уходит в сервис с готовым текстом и настройками")
    void bossBarGoesThroughTheService() {

        BossBarSettings bar = new BossBarSettings(true, "&aHello, {player}!",
                BarColor.GREEN, BarStyle.SEGMENTED_6, 5, true);

        notifier.notifyModule(player,
                bundle(ChatSettings.EMPTY, TitleSettings.EMPTY, SoundSettings.EMPTY, bar),
                Map.of("{player}", "Steve"));

        verify(bossBars).show(eq(player), eq("§aHello, Steve!"), eq(bar));

    }

    @Test
    @DisplayName("Анонс уходит каждому игроку компонентами с hover и click")
    void announcementIsSentAsComponents() {

        ChatSettings chat = new ChatSettings(true, "&6[AD] sale",
                "&aClick me", ClickEvent.Action.OPEN_URL, "https://example.com");

        notifier.broadcast(bundle(chat, TitleSettings.EMPTY, SoundSettings.EMPTY,
                BossBarSettings.EMPTY));

        ArgumentCaptor<BaseComponent[]> components = ArgumentCaptor.forClass(BaseComponent[].class);

        verify(spigot).sendMessage(components.capture());

        TextComponent component = (TextComponent) components.getValue()[0];

        assertEquals("§6[AD] sale", component.toPlainText());
        assertNotNull(component.getHoverEvent(), "hover на месте");
        assertNotNull(component.getClickEvent(), "click на месте");
        assertEquals("https://example.com", component.getClickEvent().getValue());

    }

    @Test
    @DisplayName("Анонс без интерактива все равно идет компонентами, а не broadcast")
    void announcementWithoutInteractionStillUsesComponents() {

        ChatSettings chat = new ChatSettings(true, "&6plain ad", null, null, null);

        notifier.broadcast(bundle(chat, TitleSettings.EMPTY, SoundSettings.EMPTY,
                BossBarSettings.EMPTY));

        verify(spigot).sendMessage(any(BaseComponent[].class));
        verify(server, never()).broadcastMessage(anyString());

    }

    @Test
    @DisplayName("Личные выводы анонса доходят до игрока")
    void announcementPersonalOutputsAreDelivered() {

        TitleSettings title = new TitleSettings(true, "&6Attention!", "&esub", 10, 80, 20);
        SoundSettings sound = new SoundSettings(true, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        BossBarSettings bar = new BossBarSettings(true, "&6bar",
                BarColor.YELLOW, BarStyle.SOLID, 8, false);

        notifier.broadcast(bundle(ChatSettings.EMPTY, title, sound, bar));

        verify(player).sendTitle("§6Attention!", "§esub", 10, 80, 20);
        verify(player).playSound(eq(location), eq(Sound.BLOCK_NOTE_BLOCK_PLING), eq(1.0f), eq(1.0f));
        verify(bossBars).show(eq(player), eq("§6bar"), eq(bar));

    }

    @Test
    @DisplayName("Оффлайн-игрок не получает адресных выводов модуля, чат рассылается")
    void offlinePlayerSkipsPersonalOutputs() {

        when(player.isOnline()).thenReturn(false);
        TitleSettings title = new TitleSettings(true, "t", "s", 1, 1, 1);
        ChatSettings chat = new ChatSettings(true, "hello all", null, null, null);

        notifier.notifyModule(player,
                bundle(chat, title, SoundSettings.EMPTY, BossBarSettings.EMPTY), Map.of());

        verify(server).broadcastMessage("hello all");
        verify(player, never()).sendTitle(anyString(), anyString(),
                org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.anyInt(),
                org.mockito.ArgumentMatchers.anyInt());

    }

    @Test
    @DisplayName("Null-игрок модуля не роняет доставку чата")
    void nullPlayerDoesNotBreakModuleChat() {

        ChatSettings chat = new ChatSettings(true, "system note", null, null, null);

        notifier.notifyModule(null,
                bundle(chat, TitleSettings.EMPTY, SoundSettings.EMPTY, BossBarSettings.EMPTY),
                Map.of());

        verify(server).broadcastMessage("system note");

    }

    @Test
    @DisplayName("HEX-код из значения плейсхолдера окрашивается: подстановка раньше окраски")
    void placeholderHexIsColoredAfterSubstitution() {

        ChatSettings chat = new ChatSettings(true, "{cause}", null, null, null);

        notifier.notifyModule(player,
                bundle(chat, TitleSettings.EMPTY, SoundSettings.EMPTY, BossBarSettings.EMPTY),
                Map.of("{cause}", "#ff0000lava"));

        ArgumentCaptor<String> message = ArgumentCaptor.forClass(String.class);

        verify(server).broadcastMessage(message.capture());
        assertTrue(message.getValue().startsWith("§x§f§f§0§0§0§0lava"), message.getValue());

    }

    @Test
    @DisplayName("Пустой список игроков не роняет рассылку анонса")
    void emptyAudienceIsSurvived() {

        doReturn(List.of()).when(server).getOnlinePlayers();

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                notifier.broadcast(bundle(new ChatSettings(true, "text", null, null, null),
                        TitleSettings.EMPTY, SoundSettings.EMPTY, BossBarSettings.EMPTY)));

    }

}

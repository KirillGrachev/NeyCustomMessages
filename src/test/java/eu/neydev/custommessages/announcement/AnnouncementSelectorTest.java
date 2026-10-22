package eu.neydev.custommessages.announcement;

import eu.neydev.custommessages.config.type.Announcement;
import eu.neydev.custommessages.config.type.NotificationBundle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Выбор следующего анонса: круговой порядок без переполнения и случайный
 * порядок на управляемом генераторе.
 */
class AnnouncementSelectorTest {

    private final List<Announcement> announcements = List.of(
            new Announcement("a", NotificationBundle.EMPTY),
            new Announcement("b", NotificationBundle.EMPTY),
            new Announcement("c", NotificationBundle.EMPTY));

    @Test
    @DisplayName("Круговой порядок идет по списку и зацикливается")
    void roundRobinCycles() {

        AnnouncementSelector selector = new AnnouncementSelector(new Random(42));

        assertEquals("a", selector.selectNext(announcements, false).id());
        assertEquals("b", selector.selectNext(announcements, false).id());
        assertEquals("c", selector.selectNext(announcements, false).id());
        assertEquals("a", selector.selectNext(announcements, false).id(), "второй круг");

    }

    @Test
    @DisplayName("Круговой счетчик переживает переполнение: floorMod вместо Math.abs")
    void roundRobinSurvivesOverflow() {

        // Старт ровно перед границей: старый Math.abs на Integer.MIN_VALUE
        // вернул бы отрицательный индекс и ротация упала бы с
        // ArrayIndexOutOfBoundsException - floorMod проходит границу молча
        AnnouncementSelector selector = new AnnouncementSelector(null, Integer.MAX_VALUE);

        assertEquals("b", selector.selectNext(announcements, false).id(),
                "floorMod(MAX_VALUE, 3) = 1");
        assertEquals("b", selector.selectNext(announcements, false).id(),
                "счетчик переполнился в MIN_VALUE, floorMod(MIN_VALUE, 3) = 1");
        assertEquals("c", selector.selectNext(announcements, false).id(),
                "ротация продолжается");

    }

    @Test
    @DisplayName("Случайный порядок использует инжектированный генератор")
    void randomOrderUsesInjectedGenerator() {

        Random random = new Random(7);
        AnnouncementSelector selector = new AnnouncementSelector(random);

        int expectedIndex = new Random(7).nextInt(3);

        assertSame(announcements.get(expectedIndex), selector.selectNext(announcements, true));

    }

    @Test
    @DisplayName("Случайный порядок остается в границах списка")
    void randomOrderStaysInBounds() {

        AnnouncementSelector selector = new AnnouncementSelector();

        for (int i = 0; i < 100; i++) {
            assertTrue(announcements.contains(selector.selectNext(announcements, true)));
        }

    }

    @Test
    @DisplayName("Список из одного анонса всегда возвращает его")
    void singleAnnouncementAlwaysWins() {

        AnnouncementSelector selector = new AnnouncementSelector();
        List<Announcement> single = List.of(announcements.get(1));

        assertSame(single.get(0), selector.selectNext(single, false));
        assertSame(single.get(0), selector.selectNext(single, true));

    }

}

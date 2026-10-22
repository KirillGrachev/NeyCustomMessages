package eu.neydev.custommessages.announcement;

import eu.neydev.custommessages.config.type.Announcement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Выбор следующего анонса: по кругу или случайно.
 *
 * Круговой счетчик в 1.x использовал Math.abs(getAndIncrement()): на
 * 2^31-м анонсе счетчик переполняется, Math.abs(Integer.MIN_VALUE)
 * возвращает тот же MIN_VALUE, и ротация падает с
 * ArrayIndexOutOfBoundsException. floorMod корректен при любом значении
 * счетчика. Генератор для случайного порядка - поточный: общее поле
 * Random на задачу ротации не нужно, а в тестах конструктор принимает
 * управляемый генератор.
 */
public class AnnouncementSelector {

    private final @Nullable Random injectedRandom;
    private final AtomicInteger counter = new AtomicInteger();

    public AnnouncementSelector() {
        this(null);
    }

    AnnouncementSelector(@Nullable Random injectedRandom) {
        this.injectedRandom = injectedRandom;
    }

    /**
     * Возвращает следующий анонс ротации.
     *
     * @param announcements непустой список анонсов
     * @param randomOrder   true - случайный выбор, false - по кругу
     * @return выбранный анонс
     */
    public @NotNull Announcement selectNext(@NotNull List<Announcement> announcements,
                                            boolean randomOrder) {

        if (randomOrder) {

            int index = injectedRandom != null
                    ? injectedRandom.nextInt(announcements.size())
                    : ThreadLocalRandom.current().nextInt(announcements.size());
            return announcements.get(index);

        }

        return announcements.get(Math.floorMod(counter.getAndIncrement(), announcements.size()));

    }

}

package eu.neydev.custommessages.service.impl;

import eu.neydev.custommessages.service.interfaces.ISoundService;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoundServiceImpl implements ISoundService {

    @Override
    public void playSound(@Nullable Player player, @Nullable Sound sound,
                          float volume, float pitch) {

        if (player == null || !player.isOnline() || sound == null) {
            return;
        }

        player.playSound(player.getLocation(), sound, volume, pitch);

    }

}

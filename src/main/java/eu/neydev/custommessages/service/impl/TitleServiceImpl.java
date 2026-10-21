package eu.neydev.custommessages.service.impl;

import eu.neydev.custommessages.service.interfaces.ITitleService;
import eu.neydev.custommessages.util.HexColorUtil;
import org.bukkit.entity.Player;

public class TitleServiceImpl implements ITitleService {

    @Override
    public void sendTitle(Player player, String title, String subtitle,
                          int fadeIn, int stay, int fadeOut) {

        if (player == null || !player.isOnline()) {
            return;
        }

        player.sendTitle(
                HexColorUtil.color(title),
                HexColorUtil.color(subtitle),
                fadeIn, stay, fadeOut
        );

    }
}
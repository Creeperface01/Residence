package com.bekvon.bukkit.residence.permissions;

import cn.nukkit.Player;
import ru.nukkit.multipass.Multipass;

/**
 * @author CreeperFace
 */
public class MultiPassAdapter implements PermissionsInterface {

    @Override
    public String getPlayerGroup(Player player) {
        return Multipass.getGroup(player);
    }

    @Override
    public String getPlayerGroup(String player, String world) {
        return Multipass.getGroup(player);
    }
}

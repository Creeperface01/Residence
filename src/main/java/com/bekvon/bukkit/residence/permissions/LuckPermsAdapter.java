package com.bekvon.bukkit.residence.permissions;

import cn.nukkit.Player;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.User;

/**
 * @author CreeperFace
 */
public class LuckPermsAdapter implements PermissionsInterface {


    @Override
    public String getPlayerGroup(Player player) {
        return getPlayerGroup(player.getName(), null);
    }

    @Override
    public String getPlayerGroup(String player, String world) {
        User user = LuckPerms.getApi().getUser(player);

        if (user == null) {
            return null;
        }

        return user.getPrimaryGroup();
    }
}

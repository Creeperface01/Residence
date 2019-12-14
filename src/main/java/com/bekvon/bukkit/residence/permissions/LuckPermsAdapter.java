package com.bekvon.bukkit.residence.permissions;

import cn.nukkit.Player;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

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
        User user = LuckPermsProvider.get().getUserManager().getUser(player);

        if (user == null) {
            return null;
        }

        return user.getPrimaryGroup();
    }
}

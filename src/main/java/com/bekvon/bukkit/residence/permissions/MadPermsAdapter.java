package com.bekvon.bukkit.residence.permissions;

import cn.nukkit.Player;
import me.imjack.permissions.MadPerms;
import me.imjack.permissions.api.MadPermsAPI;

/**
 * Created by CreeperFace on 22.5.2017.
 */
public class MadPermsAdapter implements PermissionsInterface {

    private MadPermsAPI api;

    public MadPermsAdapter() {
        this.api = MadPerms.getPlugin().getAPI();
    }

    @Override
    public String getPlayerGroup(String player, String world) {
        return api.getGroup(player).getName();
    }

    @Override
    public String getPlayerGroup(Player player) {
        return getPlayerGroup(player.getName(), null);
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.permissions;

import cn.nukkit.Player;

/**
 * @author Administrator
 */
public interface PermissionsInterface {

    String getPlayerGroup(Player player);

    String getPlayerGroup(String player, String world);
}

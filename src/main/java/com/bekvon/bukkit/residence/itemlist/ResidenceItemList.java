/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.itemlist;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

import java.util.Map;

/**
 * @author Administrator
 */
public class ResidenceItemList extends ItemList {

    ClaimedResidence res;

    public ResidenceItemList(ClaimedResidence parent, ListType type) {
        super(type);
        res = parent;
    }

    private ResidenceItemList() {

    }

    public void playerListChange(Player player, int mat, boolean resadmin) {
        PermissionGroup group = Residence.getPermissionManager().getGroup(player);
        if (resadmin || (res.getPermissions().hasResidencePermission(player, true) && group.itemListAccess())) {
            if (super.toggle(mat)) {
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("ListItemAdd", TextFormat.GREEN + ItemList.getNameFromId(mat) + TextFormat.YELLOW + "." + TextFormat.GREEN + type.toString().toLowerCase() + TextFormat.YELLOW));
            } else {
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("ListItemRemove", TextFormat.GREEN + ItemList.getNameFromId(mat) + TextFormat.YELLOW + "." + TextFormat.GREEN + type.toString().toLowerCase() + TextFormat.YELLOW));
            }
        } else {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
        }
    }

    public static ResidenceItemList load(ClaimedResidence parent, Map<String, Object> map) {
        ResidenceItemList newlist = new ResidenceItemList();
        newlist.res = parent;
        return (ResidenceItemList) ItemList.load(map, newlist);
    }
}

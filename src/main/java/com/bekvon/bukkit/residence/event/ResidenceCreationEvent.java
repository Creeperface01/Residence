/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;

/**
 * @author Administrator
 */
public class ResidenceCreationEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    protected String resname;
    CuboidArea area;

    public ResidenceCreationEvent(Player player, String newname, ClaimedResidence resref, CuboidArea resarea) {
        super("RESIDENCE_CREATE", resref, player);
        resname = newname;
        area = resarea;
    }

    public String getResidenceName() {
        return resname;
    }

    public void setResidenceName(String name) {
        resname = name;
    }

    public CuboidArea getPhysicalArea() {
        return area;
    }

    public void setPhysicalArea(CuboidArea newarea) {
        area = newarea;
    }
}

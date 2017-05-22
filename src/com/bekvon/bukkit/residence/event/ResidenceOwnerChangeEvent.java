/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import cn.nukkit.event.HandlerList;

/**
 *
 * @author Administrator
 */
public class ResidenceOwnerChangeEvent extends ResidenceEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    protected String newowner;

    public ResidenceOwnerChangeEvent(ClaimedResidence resref, String newOwner) {
        super("RESIDENCE_OWNER_CHANGE", resref);
        newowner = newOwner;
    }

    public String getNewOwner() {
        return newowner;
    }
}

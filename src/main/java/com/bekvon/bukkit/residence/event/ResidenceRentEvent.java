/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * @author Administrator
 */
public class ResidenceRentEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    RentEventType eventtype;

    public enum RentEventType {
        RENT, UNRENT, RENTABLE, UNRENTABLE, RENT_EXPIRE
    }

    public ResidenceRentEvent(ClaimedResidence resref, Player player, RentEventType type) {
        super("RESIDENCE_RENT_EVENT", resref, player);
        eventtype = type;
    }

    public RentEventType getCause() {
        return eventtype;
    }

}

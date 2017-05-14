/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

/**
 *
 * @author Administrator
 */
public class ResidenceEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private String message;

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    ClaimedResidence res;

    public ResidenceEvent(String eventName, ClaimedResidence resref) {
        message = eventName;
        res = resref;
    }

    public String getMessage() {
        return message;
    }

    public ClaimedResidence getResidence() {
        return res;
    }
}

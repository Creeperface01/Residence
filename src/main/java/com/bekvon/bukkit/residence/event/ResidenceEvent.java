/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import cn.nukkit.event.Event;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * @author Administrator
 */
public class ResidenceEvent extends Event {

    private String message;

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

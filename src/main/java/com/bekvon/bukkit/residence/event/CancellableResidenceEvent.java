/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bekvon.bukkit.residence.event;

import cn.nukkit.event.Cancellable;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * @author Administrator
 */
public class CancellableResidenceEvent extends ResidenceEvent implements Cancellable {

    public CancellableResidenceEvent(String eventName, ClaimedResidence resref) {
        super(eventName, resref);
    }
}

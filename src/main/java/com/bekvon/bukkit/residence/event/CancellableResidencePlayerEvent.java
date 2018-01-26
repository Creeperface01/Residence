/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * @author Administrator
 */
public class CancellableResidencePlayerEvent extends ResidencePlayerEvent implements Cancellable {

    public CancellableResidencePlayerEvent(String eventName, ClaimedResidence resref, Player player) {
        super(eventName, resref, player);
    }
}

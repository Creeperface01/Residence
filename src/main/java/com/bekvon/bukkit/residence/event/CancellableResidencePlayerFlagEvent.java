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
public class CancellableResidencePlayerFlagEvent extends ResidencePlayerFlagEvent implements Cancellable {

    public CancellableResidencePlayerFlagEvent(String eventName, ClaimedResidence resref, Player player, String flag, FlagType type, String target) {
        super(eventName, resref, player, flag, type, target);
    }
}

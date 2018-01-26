/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

/**
 * @author Administrator
 */
public class ResidenceFlagChangeEvent extends CancellableResidencePlayerFlagEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    FlagState newstate;

    public ResidenceFlagChangeEvent(ClaimedResidence resref, Player player, String flag, FlagType type, FlagState newState, String target) {
        super("RESIDENCE_FLAG_CHANGE", resref, player, flag, type, target);
        newstate = newState;
    }

    public FlagState getNewState() {
        return newstate;
    }

}

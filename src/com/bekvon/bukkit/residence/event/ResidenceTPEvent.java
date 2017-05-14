/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import cn.nukkit.level.Location;
import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;

/**
 *
 * @author Administrator
 */
public class ResidenceTPEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    Player reqPlayer;
    Location loc;

    public ResidenceTPEvent(ClaimedResidence resref, Location teleloc, Player player, Player reqplayer) {
        super("RESIDENCE_TP", resref, player);
        reqPlayer = reqplayer;
        loc = teleloc;
    }

    public Player getRequestingPlayer() {
        return reqPlayer;
    }

    public Location getTeleportLocation() {
        return loc;
    }
}

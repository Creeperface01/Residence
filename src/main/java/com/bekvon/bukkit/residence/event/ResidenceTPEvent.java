/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Position;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * @author Administrator
 */
public class ResidenceTPEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    Player reqPlayer;
    Position loc;

    public ResidenceTPEvent(ClaimedResidence resref, Position teleloc, Player player, Player reqplayer) {
        super("RESIDENCE_TP", resref, player);
        reqPlayer = reqplayer;
        loc = teleloc;
    }

    public Player getRequestingPlayer() {
        return reqPlayer;
    }

    public Position getTeleportLocation() {
        return loc;
    }
}

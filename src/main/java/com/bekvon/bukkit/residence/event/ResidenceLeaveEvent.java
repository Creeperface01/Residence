/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * Note: This event has been replaced with {@link ResidenceChangedEvent} and is
 * marked as deprecated as of 21-MAY-2013. It will be removed in future
 * releases. Please see {@link ResidenceChangedEvent} comments for further
 * information.
 * <p>
 * TODO - Remove this class at a suitable time in the future.
 *
 * @author Administrator
 */
@Deprecated
public class ResidenceLeaveEvent extends ResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    public ResidenceLeaveEvent(ClaimedResidence resref, Player player) {
        super("RESIDENCE_LEAVE", resref, player);
    }
}

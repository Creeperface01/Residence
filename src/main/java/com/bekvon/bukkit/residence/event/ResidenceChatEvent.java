/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import cn.nukkit.Player;
import cn.nukkit.event.HandlerList;
import cn.nukkit.utils.TextFormat;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 * @author Administrator
 */
public class ResidenceChatEvent extends CancellableResidencePlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    protected String message;
    TextFormat color;

    public ResidenceChatEvent(ClaimedResidence resref, Player player, String message, TextFormat color) {
        super("RESIDENCE_CHAT_EVENT", resref, player);
        this.message = message;
        this.color = color;
    }

    public String getChatMessage() {
        return message;
    }

    public void setChatMessage(String newmessage) {
        message = newmessage;
    }

    public TextFormat getColor() {
        return color;
    }

    public void setColor(TextFormat c) {
        color = c;
    }
}

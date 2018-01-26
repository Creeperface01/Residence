/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.event;

import cn.nukkit.command.CommandSender;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.Event;
import cn.nukkit.event.HandlerList;

/**
 * @author Administrator
 */
public class ResidenceCommandEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    protected boolean cancelled;
    protected String cmd;
    protected String arglist[];
    CommandSender commandsender;

    public ResidenceCommandEvent(String command, String args[], CommandSender sender) {
        super();
        cancelled = false;
        arglist = args;
        cmd = command;
        commandsender = sender;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean bln) {
        cancelled = bln;
    }

    public String getCommand() {
        return cmd;
    }

    public String[] getArgs() {
        return arglist;
    }

    public CommandSender getSender() {
        return commandsender;
    }

}

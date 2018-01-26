package com.bekvon.bukkit.residence.event;

import cn.nukkit.event.HandlerList;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

public class ResidenceRenameEvent extends ResidenceEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    protected String NewResName;
    protected String OldResName;
    protected ClaimedResidence res;

    public ResidenceRenameEvent(ClaimedResidence resref, String NewName, String OldName) {
        super("RESIDENCE_RENAME", resref);
        NewResName = NewName;
        OldResName = OldName;
        res = resref;
    }

    public String getNewResidenceName() {
        return NewResName;
    }

    public String getOldResidenceName() {
        return OldResName;
    }

    public ClaimedResidence getResidence() {
        return res;
    }
}

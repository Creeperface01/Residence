/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.permissions;

import cn.nukkit.Player;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.protection.FlagPermissions.FlagState;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Administrator
 * <p>
 * changed by inori 03/17/2012 line 91:limits MaxHeight changed to 255
 */
public class PermissionGroup {

    protected int xmax;
    protected int ymax;
    protected int zmax;
    protected int resmax;
    protected double costperarea;
    protected boolean tpaccess;
    protected int subzonedepth;
    protected FlagPermissions flagPerms;
    protected Map<String, Boolean> creatorDefaultFlags;
    protected Map<String, Map<String, Boolean>> groupDefaultFlags;
    protected Map<String, Boolean> residenceDefaultFlags;
    protected boolean messageperms;
    protected String defaultEnterMessage;
    protected String defaultLeaveMessage;
    protected int maxLeaseTime;
    protected int leaseGiveTime;
    protected double renewcostperarea;
    protected boolean canBuy;
    protected boolean canSell;
    protected boolean buyIgnoreLimits;
    protected boolean cancreate;
    protected String groupname;
    protected int maxPhysical;
    protected boolean unstuck;
    protected boolean kick;
    protected int minHeight;
    protected int maxHeight;
    protected int maxRents;
    protected int maxRentables;
    protected boolean selectCommandAccess;
    protected boolean itemListAccess;

    public PermissionGroup(String name) {
        flagPerms = new FlagPermissions();
        creatorDefaultFlags = new HashMap<String, Boolean>();
        residenceDefaultFlags = new HashMap<String, Boolean>();
        groupDefaultFlags = new HashMap<String, Map<String, Boolean>>();
        groupname = name;
    }

    public PermissionGroup(String name, ConfigSection node) {
        this(name);
        this.parseGroup(node);
    }

    public PermissionGroup(String name, ConfigSection node, FlagPermissions parentFlagPerms) {
        this(name, node);
        flagPerms.setParent(parentFlagPerms);
    }

    private void parseGroup(ConfigSection limits) {
        if (limits == null) {
            return;
        }
        cancreate = limits.getBoolean("Residence.CanCreate", false);
        resmax = limits.getInt("Residence.MaxResidences", 0);
        maxPhysical = limits.getInt("Residence.MaxAreasPerResidence", 2);
        xmax = limits.getInt("Residence.MaxEastWest", 0);
        ymax = limits.getInt("Residence.MaxUpDown", 0);
        zmax = limits.getInt("Residence.MaxNorthSouth", 0);
        minHeight = limits.getInt("Residence.MinHeight", 0);
        maxHeight = limits.getInt("Residence.MaxHeight", 255);
        tpaccess = limits.getBoolean("Residence.CanTeleport", false);
        subzonedepth = limits.getInt("Residence.SubzoneDepth", 0);
        messageperms = limits.getBoolean("Messaging.CanChange", false);
        defaultEnterMessage = limits.getString("Messaging.DefaultEnter", null);
        defaultLeaveMessage = limits.getString("Messaging.DefaultLeave", null);
        maxLeaseTime = limits.getInt("Lease.MaxDays", 16);
        leaseGiveTime = limits.getInt("Lease.RenewIncrement", 14);
        maxRents = limits.getInt("Rent.MaxRents", 0);
        maxRentables = limits.getInt("Rent.MaxRentables", 0);
        renewcostperarea = limits.getDouble("Economy.RenewCost", 0.02D);
        canBuy = limits.getBoolean("Economy.CanBuy", false);
        canSell = limits.getBoolean("Economy.CanSell", false);
        buyIgnoreLimits = limits.getBoolean("Economy.IgnoreLimits", false);
        costperarea = limits.getDouble("Economy.BuyCost", 0);
        unstuck = limits.getBoolean("Residence.Unstuck", false);
        kick = limits.getBoolean("Residence.Kick", false);
        selectCommandAccess = limits.getBoolean("Residence.SelectCommandAccess", true);
        itemListAccess = limits.getBoolean("Residence.ItemListAccess", true);
        ConfigSection node = limits.getSection("Flags.Permission");
        if (node != null) {
            node.forEach((flagname, value) -> flagPerms.setFlag(flagname, (boolean) value ? FlagState.TRUE : FlagState.FALSE));
        }

        node = limits.getSection("Flags.CreatorDefault");
        if (node != null) {
            node.forEach((flagname, value) -> creatorDefaultFlags.put(flagname, (boolean) value));
        }

        node = limits.getSection("Flags.Default");
        if (node != null) {
            node.forEach((flagname, value) -> residenceDefaultFlags.put(flagname, (boolean) value));
        }

        node = limits.getSection("Flags.GroupDefault");

        if (node != null) {
            node.forEach((groupName, value) -> {
                if (!(value instanceof ConfigSection)) {
                    return;
                }

                ConfigSection flags = (ConfigSection) value;
                groupDefaultFlags.put(groupName, flags.entrySet()
                        .stream().collect(Collectors.toMap(Entry::getKey, v -> (boolean) v.getValue())));
            });
        }

        Set<String> groupDef = null;
        if (node != null) {
            groupDef = node.getKeys(false);
        }
        if (groupDef != null) {
            for (String name : groupDef) {
                Map<String, Boolean> gflags = new HashMap<String, Boolean>();
                Set<String> flags = limits.getSection("Flags.GroupDefault." + name).getKeys(false);
                for (String flagname : flags) {
                    boolean access = limits.getBoolean("Flags.GroupDefault." + name + "." + flagname, false);
                    gflags.put(flagname, access);
                }
                groupDefaultFlags.put(name, gflags);
            }
        }
    }

    public int getMaxX() {
        return xmax;
    }

    public int getMaxY() {
        return ymax;
    }

    public int getMaxZ() {
        return zmax;
    }

    public int getMinHeight() {
        return minHeight;
    }

    public int getMaxHeight() {
        return maxHeight;
    }

    public int getMaxZones() {
        return resmax;
    }

    public double getCostPerBlock() {
        return costperarea;
    }

    public boolean hasTpAccess() {
        return tpaccess;
    }

    public int getMaxSubzoneDepth() {
        return subzonedepth;
    }

    public boolean canSetEnterLeaveMessages() {
        return messageperms;
    }

    public String getDefaultEnterMessage() {
        return defaultEnterMessage;
    }

    public String getDefaultLeaveMessage() {
        return defaultLeaveMessage;
    }

    public int getMaxLeaseTime() {
        return maxLeaseTime;
    }

    public int getLeaseGiveTime() {
        return leaseGiveTime;
    }

    public double getLeaseRenewCost() {
        return renewcostperarea;
    }

    public boolean canBuyLand() {
        return canBuy;
    }

    public boolean canSellLand() {
        return canSell;
    }

    public int getMaxRents() {
        return maxRents;
    }

    public int getMaxRentables() {
        return maxRentables;
    }

    public boolean buyLandIgnoreLimits() {
        return buyIgnoreLimits;
    }

    public boolean hasUnstuckAccess() {
        return unstuck;
    }

    public boolean hasKickAccess() {
        return kick;
    }

    public int getMaxPhysicalPerResidence() {
        return maxPhysical;
    }

    public Set<Entry<String, Boolean>> getDefaultResidenceFlags() {
        return residenceDefaultFlags.entrySet();
    }

    public Set<Entry<String, Boolean>> getDefaultCreatorFlags() {
        return creatorDefaultFlags.entrySet();
    }

    public Set<Entry<String, Map<String, Boolean>>> getDefaultGroupFlags() {
        return groupDefaultFlags.entrySet();
    }

    public boolean canCreateResidences() {
        return cancreate;
    }

    public boolean hasFlagAccess(String flag) {
        return flagPerms.has(flag, false);
    }

    public boolean inLimits(CuboidArea area) {
        if (area.getXSize() > xmax || area.getYSize() > ymax || area.getZSize() > zmax) {
            return false;
        }
        return true;
    }

    public boolean selectCommandAccess() {
        return selectCommandAccess;
    }

    public boolean itemListAccess() {
        return itemListAccess;
    }

    public void printLimits(Player player) {
        player.sendMessage(TextFormat.GRAY + "---------------------------");
        player.sendMessage(TextFormat.YELLOW + "Permissions Group:" + TextFormat.DARK_AQUA + " " + Residence.getPermissionManager().getPermissionsGroup(player));
        player.sendMessage(TextFormat.YELLOW + "Residence Group:" + TextFormat.DARK_AQUA + " " + groupname);
        player.sendMessage(TextFormat.YELLOW + "Residence Admin:" + TextFormat.DARK_AQUA + " " + Residence.getPermissionManager().isResidenceAdmin(player));
        player.sendMessage(TextFormat.YELLOW + "Can Create Residences:" + TextFormat.DARK_AQUA + " " + cancreate);
        player.sendMessage(TextFormat.YELLOW + "Max Residences:" + TextFormat.DARK_AQUA + " " + resmax);
        player.sendMessage(TextFormat.YELLOW + "Max East/West Size:" + TextFormat.DARK_AQUA + " " + xmax);
        player.sendMessage(TextFormat.YELLOW + "Max North/South Size:" + TextFormat.DARK_AQUA + " " + zmax);
        player.sendMessage(TextFormat.YELLOW + "Max Up/Down Size:" + TextFormat.DARK_AQUA + " " + ymax);
        player.sendMessage(TextFormat.YELLOW + "Min/Max Protection Height:" + TextFormat.DARK_AQUA + " " + minHeight + " to " + maxHeight);
        player.sendMessage(TextFormat.YELLOW + "Max Subzone Depth:" + TextFormat.DARK_AQUA + " " + subzonedepth);
        player.sendMessage(TextFormat.YELLOW + "Can Set Enter/Leave Messages:" + TextFormat.DARK_AQUA + " " + messageperms);
        player.sendMessage(TextFormat.YELLOW + "Number of Residences you own:" + TextFormat.DARK_AQUA + " " + Residence.getResidenceManager().getOwnedZoneCount(player.getName()));
        if (Residence.getEconomyManager() != null) {
            player.sendMessage(TextFormat.YELLOW + "Residence Cost Per Block:" + TextFormat.DARK_AQUA + " " + costperarea);
        }
        player.sendMessage(TextFormat.YELLOW + "Flag Permissions:" + TextFormat.DARK_AQUA + " " + flagPerms.listFlags());
        if (Residence.getConfigManager().useLeases()) {
            player.sendMessage(TextFormat.YELLOW + "Max Lease Days:" + TextFormat.DARK_AQUA + " " + maxLeaseTime);
            player.sendMessage(TextFormat.YELLOW + "Lease Time Given on Renew:" + TextFormat.DARK_AQUA + " " + leaseGiveTime);
            player.sendMessage(TextFormat.YELLOW + "Renew Cost Per Block:" + TextFormat.DARK_AQUA + " " + renewcostperarea);
        }
        player.sendMessage(TextFormat.GRAY + "---------------------------");
    }

}

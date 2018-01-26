/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.protection;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.utils.TextFormat;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.economy.ResidenceBank;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.event.ResidenceTPEvent;
import com.bekvon.bukkit.residence.itemlist.ItemList.ListType;
import com.bekvon.bukkit.residence.itemlist.ResidenceItemList;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.text.help.InformationPager;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Administrator
 */
public class ClaimedResidence {

    protected ClaimedResidence parent;
    protected Map<String, CuboidArea> areas;
    protected Map<String, ClaimedResidence> subzones;
    protected ResidencePermissions perms;
    protected ResidenceBank bank;
    protected Position tpLoc;
    protected String enterMessage;
    protected String leaveMessage;
    protected ResidenceItemList ignorelist;
    protected ResidenceItemList blacklist;

    private ClaimedResidence() {
        subzones = new HashMap<>();
        areas = new HashMap<>();
        bank = new ResidenceBank(this);
        blacklist = new ResidenceItemList(this, ListType.BLACKLIST);
        ignorelist = new ResidenceItemList(this, ListType.IGNORELIST);
    }

    public ClaimedResidence(String creationWorld) {
        this("Server Land", creationWorld);
    }

    public ClaimedResidence(String creator, String creationWorld) {
        this();
        perms = new ResidencePermissions(this, creator, creationWorld);
    }

    public ClaimedResidence(String creator, String creationWorld, ClaimedResidence parentResidence) {
        this(creator, creationWorld);
        parent = parentResidence;
    }

    public boolean addArea(CuboidArea area, String name) {
        return addArea(null, area, name, true);
    }

    public boolean addArea(Player player, CuboidArea area, String name, boolean resadmin) {
        if (!Residence.validName(name)) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            }
            return false;
        }
        if (areas.containsKey(name)) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaExists"));
            }
            return false;
        }
        if (!resadmin && Residence.getConfigManager().getEnforceAreaInsideArea() && this.getParent() == null) {
            boolean inside = false;
            for (CuboidArea are : areas.values()) {
                if (are.isAreaWithinArea(area)) {
                    inside = true;
                }
            }
            if (!inside) {
                return false;
            }
        }
        if (!area.getWorld().getName().equalsIgnoreCase(perms.getLevel())) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaDiffWorld"));
            }
            return false;
        }
        if (parent == null) {
            String collideResidence = Residence.getResidenceManager().checkAreaCollision(area, this);
            if (collideResidence != null) {
                if (player != null) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaCollision", TextFormat.YELLOW + collideResidence));
                }
                return false;
            }
        } else {
            String[] szs = parent.listSubzones();
            for (String sz : szs) {
                ClaimedResidence res = parent.getSubzone(sz);
                if (res != null && res != this) {
                    if (res.checkCollision(area)) {
                        if (player != null) {
                            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaSubzoneCollision", TextFormat.YELLOW + sz));
                        }
                        return false;
                    }
                }
            }
        }
        if (!resadmin && player != null) {
            if (!this.perms.hasResidencePermission(player, true)) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                return false;
            }
            if (parent != null) {
                if (!parent.containsLoc(area.getHighLoc()) || !parent.containsLoc(area.getLowLoc())) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaNotWithinParent"));
                    return false;
                }
                if (!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(), "subzone", true)) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ParentNoPermission"));
                    return false;
                }
            }
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if (!group.canCreateResidences() && !player.hasPermission("residence.create")) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                return false;
            }
            if (areas.size() >= group.getMaxPhysicalPerResidence()) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaMaxPhysical"));
                return false;
            }
            if (!group.inLimits(area)) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaSizeLimit"));
                return false;
            }
            if (group.getMinHeight() > area.getLowLoc().getFloorY()) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaLowLimit", TextFormat.YELLOW + String.format("%d", group.getMinHeight())));
                return false;
            }
            if (group.getMaxHeight() < area.getHighLoc().getFloorY()) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaHighLimit", TextFormat.YELLOW + String.format("%d", group.getMaxHeight())));
                return false;
            }
            if (parent == null && Residence.getConfigManager().enableEconomy()) {
                int chargeamount = (int) Math.ceil((double) area.getSize() * group.getCostPerBlock());
                if (!TransactionManager.chargeEconomyMoney(player, chargeamount)) {
                    return false;
                }
            }
        }
        Residence.getResidenceManager().removeChunkList(getName());
        areas.put(name, area);
        Residence.getResidenceManager().calculateChunks(getName());
        if (player != null) {
            player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("AreaCreate", TextFormat.YELLOW + name));
        }
        return true;
    }

    public boolean replaceArea(CuboidArea neware, String name) {
        return this.replaceArea(null, neware, name, true);
    }

    public boolean replaceArea(Player player, CuboidArea newarea, String name, boolean resadmin) {
        if (!areas.containsKey(name)) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaNonExist"));
            }
            return false;
        }
        CuboidArea oldarea = areas.get(name);
        if (!newarea.getWorld().getName().equalsIgnoreCase(perms.getLevel())) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaDiffWorld"));
            }
            return false;
        }
        if (parent == null) {
            String collideResidence = Residence.getResidenceManager().checkAreaCollision(newarea, this);
            if (collideResidence != null) {
                if (player != null) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaCollision", TextFormat.YELLOW + collideResidence));
                }
                return false;
            }
        } else {
            String[] szs = parent.listSubzones();
            for (String sz : szs) {
                ClaimedResidence res = parent.getSubzone(sz);
                if (res != null && res != this) {
                    if (res.checkCollision(newarea)) {
                        if (player != null) {
                            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaSubzoneCollision", TextFormat.YELLOW + sz));
                        }
                        return false;
                    }
                }
            }
        }
        //Remove subzones that are not in the area anymore
        String[] szs = listSubzones();
        for (String sz : szs) {
            ClaimedResidence res = getSubzone(sz);
            if (res != null && res != this) {
                String[] szareas = res.getAreaList();
                for (String area : szareas) {
                    if (!newarea.isAreaWithinArea(res.getArea(area))) {
                        boolean good = false;
                        for (CuboidArea arae : getAreaArray()) {
                            if (arae != oldarea && arae.isAreaWithinArea(res.getArea(area))) {
                                good = true;
                            }
                        }
                        if (!good) {
                            res.removeArea(area);
                        }
                    }
                }
                if (res.getAreaArray().length == 0) {
                    removeSubzone(sz);
                }
            }
        }
        if (!resadmin && player != null) {
            if (!this.perms.hasResidencePermission(player, true)) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                return false;
            }
            if (parent != null) {
                if (!parent.containsLoc(newarea.getHighLoc()) || !parent.containsLoc(newarea.getLowLoc())) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaNotWithinParent"));
                    return false;
                }
                if (!parent.getPermissions().hasResidencePermission(player, true) && !parent.getPermissions().playerHas(player.getName(), "subzone", true)) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ParentNoPermission"));
                    return false;
                }
            }
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if (!group.canCreateResidences() && !player.hasPermission("residence.create")) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                return false;
            }
            if (!group.inLimits(newarea)) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaSizeLimit"));
                return false;
            }
            if (group.getMinHeight() > newarea.getLowLoc().getFloorY()) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaLowLimit", TextFormat.YELLOW + String.format("%d", group.getMinHeight())));
                return false;
            }
            if (group.getMaxHeight() < newarea.getHighLoc().getFloorY()) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaHighLimit", TextFormat.YELLOW + String.format("%d", group.getMaxHeight())));
                return false;
            }
            if (parent == null && Residence.getConfigManager().enableEconomy()) {
                int chargeamount = (int) Math.ceil((double) (newarea.getSize() - oldarea.getSize()) * group.getCostPerBlock());
                if (chargeamount > 0) {
                    if (!TransactionManager.chargeEconomyMoney(player, chargeamount)) {
                        return false;
                    }
                }
            }

        }
        Residence.getResidenceManager().removeChunkList(getName());
        areas.remove(name);
        areas.put(name, newarea);
        Residence.getResidenceManager().calculateChunks(getName());
        if (player != null) {
            player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("AreaUpdate"));
        }
        return true;
    }

    public boolean addSubzone(String name, Position loc1, Position loc2) {
        return this.addSubzone(null, loc1, loc2, name, true);
    }

    public boolean addSubzone(Player player, Position loc1, Position loc2, String name, boolean resadmin) {
        if (player == null) {
            return this.addSubzone(null, "Server Land", loc1, loc2, name, resadmin);
        } else {
            return this.addSubzone(player, player.getName(), loc1, loc2, name, resadmin);
        }
    }

    public boolean addSubzone(Player player, String owner, Position loc1, Position loc2, String name, boolean resadmin) {
        if (!Residence.validName(name)) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            }
            return false;
        }
        if (!(this.containsLoc(loc1) && this.containsLoc(loc2))) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SubzoneSelectInside"));
            }
            return false;
        }
        if (subzones.containsKey(name)) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SubzoneExists", TextFormat.YELLOW + name));
            }
            return false;
        }
        if (!resadmin && player != null) {
            if (!this.perms.hasResidencePermission(player, true)) {
                if (!this.perms.playerHas(player.getName(), "subzone", this.perms.playerHas(player.getName(), "admin", false))) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                    return false;
                }
            }
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if (this.getZoneDepth() >= group.getMaxSubzoneDepth()) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SubzoneMaxDepth"));
                return false;
            }
        }
        CuboidArea newArea = new CuboidArea(loc1, loc2);
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        for (Entry<String, ClaimedResidence> resEntry : set) {
            ClaimedResidence res = resEntry.getValue();
            if (res.checkCollision(newArea)) {
                if (player != null) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SubzoneCollide", TextFormat.YELLOW + resEntry.getKey()));
                }
                return false;
            }
        }
        ClaimedResidence newres;
        if (player != null) {
            newres = new ClaimedResidence(owner, perms.getLevel(), this);
            newres.addArea(player, newArea, name, resadmin);
        } else {
            newres = new ClaimedResidence(owner, perms.getLevel(), this);
            newres.addArea(newArea, name);
        }
        if (newres.getAreaCount() != 0) {
            newres.getPermissions().applyDefaultFlags();
            if (player != null) {
                PermissionGroup group = Residence.getPermissionManager().getGroup(player);
                newres.setEnterMessage(group.getDefaultEnterMessage());
                newres.setLeaveMessage(group.getDefaultLeaveMessage());
            }
            if (Residence.getConfigManager().flagsInherit()) {
                newres.getPermissions().setParent(perms);
            }
            subzones.put(name, newres);
            if (player != null) {
                player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SubzoneCreate", TextFormat.YELLOW + name));
            }
            return true;
        } else {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SubzoneCreateFail", TextFormat.YELLOW + name));
            }
            return false;
        }
    }

    public String getSubzoneNameByLoc(Position loc) {
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        ClaimedResidence res = null;
        String key = null;
        for (Entry<String, ClaimedResidence> entry : set) {
            if (entry.getValue().containsLoc(loc)) {
                key = entry.getKey();
                res = entry.getValue();
                break;
            }
        }
        if (key == null || res == null) {
            return null;
        }

        String subname = res.getSubzoneNameByLoc(loc);
        if (subname != null) {
            return key + "." + subname;
        }
        return key;
    }

    public ClaimedResidence getSubzoneByLoc(Position loc) {
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        ClaimedResidence res = null;
        for (Entry<String, ClaimedResidence> entry : set) {
            if (entry.getValue().containsLoc(loc)) {
                res = entry.getValue();
                break;
            }
        }
        if (res == null) {
            return null;
        }

        ClaimedResidence subrez = res.getSubzoneByLoc(loc);
        if (subrez == null) {
            return res;
        }
        return subrez;
    }

    public ClaimedResidence getSubzone(String subzonename) {
        if (!subzonename.contains(".")) {
            return subzones.get(subzonename);
        }
        String split[] = subzonename.split("\\.");
        ClaimedResidence get = subzones.get(split[0]);
        for (int i = 1; i < split.length; i++) {
            if (get == null) {
                return null;
            }
            get = get.getSubzone(split[i]);
        }
        return get;
    }

    public String getSubzoneNameByRes(ClaimedResidence res) {
        Set<Entry<String, ClaimedResidence>> set = subzones.entrySet();
        for (Entry<String, ClaimedResidence> entry : set) {
            if (entry.getValue() == res) {
                return entry.getKey();
            }
            String n = entry.getValue().getSubzoneNameByRes(res);
            if (n != null) {
                return entry.getKey() + "." + n;
            }
        }
        return null;
    }

    public String[] getSubzoneList() {
        ArrayList<String> zones = new ArrayList<>();
        Set<String> set = subzones.keySet();
        for (String key : set) {
            if (key != null) {
                zones.add(key);
            }
        }
        return zones.toArray(new String[zones.size()]);
    }

    public boolean checkCollision(CuboidArea area) {
        Set<String> set = areas.keySet();
        for (String key : set) {
            CuboidArea checkarea = areas.get(key);
            if (checkarea != null) {
                if (checkarea.checkCollision(area)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsLoc(Position loc) {
        Collection<CuboidArea> keys = areas.values();
        for (CuboidArea key : keys) {
            if (key.containsLoc(loc)) {
                if (parent != null) {
                    return parent.containsLoc(loc);
                }
                return true;
            }
        }
        return false;
    }

    public ClaimedResidence getParent() {
        return parent;
    }

    public ClaimedResidence getTopParent() {
        if (parent == null) {
            return this;
        }
        return parent.getTopParent();
    }

    public boolean removeSubzone(String name) {
        return this.removeSubzone(null, name, true);
    }

    public boolean removeSubzone(Player player, String name, boolean resadmin) {
        ClaimedResidence res = subzones.get(name);
        if (player != null && !res.perms.hasResidencePermission(player, true) && !resadmin) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return false;
        }
        subzones.remove(name);
        if (player != null) {
            player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SubzoneRemove", TextFormat.YELLOW + name + TextFormat.GREEN));
        }
        return true;
    }

    public long getTotalSize() {
        Collection<CuboidArea> set = areas.values();
        long size = 0;
        for (CuboidArea entry : set) {
            size = size + entry.getSize();
        }
        return size;
    }

    public CuboidArea[] getAreaArray() {
        CuboidArea[] temp = new CuboidArea[areas.size()];
        int i = 0;
        for (CuboidArea area : areas.values()) {
            temp[i] = area;
            i++;
        }
        return temp;
    }

    public ResidencePermissions getPermissions() {
        return perms;
    }

    public String getEnterMessage() {
        return enterMessage;
    }

    public String getLeaveMessage() {
        return leaveMessage;
    }

    public void setEnterMessage(String message) {
        enterMessage = message;
    }

    public void setLeaveMessage(String message) {
        leaveMessage = message;
    }

    public void setEnterLeaveMessage(Player player, String message, boolean enter, boolean resadmin) {
        // if(message!=null &&
        // Residence.getConfigManager().getResidenceNameRegex() != null) {
        // Removed pending further action
        // player.sendMessage(TextFormat.RED+Residence.getLanguage().getPhrase("InvalidCharacters"));
        // return;
        // }
        if (message != null) {
            if (message.equals("")) {
                message = null;
            }
        }
        PermissionGroup group = Residence.getPermissionManager().getGroup(perms.getOwner(), perms.getLevel());
        if (!group.canSetEnterLeaveMessages() && !resadmin) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("OwnerNoPermission"));
            return;
        }
        if (!perms.hasResidencePermission(player, false) && !resadmin) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        if (enter) {
            this.setEnterMessage(message);
        } else {
            this.setLeaveMessage(message);
        }
        player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("MessageChange"));
    }

    public Position getOutsideFreeLoc(Position insideLoc) {
        int maxIt = 100;
        CuboidArea area = this.getAreaByLoc(insideLoc);
        if (area == null) {
            return insideLoc;
        }
        Position highLoc = area.getHighLoc();
        Position newLoc = new Position(highLoc.getFloorX(), highLoc.getFloorY(), highLoc.getFloorZ(), highLoc.getLevel());
        boolean found = false;
        int it = 0;
        while (!found && it < maxIt) {
            it++;
            Position lowLoc;
            newLoc.x = newLoc.getFloorX() + 1;
            newLoc.z = newLoc.getFloorZ() + 1;
            lowLoc = new Position(newLoc.getFloorX(), 254, newLoc.getFloorZ(), newLoc.getLevel());
            newLoc.y = 255;

            while ((newLoc.level.getBlockIdAt(newLoc.getFloorX(), newLoc.getFloorY(), newLoc.getFloorZ()) != 0 || lowLoc.level.getBlockIdAt(lowLoc.getFloorX(), lowLoc.getFloorY(), lowLoc.getFloorZ()) == 0) && lowLoc.getFloorY() > -126) {
                newLoc.y = newLoc.getY() - 1;
                lowLoc.y = lowLoc.getY() - 1;
            }
            if (newLoc.level.getBlockIdAt(newLoc.getFloorX(), newLoc.getFloorY(), newLoc.getFloorZ()) == 0 && lowLoc.level.getBlockIdAt(lowLoc.getFloorX(), lowLoc.getFloorY(), lowLoc.getFloorZ()) != 0) {
                found = true;
            }
        }
        if (found) {
            return newLoc;
        } else {
            Level world = Residence.getServ().getLevelByName(perms.getLevel());
            if (world != null) {
                return world.getSpawnLocation();
            }
            return insideLoc;
        }
    }

    protected CuboidArea getAreaByLoc(Position loc) {
        for (CuboidArea thisarea : areas.values()) {
            if (thisarea.containsLoc(loc)) {
                return thisarea;
            }
        }
        return null;
    }

    public String[] listSubzones() {
        String list[] = new String[subzones.size()];
        int i = 0;
        for (String res : subzones.keySet()) {
            list[i] = res;
            i++;
        }
        return list;
    }

    public void printSubzoneList(Player player, int page) {
        ArrayList<String> temp = new ArrayList<>();
        for (Entry<String, ClaimedResidence> sz : subzones.entrySet()) {
            temp.add(TextFormat.GREEN + sz.getKey() + TextFormat.YELLOW + " - " + Residence.getLanguage().getPhrase("Owner") + ": " + sz.getValue().getOwner());
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("Subzones"), temp, page);
    }

    public void printAreaList(Player player, int page) {
        ArrayList<String> temp = new ArrayList<>();
        for (String area : areas.keySet()) {
            temp.add(area);
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("PhysicalAreas"), temp, page);
    }

    public void printAdvancedAreaList(Player player, int page) {
        ArrayList<String> temp = new ArrayList<>();
        for (Entry<String, CuboidArea> entry : areas.entrySet()) {
            CuboidArea a = entry.getValue();
            Position h = a.getHighLoc();
            Position l = a.getLowLoc();
            temp.add(TextFormat.GREEN + "{" + TextFormat.YELLOW + "ID:" + TextFormat.RED + entry.getKey() + " " + TextFormat.YELLOW + "P1:" + TextFormat.RED + "(" + h.getFloorX() + "," + h.getFloorY() + "," + h.getFloorZ() + ") " + TextFormat.YELLOW + "P2:" + TextFormat.RED + "(" + l.getFloorX() + "," + l.getFloorY() + "," + l.getFloorZ() + ") " + TextFormat.YELLOW + "(Size:" + TextFormat.RED + a.getSize() + TextFormat.YELLOW + ")" + TextFormat.GREEN + "} ");
        }
        InformationPager.printInfo(player, Residence.getLanguage().getPhrase("PhysicalAreas"), temp, page);
    }

    public String[] getAreaList() {
        String arealist[] = new String[areas.size()];
        int i = 0;
        for (Entry<String, CuboidArea> entry : areas.entrySet()) {
            arealist[i] = entry.getKey();
            i++;
        }
        return arealist;
    }

    public int getZoneDepth() {
        int count = 0;
        ClaimedResidence res = parent;
        while (res != null) {
            count++;
            res = res.getParent();
        }
        return count;
    }

    public void setTpLoc(Player player, boolean resadmin) {
        if (!this.perms.hasResidencePermission(player, false) && !resadmin) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        if (!this.containsLoc(player.getPosition())) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NotInResidence"));
            return;
        }
        tpLoc = player.getPosition();
        player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SetTeleportLocation"));
    }

    public void tpToResidence(Player reqPlayer, Player targetPlayer, boolean resadmin) {
        if (!resadmin) {
            PermissionGroup group = Residence.getPermissionManager().getGroup(reqPlayer);
            if (!group.hasTpAccess()) {
                reqPlayer.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("TeleportDeny"));
                return;
            }
            if (!reqPlayer.equals(targetPlayer)) {
                reqPlayer.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                return;
            }
            if (!this.perms.playerHas(reqPlayer.getName(), "tp", true)) {
                reqPlayer.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("TeleportNoFlag"));
                return;
            }
        }
        if (tpLoc != null) {
            ResidenceTPEvent tpevent = new ResidenceTPEvent(this, tpLoc, targetPlayer, reqPlayer);
            Residence.getServ().getPluginManager().callEvent(tpevent);
            if (!tpevent.isCancelled()) {
                targetPlayer.teleport(tpLoc);
                targetPlayer.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("TeleportSuccess"));
            }
        } else {
            CuboidArea area = areas.values().iterator().next();
            if (area == null) {
                reqPlayer.sendMessage(TextFormat.RED + "Could not find area to teleport to...");
                return;
            }
            Position targloc = this.getOutsideFreeLoc(area.getHighLoc());
            ResidenceTPEvent tpevent = new ResidenceTPEvent(this, targloc, targetPlayer, reqPlayer);
            Residence.getServ().getPluginManager().callEvent(tpevent);
            if (!tpevent.isCancelled()) {
                targetPlayer.teleport(targloc);
                targetPlayer.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("TeleportNear"));
            }
        }
    }

    public String getAreaIDbyLoc(Position loc) {
        for (Entry<String, CuboidArea> area : areas.entrySet()) {
            if (area.getValue().containsLoc(loc)) {
                return area.getKey();
            }
        }
        return null;
    }

    public void removeArea(String id) {
        Residence.getResidenceManager().removeChunkList(getName());
        areas.remove(id);
        Residence.getResidenceManager().calculateChunks(getName());
    }

    public void removeArea(Player player, String id, boolean resadmin) {

        if (this.getPermissions().hasResidencePermission(player, true) || resadmin) {
            if (!areas.containsKey(id)) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaNonExist"));
                return;
            }
            if (areas.size() == 1 && !Residence.getConfigManager().allowEmptyResidences()) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaRemoveLast"));
                return;
            }
            removeArea(id);
            player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("AreaRemove"));
        } else {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
        }
    }

    public Map<String, Object> save() {
        Map<String, Object> root = new HashMap<>();
        Map<String, Object> areamap = new HashMap<>();
        root.put("EnterMessage", enterMessage);
        root.put("LeaveMessage", leaveMessage);
        root.put("StoredMoney", bank.getStoredMoney());
        root.put("BlackList", blacklist.save());
        root.put("IgnoreList", ignorelist.save());
        for (Entry<String, CuboidArea> entry : areas.entrySet()) {
            areamap.put(entry.getKey(), entry.getValue().save());
        }
        root.put("Areas", areamap);
        Map<String, Object> subzonemap = new HashMap<>();
        for (Entry<String, ClaimedResidence> sz : subzones.entrySet()) {
            subzonemap.put(sz.getKey(), sz.getValue().save());
        }
        root.put("Subzones", subzonemap);
        root.put("Permissions", perms.save());
        if (tpLoc != null) {
            Map<String, Object> tpmap = new HashMap<>();
            tpmap.put("X", tpLoc.getFloorX());
            tpmap.put("Y", tpLoc.getFloorY());
            tpmap.put("Z", tpLoc.getFloorZ());
            root.put("TPLoc", tpmap);
        }
        return root;
    }

    public static ClaimedResidence load(Map<String, Object> root, ClaimedResidence parent) throws Exception {
        ClaimedResidence res = new ClaimedResidence();
        if (root == null) {
            throw new Exception("Null residence!");
        }
        res.enterMessage = (String) root.get("EnterMessage");
        res.leaveMessage = (String) root.get("LeaveMessage");
        if (root.containsKey("StoredMoney")) {
            res.bank.setStoredMoney((Integer) root.get("StoredMoney"));
        }
        if (root.containsKey("BlackList")) {
            res.blacklist = ResidenceItemList.load(res, (Map<String, Object>) root.get("BlackList"));
        }
        if (root.containsKey("IgnoreList")) {
            res.ignorelist = ResidenceItemList.load(res, (Map<String, Object>) root.get("IgnoreList"));
        }
        Map<String, Object> areamap = (Map<String, Object>) root.get("Areas");
        res.perms = ResidencePermissions.load(res, (Map<String, Object>) root.get("Permissions"));
        Level world = Residence.getServ().getLevelByName(res.perms.getLevel());
        if (world == null) {
            throw new Exception("Cant Find World: " + res.perms.getLevel());
        }
        for (Entry<String, Object> map : areamap.entrySet()) {
            res.areas.put(map.getKey(), CuboidArea.load((Map<String, Object>) map.getValue(), world));
        }
        Map<String, Object> subzonemap = (Map<String, Object>) root.get("Subzones");
        for (Entry<String, Object> map : subzonemap.entrySet()) {
            ClaimedResidence subres = ClaimedResidence.load((Map<String, Object>) map.getValue(), res);
            if (Residence.getConfigManager().flagsInherit()) {
                subres.getPermissions().setParent(res.getPermissions());
            }
            res.subzones.put(map.getKey(), subres);
        }
        res.parent = parent;
        Map<String, Object> tploc = (Map<String, Object>) root.get("TPLoc");
        if (tploc != null) {
            res.tpLoc = new Position((Integer) tploc.get("X"), (Integer) tploc.get("Y"), (Integer) tploc.get("Z"), world);
        }
        return res;
    }

    public int getAreaCount() {
        return areas.size();
    }

    public boolean renameSubzone(String oldName, String newName) {
        return this.renameSubzone(null, oldName, newName, true);
    }

    public boolean renameSubzone(Player player, String oldName, String newName, boolean resadmin) {
        if (!Residence.validName(newName)) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            return false;
        }
        ClaimedResidence res = subzones.get(oldName);
        if (res == null) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidSubzone"));
            }
            return false;
        }
        if (player != null && !res.getPermissions().hasResidencePermission(player, true) && !resadmin) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return false;
        }
        if (subzones.containsKey(newName)) {
            if (player != null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SubzoneExists", TextFormat.YELLOW + newName));
            }
            return false;
        }
        subzones.put(newName, res);
        subzones.remove(oldName);
        if (player != null) {
            player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SubzoneRename", oldName + "." + newName));
        }
        return true;
    }

    public boolean renameArea(String oldName, String newName) {
        return this.renameArea(null, oldName, newName, true);
    }

    public boolean renameArea(Player player, String oldName, String newName, boolean resadmin) {
        if (!Residence.validName(newName)) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidNameCharacters"));
            return false;
        }
        if (player == null || perms.hasResidencePermission(player, true) || resadmin) {
            if (areas.containsKey(newName)) {
                if (player != null) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaExists"));
                }
                return false;
            }
            CuboidArea area = areas.get(oldName);
            if (area == null) {
                if (player != null) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AreaInvalidName"));
                }
                return false;
            }
            areas.put(newName, area);
            areas.remove(oldName);
            if (player != null) {
                player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("AreaRename", oldName + "." + newName));
            }
            return true;
        } else {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return false;
        }
    }

    public CuboidArea getArea(String name) {
        return areas.get(name);
    }

    public String getName() {
        return Residence.getResidenceManager().getNameByRes(this);
    }

    public void remove() {
        String name = getName();
        if (name != null) {
            Residence.getResidenceManager().removeResidence(name);
            Residence.getResidenceManager().removeChunkList(name);
        }
    }

    public ResidenceBank getBank() {
        return bank;
    }

    public String getWorld() {
        return perms.getLevel();
    }

    public String getOwner() {
        return perms.getOwner();
    }

    public ResidenceItemList getItemBlacklist() {
        return blacklist;
    }

    public ResidenceItemList getItemIgnoreList() {
        return ignorelist;
    }

    public ArrayList<Player> getPlayersInResidence() {
        ArrayList<Player> within = new ArrayList<>();
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            if (this.containsLoc(player.getPosition())) {
                within.add(player);
            }
        }
        return within;
    }
}

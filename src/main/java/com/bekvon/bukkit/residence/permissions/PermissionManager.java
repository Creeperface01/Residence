/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.permissions;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.MainLogger;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import java.util.*;

/**
 * @author Administrator
 */
public class PermissionManager {

    protected static PermissionsInterface perms;
    protected Map<String, PermissionGroup> groups;
    protected Map<String, String> playersGroup;
    protected FlagPermissions globalFlagPerms;

    public PermissionManager(Config config) {
        try {
            groups = Collections.synchronizedMap(new HashMap<String, PermissionGroup>());
            playersGroup = Collections.synchronizedMap(new HashMap<String, String>());
            globalFlagPerms = new FlagPermissions();
            this.readConfig(config);
            boolean enable = config.getBoolean("Global.EnablePermissions", true);
            if (enable) {
                this.checkPermissions();
            }
        } catch (Exception ex) {
            MainLogger.getLogger().logException(ex);
        }
    }

    public PermissionGroup getGroup(Player player) {
        return groups.get(this.getGroupNameByPlayer(player));
    }

    public PermissionGroup getGroup(String player, String world) {
        return groups.get(this.getGroupNameByPlayer(player, world));
    }

    public PermissionGroup getGroupByName(String group) {
        group = group.toLowerCase();
        if (!groups.containsKey(group)) {
            return groups.get(Residence.getConfigManager().getDefaultGroup());
        }
        return groups.get(group);
    }

    public String getGroupNameByPlayer(Player player) {
        return this.getGroupNameByPlayer(player.getName(), player.getLevel().getName());
    }

    public String getGroupNameByPlayer(String player, String world) {
        player = player.toLowerCase();
        if (playersGroup.containsKey(player)) {
            String group = playersGroup.get(player);
            if (group != null) {
                group = group.toLowerCase();
                if (group != null && groups.containsKey(group)) {
                    return group;
                }
            }
        }
        String group = this.getPermissionsGroup(player, world);
        if (group == null || !groups.containsKey(group)) {
            return Residence.getConfigManager().getDefaultGroup().toLowerCase();
        } else {
            return group;
        }
    }

    public String getPermissionsGroup(Player player) {
        return this.getPermissionsGroup(player.getName(), player.getLevel().getName());
    }

    public String getPermissionsGroup(String player, String world) {
        if (perms == null) {
            return Residence.getConfigManager().getDefaultGroup();
        }
        return perms.getPlayerGroup(player, world);
    }

    public boolean isResidenceAdmin(Player player) {
        return (player.hasPermission("residence.admin") || (player.isOp() && Residence.getConfigManager().getOpsAreAdmins()));
    }

    private void checkPermissions() {
        Server server = Residence.getServ();

        Plugin plugin = server.getPluginManager().getPlugin("LuckPerms");
        if (plugin != null) {
            perms = new LuckPermsAdapter();
            MainLogger.getLogger().notice("[Residence] Found LuckPerms Plugin!");
            return;
        }

        plugin = server.getPluginManager().getPlugin("MadPerms");

        if (plugin != null) {
            perms = new MadPermsAdapter();
            MainLogger.getLogger().notice("[Residence] Found MadPerms Plugin!");
            return;
        }

        plugin = server.getPluginManager().getPlugin("Multipass");
        if (plugin != null) {
            perms = new MultiPassAdapter();
            MainLogger.getLogger().notice("[Residence] Found Multipass Plugin!");
            return;
        }

        MainLogger.getLogger().warning("[Residence] Permissions plugin NOT FOUND!");
    }

    private void readConfig(Config config) {
        String defaultGroup = Residence.getConfigManager().getDefaultGroup();
        globalFlagPerms = FlagPermissions.parseFromConfigNode("FlagPermission", config.getSection("Global"));
        ConfigSection nodes = config.getSection("Groups");
        if (nodes != null) {
            Set<String> entrys = nodes.getKeys(false);
            for (String key : entrys) {
                try {
                    groups.put(key.toLowerCase(), new PermissionGroup(key.toLowerCase(), nodes.getSection(key), globalFlagPerms));
                    List<String> mirrors = nodes.getSection(key).getStringList("Mirror");
                    for (String group : mirrors) {
                        groups.put(group.toLowerCase(), new PermissionGroup(key.toLowerCase(), nodes.getSection(key), globalFlagPerms));
                    }
                } catch (Exception ex) {
                    MainLogger.getLogger().error("[Residence] Error parsing group from config:" + key + " Exception:", ex);
                }
            }
        }
        if (!groups.containsKey(defaultGroup)) {
            groups.put(defaultGroup, new PermissionGroup(defaultGroup));
        }
        Set<String> keys = config.getSection("GroupAssignments").getKeys(false);
        if (keys != null) {
            for (String key : keys) {
                playersGroup.put(key.toLowerCase(), config.getString("GroupAssignments." + key, defaultGroup).toLowerCase());
            }
        }
    }

    public boolean hasGroup(String group) {
        group = group.toLowerCase();
        return groups.containsKey(group);
    }

    public PermissionsInterface getPermissionsPlugin() {
        return perms;
    }
}

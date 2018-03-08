package com.bekvon.bukkit.residence.permissions;

import cn.nukkit.Player;
import cn.nukkit.utils.MainLogger;
import me.lucko.luckperms.LuckPerms;
import me.lucko.luckperms.api.User;

/**
 * @author CreeperFace
 */
public class LuckPermsAdapter implements PermissionsInterface {


    @Override
    public String getPlayerGroup(Player player) {
        return getPlayerGroup(player.getName(), null);
    }

    @Override
    public String getPlayerGroup(String player, String world) {
        User user = LuckPerms.getApi().getUser(player);

        if(user == null) {
            return null;
        }

        /*String[] groups = user.getAllNodes().stream()
                .filter(Node::isGroupNode)
                .filter((n) -> {
                    if(world == null) {
                        return true;
                    }

                    Optional<String> w = n.getWorld();
                    if(w.isPresent()) {
                        return Objects.equals(w.get(), world);
                    }

                    return false;
                })
                .map(Node::getGroupName)
                .toArray(String[]::new);

        String group = groups != null && groups.length > 0 ? groups[0] : null;*/
        String group = user.getPrimaryGroup();
        MainLogger.getLogger().info("group: "+group);

        return group;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.listeners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.block.BlockIgniteEvent.BlockIgniteCause;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.level.Location;
import cn.nukkit.item.Item;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.block.BlockSpreadEvent;

import com.bekvon.bukkit.residence.protection.FlagPermissions;

import cn.nukkit.event.block.BlockBurnEvent;
import cn.nukkit.event.block.BlockFromToEvent;
import cn.nukkit.event.block.BlockIgniteEvent;
import cn.nukkit.event.block.BlockPlaceEvent;

import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;

/**
 *
 * @author Administrator
 */
public class ResidenceBlockListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        Block b = event.getBlock();

        String level = b.getLevel().getName();
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if (Residence.getItemManager().isIgnored(b.getId(), group, level)) {
            return;
        }
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if (Residence.getConfigManager().enabledRentSystem()) {
            if (res != null) {
                String resname = res.getName();
                if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
        String pname = player.getName();
        if (res != null) {
            if (res.getItemIgnoreList().isListed(b.getId())) {
                return;
            }
        }
        boolean hasdestroy = perms.playerHas(pname, player.getLevel().getName(), "destroy", perms.playerHas(pname, player.getLevel().getName(), "build", true));
        boolean hasContainer = perms.playerHas(pname, player.getLevel().getName(), "container", true);
        if (!hasdestroy || (!hasContainer && b.getId() == Block.CHEST)) {
            event.setCancelled(true);
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        Block b = event.getBlock();
        String world = event.getBlock().getLevel().getName();
        String group = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if (Residence.getItemManager().isIgnored(b.getId(), group, world)) {
            return;
        }
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlock().getLocation());
        if (Residence.getConfigManager().enabledRentSystem()) {
            if (res != null) {
                String resname = res.getName();
                if (Residence.getConfigManager().preventRentModify() && Residence.getRentManager().isRented(resname)) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        String pname = player.getName();
        if (res != null) {
            if (!res.getItemBlacklist().isAllowed(b.getId())) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
                event.setCancelled(true);
                return;
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
        boolean hasplace = perms.playerHas(pname, player.getLevel().getName(), "place", perms.playerHas(pname, player.getLevel().getName(), "build", true));
        if (!hasplace) {
            event.setCancelled(true);
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockSpread(BlockSpreadEvent event) {
        Location loc = event.getBlock().getLocation();
        FlagPermissions perms = Residence.getPermsByLoc(loc);
        if (!perms.has("spread", true)) {
            event.setCancelled(true);
        }
    }

    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true) //TODO: piston
    public void onBlockPistonRetract(BlockPistonRetractEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has("piston", true)) {
            event.setCancelled(true);
            return;
        }
        if (event.isSticky()) {
            Location location = event.getRetractLocation();
            FlagPermissions blockperms = Residence.getPermsByLoc(location);
            if (!blockperms.has("piston", true)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPistonExtend(BlockPistonExtendEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has("piston", true)) {
            event.setCancelled(true);
        }
        for (Block block : event.getBlocks()) {
            FlagPermissions blockpermsfrom = Residence.getPermsByLoc(block.getLocation());
            Location blockto = block.getLocation();
            blockto.setX(blockto.getX() + event.getDirection().getModX());
            blockto.setY(blockto.getY() + event.getDirection().getModY());
            blockto.setZ(blockto.getZ() + event.getDirection().getModZ());
            FlagPermissions blockpermsto = Residence.getPermsByLoc(blockto);
            if (!blockpermsfrom.has("piston", true) || !blockpermsto.has("piston", true)) {
                event.setCancelled(true);
                return;
            }
        }
    }*/

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockFromTo(BlockFromToEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getTo().getLocation());
        boolean hasflow = perms.has("flow", true);
        Block b = event.getBlock();
        if (!hasflow) {
            event.setCancelled(true);
            return;
        }
        if (b.getId() == Block.LAVA || b.getId() == Block.STILL_LAVA) {
            if (!perms.has("lavaflow", hasflow)) {
                event.setCancelled(true);
            }
            return;
        }
        if (b.getId() == Block.WATER || b.getId() == Block.STILL_WATER) {
            if (!perms.has("waterflow", hasflow)) {
                event.setCancelled(true);
            }
            return;
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
        if (!perms.has("firespread", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        Player player = (Player) event.getEntity();

        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlock().getLocation(), player);
        BlockIgniteCause cause = event.getCause();
        if (cause == BlockIgniteCause.SPREAD) {
            if (!perms.has("firespread", true)) {
                event.setCancelled(true);
            }
        } else if (cause == BlockIgniteCause.FLINT_AND_STEEL) {
            if (player != null && !perms.playerHas(player.getName(), player.getLevel().getName(), "ignite", true) && !Residence.isResAdminOn(player)) {
                event.setCancelled(true);
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            }
        } else {
            if (!perms.has("ignite", true)) {
                event.setCancelled(true);
            }
        }
    }
}

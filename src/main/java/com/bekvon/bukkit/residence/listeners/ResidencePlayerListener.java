/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.listeners;

import cn.nukkit.Player;
import cn.nukkit.PlayerFood;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockRedstoneDiode;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.EntityRegainHealthEvent;
import cn.nukkit.event.player.*;
import cn.nukkit.event.player.PlayerInteractEvent.Action;
import cn.nukkit.event.player.PlayerTeleportEvent.TeleportCause;
import cn.nukkit.item.Item;
import cn.nukkit.level.Location;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.TextFormat;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.chat.ChatChannel;
import com.bekvon.bukkit.residence.event.ResidenceChangedEvent;
import com.bekvon.bukkit.residence.event.ResidenceEnterEvent;
import com.bekvon.bukkit.residence.event.ResidenceLeaveEvent;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;
import com.bekvon.bukkit.residence.utils.ActionBar;
import com.bekvon.bukkit.residence.utils.Optimization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Administrator
 */
public class ResidencePlayerListener implements Listener {

    protected Map<String, String> currentRes;
    protected Map<Long, Long> lastUpdate;
    protected Map<String, Location> lastOutsideLoc;
    protected int minUpdateTime;
    protected boolean chatenabled;
    protected List<String> playerToggleChat;

    public ResidencePlayerListener() {
        currentRes = new HashMap<>();
        lastUpdate = new HashMap<>();
        lastOutsideLoc = new HashMap<>();
        playerToggleChat = new ArrayList<>();
        minUpdateTime = Residence.getConfigManager().getMinMoveUpdateInterval();
        chatenabled = Residence.getConfigManager().chatEnabled();
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            lastUpdate.put(player.getId(), System.currentTimeMillis());
        }
    }

    public void reload() {
        currentRes = new HashMap<>();
        lastUpdate = new HashMap<>();
        lastOutsideLoc = new HashMap<>();
        playerToggleChat = new ArrayList<>();
        minUpdateTime = Residence.getConfigManager().getMinMoveUpdateInterval();
        chatenabled = Residence.getConfigManager().chatEnabled();
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            lastUpdate.put(player.getId(), System.currentTimeMillis());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        String pname = event.getPlayer().getName();
        currentRes.remove(pname);
        lastUpdate.remove(event.getPlayer().getId());
        lastOutsideLoc.remove(pname);
        Residence.getChatManager().removeFromChannel(pname);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        lastUpdate.put(player.getId(), 0L);
        if (Residence.getPermissionManager().isResidenceAdmin(player)) {
            Residence.turnResAdminOn(player);
        }
        handleNewLocation(player, player.getLocation(), false);

        if (player.isOp() || player.hasPermission("residence.versioncheck")) {
            Residence.getVersionChecker().VersionCheck(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerSpawn(PlayerRespawnEvent event) {
        Position loc = event.getRespawnPosition();
        Player player = event.getPlayer();
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
        if (res == null) {
            return;
        }
        if (res.getPermissions().playerHas(player.getName(), "move", true)) {
            return;
        }
        /*if (bed) {
            loc = player.getLevel().getSpawnLocation();
        }*/
        res = Residence.getResidenceManager().getByLoc(loc);
        if (res != null) {
            if (!res.getPermissions().playerHas(player.getName(), "move", true)) {
                loc = res.getOutsideFreeLoc(loc);
            }
        }
        player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoSpawn"));
        event.setRespawnPosition(loc);
    }

    private boolean isContainer(int mat, Block block) {
        return FlagPermissions.getMaterialUseFlagList().containsKey(mat) && FlagPermissions.getMaterialUseFlagList().get(mat).equals("container") || Residence.getConfigManager().getCustomContainers().contains(block.getId());
    }

    private boolean isCanUseEntity_BothClick(int mat, Block block) {
        return mat == Block.LEVER || mat == Block.STONE_BUTTON || mat == Block.WOODEN_BUTTON
                || mat == Block.WOODEN_DOOR_BLOCK || mat == Block.SPRUCE_DOOR_BLOCK || mat == Block.BIRCH_DOOR_BLOCK
                || mat == Block.JUNGLE_DOOR_BLOCK || mat == Block.ACACIA_DOOR_BLOCK || mat == Block.DARK_OAK_DOOR_BLOCK
                || mat == Block.FENCE_GATE_SPRUCE
                || mat == Block.FENCE_GATE_BIRCH || mat == Block.FENCE_GATE_JUNGLE || mat == Block.FENCE_GATE_ACACIA
                || mat == Block.FENCE_GATE_DARK_OAK || mat == Block.TRAPDOOR || mat == Block.IRON_TRAPDOOR || mat == Block.FENCE_GATE
                || mat == Block.PISTON || mat == Block.STICKY_PISTON || mat == Block.DRAGON_EGG
                || Residence.getConfigManager().getCustomBothClick().contains(block.getId());
    }

    private boolean isCanUseEntity_RClickOnly(int mat, Block block) {
        return mat == Block.ITEM_FRAME_BLOCK || mat == Block.BEACON || mat == Block.FLOWER_POT_BLOCK /*|| mat == Block.COMMAND*/ || mat == Block.ANVIL || mat == Block.CAKE_BLOCK || mat == Block.NOTEBLOCK
                || block instanceof BlockRedstoneDiode
                || mat == Block.BED_BLOCK || mat == Block.WORKBENCH || mat == Block.BREWING_STAND_BLOCK
                || mat == Block.ENCHANTMENT_TABLE
                || Residence.getConfigManager().getCustomRightClick().contains(block.getId());
    }

    private boolean isCanUseEntity(int mat, Block block) {
        return isCanUseEntity_BothClick(mat, block) || isCanUseEntity_RClickOnly(mat, block);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Item heldItem = event.getItem();
        Block block = event.getBlock();
        Action action = event.getAction();

        if (block == null) {
            return;
        }
        int mat = block.getId();
        if (!((isContainer(mat, block) || isCanUseEntity_RClickOnly(mat, block)) && action == Action.RIGHT_CLICK_BLOCK || isCanUseEntity_BothClick(mat, block) || event.getAction() == Action.PHYSICAL)) {
            int typeId = heldItem.getId();
            if (typeId != Residence.getConfigManager().getSelectionTooldID() && typeId != Residence.getConfigManager().getInfoToolID() && typeId != 351 && typeId != 416) {
                return;
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(block.getLocation(), player);
        String world = player.getLevel().getName();
        String permgroup = Residence.getPermissionManager().getGroupNameByPlayer(player);
        boolean resadmin = Residence.isResAdminOn(player);
        if (event.getAction() == Action.PHYSICAL) {
            if (!resadmin) {
                boolean hasuse = perms.playerHas(player.getName(), world, "use", true);
                boolean haspressure = perms.playerHas(player.getName(), world, "pressure", hasuse);
                if ((!hasuse && !haspressure || !haspressure) && (mat == Block.STONE_PRESSURE_PLATE || mat == Block.WOODEN_PRESSURE_PLATE)) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (!perms.playerHas(player.getName(), world, "trample", perms.playerHas(player.getName(), world, "build", true)) && (mat == Block.FARMLAND || mat == Block.SOUL_SAND)) {
                event.setCancelled(true);
                return;
            }
            return;
        }
        if (!resadmin && !Residence.getItemManager().isAllowed(heldItem.getId(), permgroup, world)) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
            event.setCancelled(true);
            return;
        }
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (heldItem.getId() == Residence.getConfigManager().getSelectionTooldID()) {
                Plugin wep = Server.getInstance().getPluginManager().getPlugin("FastAsyncWorldEdit");
                if (wep != null) {
                    if (Optimization.getBasicFAWEConfig(wep).getInt("wand-item") == Residence.getConfigManager().getSelectionTooldID()) {
                        return;
                    }
                }
                PermissionGroup group = Residence.getPermissionManager().getGroup(player);
                if (player.hasPermission("residence.select") || player.hasPermission("residence.create") && !player.isPermissionSet("residence.select") || group.canCreateResidences() && !player.isPermissionSet("residence.create") && !player.isPermissionSet("residence.select") || resadmin) {
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        Location loc = block.getLocation();
                        Residence.getSelectionManager().placeLoc1(player, loc);
                        player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SelectPoint", Residence.getLanguage().getPhrase("Primary")) + TextFormat.RED + "(" + loc.getFloorX() + "," + loc.getFloorY() + "," + loc.getFloorZ() + ")" + TextFormat.GREEN + "!");
                        event.setCancelled();
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Location loc = block.getLocation();
                        Residence.getSelectionManager().placeLoc2(player, loc);
                        player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SelectPoint", Residence.getLanguage().getPhrase("Secondary")) + TextFormat.RED + "(" + loc.getFloorX() + "," + loc.getFloorY() + "," + loc.getFloorZ() + ")" + TextFormat.GREEN + "!");
                        event.setCancelled();
                    }
                }
            }
            if (heldItem.getId() == Residence.getConfigManager().getInfoToolID()) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    Location loc = block.getLocation();
                    String res = Residence.getResidenceManager().getNameByLoc(loc);
                    if (res != null) {
                        Residence.getResidenceManager().printAreaInfo(res, player);
                        event.setCancelled(true);
                    }
                    if (res == null) {
                        event.setCancelled(true);
                        player.sendMessage(Residence.getLanguage().getPhrase("NoResHere"));
                    }
                }
            }
            if (!resadmin) {
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (heldItem.getId() == 351) {
                        if (heldItem.getDamage() == 15 && block.getId() == Block.GRASS || heldItem.getDamage() == 3 && block.getId() == 17 && (block.getDamage() == 3 || block.getDamage() == 7 || block.getDamage() == 11 || block.getDamage() == 15)) {
                            perms = Residence.getPermsByLocForPlayer(block.getSide(event.getFace()).getLocation(), player);
                            if (!perms.playerHas(player.getName(), world, "build", true)) {
                                event.setCancelled(true);
                                return;
                            }
                        }
                    }
                        /*if (heldItem == Material.ARMOR_STAND) { //TODO: armor stand
                            perms = Residence.getPermsByLocForPlayer(block.getRelative(event.getBlockFace()).getLocation(), player);
                            if (!perms.playerHas(player.getName(), world, "build", true)) {
                                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                                event.setCancelled(true);
                                return;
                            }
                        }*/

                    if (block.getId() == Block.ITEM_FRAME_BLOCK) {
                        if (!perms.playerHas(player.getName(), world, "container", perms.playerHas(player.getName(), world, "use", true))) {
                            event.setCancelled(true);
                            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
                        }
                    }
                }

                if (isContainer(mat, block) || isCanUseEntity(mat, block)) {
                    boolean hasuse = perms.playerHas(player.getName(), world, "use", true);
                    for (Entry<Integer, String> checkMat : FlagPermissions.getMaterialUseFlagList().entrySet()) {
                        if (mat == checkMat.getKey()) {
                            if (!perms.playerHas(player.getName(), world, checkMat.getValue(), hasuse)) {
                                if (hasuse || checkMat.getValue().equals("container")) {
                                    event.setCancelled(true);
                                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", checkMat.getValue()));
                                    return;
                                } else {
                                    event.setCancelled(true);
                                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
                                    return;
                                }
                            }
                        }
                    }
                    if (Residence.getConfigManager().getCustomContainers().contains(block.getId())) {
                        if (!perms.playerHas(player.getName(), world, "container", hasuse)) {
                            event.setCancelled(true);
                            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
                            return;
                        }
                    }
                    if (Residence.getConfigManager().getCustomBothClick().contains(block.getId())) {
                        if (!hasuse) {
                            event.setCancelled(true);
                            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
                            return;
                        }
                    }
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        if (Residence.getConfigManager().getCustomRightClick().contains(block.getId())) {
                            if (!hasuse) {
                                event.setCancelled(true);
                                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "use"));
                                return;
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        Entity ent = event.getEntity();
        /* Trade */
        /*if (ent.getType() == EntityType.VILLAGER) { //TODO: trade
            ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getPlayer().getLocation());

            if (res != null && !res.getPermissions().playerHas(player.getName(), "trade", true)) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                event.setCancelled(true);
            }
        }*/

        /* Container - ItemFrame protection */
        /*Item heldItem = event.getItem(); //item frame is not entity
        if (!(ent instanceof Hanging)) {
            return;
        }
        Hanging hanging = (Hanging) ent;
        if (hanging.getType() != EntityType.ITEM_FRAME) {
            return;
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(ent.getLocation(), player);
        String world = player.getLevel().getName();
        String permgroup = Residence.getPermissionManager().getGroupNameByPlayer(player);
        if (!Residence.getItemManager().isAllowed(heldItem, permgroup, world)) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ItemBlacklisted"));
            event.setCancelled(true);
            return;
        }
        if (!perms.playerHas(player.getName(), world, "container", perms.playerHas(player.getName(), world, "use", true))) {
            event.setCancelled(true);
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
        }*/
    }

    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true) //TODO: armor stands
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }

        Entity ent = event.getRightClicked();
        if (ent.getType() != EntityType.ARMOR_STAND) {
            return;
        }

        FlagPermissions perms = Residence.getPermsByLocForPlayer(ent.getLocation(), player);
        String world = player.getLevel().getName();

        if (!perms.playerHas(player.getName(), world, "container", perms.playerHas(player.getName(), world, "use", true))) {
            event.setCancelled(true);
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
        }
    }*/

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerFoodChange(PlayerFoodLevelChangeEvent e) {
        Player p = e.getPlayer();

        if (e.getFoodLevel() >= p.getFoodData().getLevel()) {
            return;
        }

        FlagPermissions perms = Residence.getPermsByLocForPlayer(p, p);

        if (!perms.has("starvation", true)) {
            e.setCancelled();
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        String pname = player.getName();
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlockClicked().getLocation());
        if (res != null) {
            if (Residence.getConfigManager().preventRentModify() && Residence.getConfigManager().enabledRentSystem()) {
                if (Residence.getRentManager().isRented(res.getName())) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlockClicked().getLocation(), player);
        if (!perms.playerHas(pname, player.getLevel().getName(), "bucket", perms.playerHas(pname, player.getLevel().getName(), "build", true))) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "bucket"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerBucketFill(PlayerBucketFillEvent event) {
        Player player = event.getPlayer();
        String pname = player.getName();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(event.getBlockClicked().getLocation());
        if (res != null) {
            if (Residence.getConfigManager().preventRentModify() && Residence.getConfigManager().enabledRentSystem()) {
                if (Residence.getRentManager().isRented(res.getName())) {
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("RentedModifyDeny"));
                    event.setCancelled(true);
                    return;
                }
            }
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getBlockClicked().getLocation(), player);
        boolean hasbucket = perms.playerHas(pname, player.getLevel().getName(), "bucket", perms.playerHas(pname, player.getLevel().getName(), "build", true));
        if (!hasbucket) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "bucket"));
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Location loc = event.getTo();
        Player player = event.getPlayer();

        if (Residence.isResAdminOn(player)) {
            handleNewLocation(player, loc, false);
            return;
        }

        ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
        if (event.getCause() == TeleportCause.ENDER_PEARL || event.getCause() == TeleportCause.COMMAND || event.getCause() == TeleportCause.NETHER_PORTAL) {
            if (res != null) {
                String areaname = Residence.getResidenceManager().getNameByLoc(loc);
                if (!res.getPermissions().playerHas(player.getName(), "move", true)) {
                    event.setCancelled(true);
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ResidenceMoveDeny", areaname));
                    return;
                }
            }
        }
        if (event.getCause() == TeleportCause.PLUGIN) {
            if (res != null) {
                String areaname = Residence.getResidenceManager().getNameByLoc(loc);
                if (!res.getPermissions().playerHas(player.getName(), "tp", true) && !player.hasPermission("residence.admin.tp")) {
                    event.setCancelled(true);
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("TeleportDeny", areaname));
                    return;
                }
            }
        }
        handleNewLocation(player, loc, false);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player == null) {
            return;
        }
        long last = lastUpdate.get(player.getId());
        long now = System.currentTimeMillis();
        if (now - last < Residence.getConfigManager().getMinMoveUpdateInterval()) {
            return;
        }
        lastUpdate.put(player.getId(), now);
        if (event.getFrom().getLevel().getId() == event.getTo().getLevel().getId()) {
            if (event.getFrom().distance(event.getTo()) == 0) {
                return;
            }
        }
        handleNewLocation(player, event.getTo(), true);
    }

    public void handleNewLocation(Player player, Location loc, boolean move) {
        String pname = player.getName();

        ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);
        String areaname = null;
        boolean chatchange = false;
        String subzone = null;
        if (res != null) {
            areaname = Residence.getResidenceManager().getNameByLoc(loc);
            while (res.getSubzoneByLoc(player.getLocation()) != null) {
                subzone = res.getSubzoneNameByLoc(player.getLocation());
                res = res.getSubzoneByLoc(player.getLocation());
                areaname = areaname + "." + subzone;
            }
        }
        ClaimedResidence ResOld = null;
        if (currentRes.containsKey(pname)) {
            ResOld = Residence.getResidenceManager().getByName(currentRes.get(pname));
            if (ResOld == null) {
                currentRes.remove(pname);
            }
        }
        if (res == null) {
            lastOutsideLoc.put(pname, loc);
            if (ResOld != null) {
                String leave = ResOld.getLeaveMessage();
                /*
                 * TODO - ResidenceLeaveEvent is deprecated as of 21-MAY-2013. Its functionality is replaced by
                 * ResidenceChangedEvent. For now, this event is still supported until it is removed at a
                 * suitable time in the future.
                 */
                ResidenceLeaveEvent leaveevent = new ResidenceLeaveEvent(ResOld, player);
                Residence.getServ().getPluginManager().callEvent(leaveevent);

                // New ResidenceChangeEvent
                ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(ResOld, null, player);
                Residence.getServ().getPluginManager().callEvent(chgEvent);

                if (leave != null && !leave.equals("")) {
                    if (Residence.getConfigManager().useActionBar()) {
                        ActionBar.send(player, (new StringBuilder()).append(TextFormat.YELLOW).append(insertMessages(player, ResOld.getName(), ResOld, leave)).toString());
                    } else {
                        player.sendMessage(TextFormat.YELLOW + this.insertMessages(player, ResOld.getName(), ResOld, leave));
                    }
                }
                currentRes.remove(pname);
                Residence.getChatManager().removeFromChannel(pname);
            }
            return;
        }
        if (move) {
            if (!res.getPermissions().playerHas(pname, "move", true) && !Residence.isResAdminOn(player) && !player.hasPermission("residence.admin.move")) {
                Location lastLoc = lastOutsideLoc.get(pname);
                if (lastLoc != null) {
                    player.teleport(lastLoc);
                } else {
                    player.teleport(res.getOutsideFreeLoc(loc));
                }
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ResidenceMoveDeny", res.getName().split("\\.")[res.getName().split("\\.").length - 1]));
                return;
            }
        }
        lastOutsideLoc.put(pname, loc);
        if (!currentRes.containsKey(pname) || ResOld != res) {
            currentRes.put(pname, areaname);
            if (subzone == null) {
                chatchange = true;
            }

            // "from" residence for ResidenceChangedEvent
            ClaimedResidence chgFrom = null;
            if (ResOld != res && ResOld != null) {
                String leave = ResOld.getLeaveMessage();
                chgFrom = ResOld;

                /*
                 * TODO - ResidenceLeaveEvent is deprecated as of 21-MAY-2013. Its functionality is replaced by
                 * ResidenceChangedEvent. For now, this event is still supported until it is removed at a
                 * suitable time in the future.
                 */
                ResidenceLeaveEvent leaveevent = new ResidenceLeaveEvent(ResOld, player);
                Residence.getServ().getPluginManager().callEvent(leaveevent);

                if (leave != null && !leave.equals("") && ResOld != res.getParent()) {
                    if (Residence.getConfigManager().useActionBar()) {
                        ActionBar.send(player, (new StringBuilder()).append(TextFormat.YELLOW).append(insertMessages(player, ResOld.getName(), ResOld, leave)).toString());
                    } else {
                        player.sendMessage(TextFormat.YELLOW + this.insertMessages(player, ResOld.getName(), ResOld, leave));
                    }
                }
            }
            String enterMessage = res.getEnterMessage();

            /*
             * TODO - ResidenceEnterEvent is deprecated as of 21-MAY-2013. Its functionality is replaced by
             * ResidenceChangedEvent. For now, this event is still supported until it is removed at a
             * suitable time in the future.
             */
            ResidenceEnterEvent enterevent = new ResidenceEnterEvent(res, player);
            Residence.getServ().getPluginManager().callEvent(enterevent);

            // New ResidenceChangedEvent
            ResidenceChangedEvent chgEvent = new ResidenceChangedEvent(chgFrom, res, player);
            Residence.getServ().getPluginManager().callEvent(chgEvent);

            if (enterMessage != null && !enterMessage.equals("") && !(ResOld != null && res == ResOld.getParent())) {
                if (Residence.getConfigManager().useActionBar()) {
                    ActionBar.send(player, (new StringBuilder()).append(TextFormat.YELLOW).append(insertMessages(player, areaname, res, enterMessage)).toString());
                } else {
                    player.sendMessage(TextFormat.YELLOW + this.insertMessages(player, areaname, res, enterMessage));
                }
            }
        }
        if (chatchange && chatenabled) {
            Residence.getChatManager().setChannel(pname, areaname);
        }
    }

    public String insertMessages(Player player, String areaname, ClaimedResidence res, String message) {
        try {
            message = message.replaceAll("%player", player.getName());
            message = message.replaceAll("%owner", res.getPermissions().getOwner());
            message = message.replaceAll("%residence", areaname);
        } catch (Exception ex) {
            return "";
        }
        return message;
    }

    public void doHeals() {
        try {
            for (Player player : Server.getInstance().getOnlinePlayers().values()) {
                String resname = Residence.getPlayerListener().getCurrentResidenceName(player.getName());
                ClaimedResidence res = null;
                if (resname != null) {
                    res = Residence.getResidenceManager().getByName(resname);
                }
                if (res != null) {
                    if (res.getPermissions().has("healing", false)) {
                        double health = player.getHealth();
                        if (health < 20 && player.isAlive()) {
                            player.heal(new EntityRegainHealthEvent(player, 1, EntityRegainHealthEvent.CAUSE_CUSTOM));
                        }
                    }

                    if (res.getPermissions().has("feeding", false)) {
                        PlayerFood food = player.getFoodData();

                        double current = food.getLevel();

                        if (current < food.getMaxLevel() && player.isAlive()) {
                            food.addFoodLevel(1, 1);
                        }
                    }
                }
            }
        } catch (Exception ex) {
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(PlayerChatEvent event) {
        String pname = event.getPlayer().getName();
        if (chatenabled && playerToggleChat.contains(pname)) {
            String area = currentRes.get(pname);
            if (area != null) {
                ChatChannel channel = Residence.getChatManager().getChannel(area);
                if (channel != null) {
                    channel.chat(pname, event.getMessage());
                }
                event.setCancelled(true);
            }
        }
    }

    public void tooglePlayerResidenceChat(Player player) {
        String pname = player.getName();
        if (playerToggleChat.contains(pname)) {
            playerToggleChat.remove(pname);
            player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("ResidenceChat", TextFormat.RED + "OFF" + TextFormat.YELLOW + "!"));
        } else {
            playerToggleChat.add(pname);
            player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("ResidenceChat", TextFormat.RED + "ON" + TextFormat.YELLOW + "!"));
        }
    }

    public String getCurrentResidenceName(String player) {
        return currentRes.get(player);
    }
}

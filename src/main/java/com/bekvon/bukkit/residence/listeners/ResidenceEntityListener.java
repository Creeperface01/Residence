/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.listeners;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityFallingBlock;
import cn.nukkit.entity.item.EntityMinecartTNT;
import cn.nukkit.entity.item.EntityPrimedTNT;
import cn.nukkit.entity.mob.EntityCreeper;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.EventPriority;
import cn.nukkit.event.Listener;
import cn.nukkit.event.entity.*;
import cn.nukkit.utils.TextFormat;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.FlagPermissions;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class ResidenceEntityListener implements Listener {

    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEndermanChangeBlock(EntityBlockChangeEvent event) {
        if (event.getEntityType() != EntityType.ENDERMAN && event.getEntityType() != EntityType.WITHER) {
            return;
        }
        FlagPermissions perms = Residence.getPermsByLoc(event.getBlock().getLocation());
        FlagPermissions world = Residence.getWorldFlags().getPerms(event.getBlock().getLevel().getName());
        if (event.getEntityType() == EntityType.WITHER) {
            if (!perms.has("wither", perms.has("explode", world.has("wither", world.has("explode", true))))) {
                event.setCancelled(true);
            }
        } else if (!perms.has("build", true)) {
            event.setCancelled(true);
        }
    }*/

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityInteract(EntityInteractEvent event) {
        Block block = event.getBlock();

        Entity entity = event.getEntity();
        FlagPermissions perms = Residence.getPermsByLoc(block.getLocation());
        boolean hastrample = perms.has("trample", perms.has("hasbuild", true));
        if (!hastrample && !(entity instanceof EntityFallingBlock) && (block.getId() == Block.FARMLAND || block.getId() == Block.SOUL_SAND)) {
            event.setCancelled(true);
        }
    }

    private boolean isMonster(Entity ent) {
        //return (ent instanceof Monster || ent instanceof Slime || ent instanceof Ghast);
        return false;
    }

    private boolean isAnimal(Entity ent) {
        return false;
        //return (ent instanceof Horse || ent instanceof Bat || ent instanceof Snowman || ent instanceof IronGolem || ent instanceof Ocelot || ent instanceof Pig || ent instanceof Sheep || ent instanceof Chicken || ent instanceof Wolf || ent instanceof Cow || ent instanceof Squid || ent instanceof Villager || ent instanceof Rabbit);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void AnimalKilling(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();

        if (!(damager instanceof Player)) {
            return;
        }

        Player cause = (Player) damager;

        if (Residence.isResAdminOn(cause)) {
            return;
        }

        Entity entity = event.getEntity();
        ClaimedResidence res = Residence.getResidenceManager().getByLoc(entity.getLocation());

        if (res != null && !res.getPermissions().playerHas(cause.getName(), "animalkilling", true)) {
            if (isAnimal(entity)) {
                cause.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                event.setCancelled(true);
            }
        }
    }

    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getLocation());
        Entity ent = event.getEntity();
        if (isAnimal(ent)) {
            if (!perms.has("animals", true)) {
                event.setCancelled(true);
            }
        } else {
            if (!perms.has("monsters", true) && isMonster(ent)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingPlace(HangingPlaceEvent event) {
        Player player = event.getPlayer();
        if (Residence.isResAdminOn(player)) {
            return;
        }
        FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getEntity().getLocation(), player);
        String pname = player.getName();
        String world = player.getLevel().getName();
        if (!perms.playerHas(pname, world, "place", perms.playerHas(pname, world, "build", true))) {
            event.setCancelled(true);
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onHangingBreak(HangingBreakEvent event) {
        if (event instanceof HangingBreakByEntityEvent) {
            HangingBreakByEntityEvent evt = (HangingBreakByEntityEvent) event;
            if (evt.getRemover() instanceof Player) {
                Player player = (Player) evt.getRemover();
                if (Residence.isResAdminOn(player)) {
                    return;
                }
                String pname = player.getName();
                FlagPermissions perms = Residence.getPermsByLocForPlayer(event.getEntity().getLocation(), player);
                String world = event.getEntity().getLevel().getName();
                if (!perms.playerHas(pname, world, "destroy", perms.playerHas(pname, world, "build", true))) {
                    event.setCancelled(true);
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                }
            }
        }
    }*/

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
        if (!perms.has("burn", true)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        int entity = event.getEntity().getNetworkId();

        FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
        if (entity == EntityCreeper.NETWORK_ID) {
            if (!perms.has("creeper", perms.has("explode", true))) {
                event.setCancelled(true);
                event.getEntity().close();
            }
        }
        if (entity == EntityPrimedTNT.NETWORK_ID || entity == EntityMinecartTNT.NETWORK_ID) {
            if (!perms.has("tnt", perms.has("explode", true))) {
                event.setCancelled(true);
                event.getEntity().close();
            }
        }
        /*if (entity == EntityType.FIREBALL) {
            if (!perms.has("fireball", perms.has("explode", true))) {
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }
        if (entity == EntityType.SMALL_FIREBALL) {
            if (!perms.has("fireball", perms.has("explode", true))) {
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }
        if (entity == EntityType.WITHER_SKULL) {
            if (!perms.has("witherdamage", perms.has("damage", true))) {
                event.setCancelled(true);
                event.getEntity().remove();
            }
        }*/
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.isCancelled() || event.getEntity() == null) {
            return;
        }
        boolean cancel = false;
        int entity = event.getEntity().getNetworkId();

        FlagPermissions perms = Residence.getPermsByLoc(event.getEntity().getLocation());
        FlagPermissions world = Residence.getWorldFlags().getPerms(event.getEntity().getLevel().getName());
        if (entity == EntityCreeper.NETWORK_ID) {
            if (!perms.has("creeper", perms.has("explode", true))) {
                cancel = true;
            }
        }
        if (entity == EntityPrimedTNT.NETWORK_ID || entity == EntityMinecartTNT.NETWORK_ID) {
            if (!perms.has("tnt", perms.has("explode", true))) {
                cancel = true;
            }
        }
        /*if (entity == EntityFireBall.NETWORK_ID) {
            if (!perms.has("fireball", perms.has("explode", true))) {
                cancel = true;
            }
        }*/
        /*if (entity == EntityType.SMALL_FIREBALL) {
            if (!perms.has("fireball", perms.has("explode", true))) {
                cancel = true;
            }
        }*/
        /*if (entity == EntityType.WITHER_SKULL || entity == EntityType.WITHER) {
            if (!perms.has("wither", perms.has("explode", world.has("wither", world.has("explode", true))))) {
                cancel = true;
            }
        }*/
        if (cancel) {
            event.setCancelled();
        } else {
            List<Block> preserve = new ArrayList<>();
            for (Block block : event.getBlockList()) {
                FlagPermissions blockperms = Residence.getPermsByLoc(block.getLocation());
                if ((!blockperms.has("wither", blockperms.has("explode", world.has("wither", world.has("explode", true)))) && (false/*entity == EntityType.WITHER || entity == EntityType.WITHER_SKULL*/) || (!blockperms.has("fireball", blockperms.has("explode", true)) && (false/*entity == EntityType.FIREBALL || entity == EntityType.SMALL_FIREBALL*/)) || (!blockperms.has("tnt", blockperms.has("explode", true)) && (entity == EntityPrimedTNT.NETWORK_ID || entity == EntityMinecartTNT.NETWORK_ID)) || (!blockperms.has("creeper", blockperms.has("explode", true)) && entity == EntityCreeper.NETWORK_ID))) {
                    preserve.add(block);
                }
            }

            for (Block block : preserve) {
                event.getBlockList().remove(block);
            }
        }
    }

    /*@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSplashPotion(PotionSplashEvent event) { //TODO: splash potion
        if (event.isCancelled()) {
            return;
        }
        Entity ent = event.getEntity();
        boolean srcpvp = Residence.getPermsByLoc(ent.getLocation()).has("pvp", true);
        Iterator<LivingEntity> it = event.getAffectedEntities().iterator();
        while (it.hasNext()) {
            LivingEntity target = it.next();
            if (target.getType() == EntityType.PLAYER) {
                Boolean tgtpvp = Residence.getPermsByLoc(target.getLocation()).has("pvp", true);
                if (!srcpvp || !tgtpvp) {
                    event.setIntensity(target, 0);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent event) { //TODO: armor stand
        if (event.getEntityType() == EntityType.ARMOR_STAND) {
            Entity dmgr = event.getDamager();

            Location loc = event.getEntity().getLocation();
            ClaimedResidence res = Residence.getResidenceManager().getByLoc(loc);

            if (res != null) {

                if ((dmgr instanceof Projectile) && (!(((Projectile) dmgr).getShooter() instanceof Player))) {
                    if (!res.getPermissions().has("container", true)) {
                        event.setCancelled(true);
                        return;
                    }
                }

                Player player;
                if (dmgr instanceof Player) {
                    player = (Player) event.getDamager();
                } else {
                    if (dmgr instanceof Projectile && ((Projectile) dmgr).getShooter() instanceof Player) {
                        player = (Player) ((Projectile) dmgr).getShooter();
                    } else {
                        return;
                    }
                }

                if (Residence.isResAdminOn(player)) {
                    return;
                }

                if (!res.getPermissions().playerHas(player.getName(), "container", true)) {
                    event.setCancelled(true);
                    player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("FlagDeny", "container"));
                }
            }
        }
    }*/

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        Entity ent = event.getEntity();
        if (ent.hasMetadata("NPC")) {
            return;
        }
        boolean tamedWolf = /*ent instanceof EntityWolf ? ((Wolf) ent).isTamed() :*/ false;
        ClaimedResidence area = Residence.getResidenceManager().getByLoc(ent.getLocation());
        // Living Entities
        if (event instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent attackevent = (EntityDamageByEntityEvent) event;
            Entity damager = attackevent.getDamager();
            ClaimedResidence srcarea = null;
            if (damager != null) {
                srcarea = Residence.getResidenceManager().getByLoc(damager.getLocation());
            }
            boolean srcpvp = true;
            if (srcarea != null) {
                srcpvp = srcarea.getPermissions().has("pvp", true);
            }
            ent = attackevent.getEntity();
            if ((ent instanceof Player || tamedWolf) && damager instanceof Player) {
                Player attacker = (Player) damager;

                if (!srcpvp) {
                    attacker.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPVPZone"));
                    event.setCancelled(true);
                    return;
                }
                // Check for Player vs Player
                if (area == null) {
                    // World PvP
                    if (!Residence.getWorldFlags().getPerms(damager.getLevel().getName()).has("pvp", true)) {
                        //attacker.sendMessage(TextFormat.RED+Residence.getLanguage().getPhrase("WorldPVPDisabled"));
                        event.setCancelled(true);
                    }
                } else {
                    // Normal PvP
                    if (!area.getPermissions().has("pvp", true)) {
                        //attacker.sendMessage(TextFormat.RED+Residence.getLanguage().getPhrase("NoPVPZone"));
                        event.setCancelled(true);
                    }
                }
                return;
            } else if ((ent instanceof Player || tamedWolf) && (damager instanceof EntityCreeper)) {
                if (area == null) {
                    if (!Residence.getWorldFlags().getPerms(damager.getLevel().getName()).has("creeper", true)) {
                        event.setCancelled(true);
                    }
                } else {
                    if (!area.getPermissions().has("creeper", true)) {
                        event.setCancelled(true);
                    }
                }
            }
        }
        if (area == null) {
            if (!Residence.getWorldFlags().getPerms(ent.getLevel().getName()).has("damage", true) && (ent instanceof Player || tamedWolf)) {
                event.setCancelled(true);
            }
        } else {
            if (!area.getPermissions().has("damage", true) && (ent instanceof Player || tamedWolf)) {
                event.setCancelled(true);
            }
        }
        if (event.isCancelled()) {
            // Put out a fire on a player
            if ((ent instanceof Player || tamedWolf)
                    && (event.getCause() == EntityDamageEvent.DamageCause.FIRE
                    || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)) {
                ent.setOnFire(0);
            }
        }
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.selection;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import com.boydti.fawe.object.FawePlayer;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.regions.RegionSelector;
import com.sk89q.worldedit.regions.selector.CuboidRegionSelector;

/**
 * @author Administrator
 */
public class WorldEditSelectionManager extends SelectionManager {

    public WorldEditSelectionManager(Server serv) {
        super(serv);
    }

    @Override
    public boolean worldEdit(Player player) {
        FawePlayer<Player> data = FawePlayer.wrap(player);

        if (data != null) {
            RegionSelector manager = data.getSession().getRegionSelector(data.getWorld());

            if (manager instanceof CuboidRegionSelector) {
                CuboidRegionSelector sel = new CuboidRegionSelector(data.getWorld());

                if (sel.position1 != null) {
                    this.playerLoc1.put(player.getName(), new Position(sel.position1.getX(), sel.position1.getY(), sel.position1.getZ(), player.getLevel()));
                }

                if (sel.position2 != null) {
                    this.playerLoc2.put(player.getName(), new Position(sel.position2.getX(), sel.position2.getY(), sel.position2.getZ(), player.getLevel()));
                }
            }
        }

        return false;
    }

    private void afterSelectionUpdate(Player player) {
        if (hasPlacedBoth(player.getName())) {
            FawePlayer<Player> data = FawePlayer.wrap(player);

            if (data != null) {
                RegionSelector manager = data.getSession().getRegionSelector(data.getWorld());

                if (!(manager instanceof CuboidRegionSelector)) {
                    manager = new CuboidRegionSelector(data.getWorld());
                }

                manager.selectPrimary(wrapVector(playerLoc1.get(player.getName()).clone()), null);
                manager.selectSecondary(wrapVector(playerLoc2.get(player.getName()).clone()), null);
            }
        }
    }

    @Override
    public void placeLoc1(Player player, Position loc) {
        this.worldEdit(player);
        super.placeLoc1(player, loc);
        this.afterSelectionUpdate(player);
    }

    @Override
    public void placeLoc2(Player player, Position loc) {
        this.worldEdit(player);
        super.placeLoc2(player, loc);
        this.afterSelectionUpdate(player);
    }

    @Override
    public void sky(Player player, boolean resadmin) {
        this.worldEdit(player);
        super.sky(player, resadmin);
        afterSelectionUpdate(player);
    }

    @Override
    public void bedrock(Player player, boolean resadmin) {
        this.worldEdit(player);
        super.bedrock(player, resadmin);
        afterSelectionUpdate(player);
    }

    @Override
    public void modify(Player player, boolean shift, int amount) {
        this.worldEdit(player);
        super.modify(player, shift, amount);
        afterSelectionUpdate(player);
    }

    @Override
    public void selectChunk(Player player) {
        this.worldEdit(player);
        super.selectChunk(player);
        afterSelectionUpdate(player);
    }

    @Override
    public void showSelectionInfo(Player player) {
        this.worldEdit(player);
        super.showSelectionInfo(player);
    }

    private Vector wrapVector(Vector3 v) {
        return new Vector(v.x, v.y, v.z);
    }
}
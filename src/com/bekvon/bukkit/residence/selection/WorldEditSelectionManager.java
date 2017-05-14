/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.selection;

import WorldEdit.WorldEdit;
import WorldEdit.Selection;
import WorldEdit.PlayerData;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;

/**
 *
 * @author Administrator
 */
public class WorldEditSelectionManager extends SelectionManager {

    public WorldEditSelectionManager(Server serv) {
        super(serv);
    }

    @Override
    public boolean worldEdit(Player player) {
        WorldEdit wep = (WorldEdit) server.getPluginManager().getPlugin("WorldEdit");

        PlayerData data = wep.getPlayerData(player);
        Selection sel = null;

        if (data != null) {
            sel = data.getSelection();
        }

        if (sel != null) {

            Location pos1 = new Location(Math.min(sel.pos1.x, sel.pos2.x), Math.min(sel.pos1.y, sel.pos2.y), Math.min(sel.pos1.z, sel.pos2.z), 0, 0, sel.pos1.getLevel());
            Location pos2 = new Location(Math.max(sel.pos1.x, sel.pos2.x), Math.max(sel.pos1.y, sel.pos2.y), Math.max(sel.pos1.z, sel.pos2.z), 0, 0, sel.pos1.getLevel());

            this.playerLoc1.put(player.getName(), pos1);
            this.playerLoc2.put(player.getName(), pos2);
            return true;
        }
        return false;
    }

    private void afterSelectionUpdate(Player player) {
        if (hasPlacedBoth(player.getName())) {
            WorldEdit wep = (WorldEdit) server.getPluginManager().getPlugin("WorldEdit");
            Level level = playerLoc1.get(player.getName()).getLevel();

            PlayerData data = wep.getPlayerData(player);

            if (data != null) {
                data.getSelection().pos1 = playerLoc1.get(player.getName()).clone();
                data.getSelection().pos2 = playerLoc2.get(player.getName()).clone();
            }
        }
    }

    @Override
    public void placeLoc1(Player player, Location loc) {
        this.worldEdit(player);
        super.placeLoc1(player, loc);
        this.afterSelectionUpdate(player);
    }

    @Override
    public void placeLoc2(Player player, Location loc) {
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
}

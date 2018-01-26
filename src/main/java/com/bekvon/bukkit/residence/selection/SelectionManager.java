/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.selection;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.utils.TextFormat;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.protection.CuboidArea;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 */
public class SelectionManager {

    protected Map<String, Position> playerLoc1;
    protected Map<String, Position> playerLoc2;
    protected Server server;

    public static final int MAX_HEIGHT = 255, MIN_HEIGHT = 0;

    public enum Direction {
        UP, DOWN, PLUSX, PLUSZ, MINUSX, MINUSZ
    }

    public SelectionManager(Server server) {
        this.server = server;
        playerLoc1 = Collections.synchronizedMap(new HashMap<String, Position>());
        playerLoc2 = Collections.synchronizedMap(new HashMap<String, Position>());
    }

    public void placeLoc1(Player player, Position loc) {
        if (loc != null) {
            playerLoc1.put(player.getName(), loc);
        }
    }

    public void placeLoc2(Player player, Position loc) {
        if (loc != null) {
            playerLoc2.put(player.getName(), loc);
        }
    }

    public Position getPlayerLoc1(String player) {
        return playerLoc1.get(player);
    }

    public Position getPlayerLoc2(String player) {
        return playerLoc2.get(player);
    }

    public boolean hasPlacedBoth(String player) {
        return (playerLoc1.containsKey(player) && playerLoc2.containsKey(player));
    }

    public void showSelectionInfo(Player player) {
        String pname = player.getName();
        if (this.hasPlacedBoth(pname)) {
            CuboidArea cuboidArea = new CuboidArea(getPlayerLoc1(pname), getPlayerLoc2(pname));
            player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Selection.Total.Size") + ":" + TextFormat.DARK_AQUA + " " + cuboidArea.getSize());
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            if (Residence.getConfigManager().enableEconomy()) {
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Land.Cost") + ":" + TextFormat.DARK_AQUA + " " + ((int) Math.ceil((double) cuboidArea.getSize() * group.getCostPerBlock())));
            }
            player.sendMessage(TextFormat.YELLOW + "X" + Residence.getLanguage().getPhrase("Size") + ":" + TextFormat.DARK_AQUA + " " + cuboidArea.getXSize());
            player.sendMessage(TextFormat.YELLOW + "Y" + Residence.getLanguage().getPhrase("Size") + ":" + TextFormat.DARK_AQUA + " " + cuboidArea.getYSize());
            player.sendMessage(TextFormat.YELLOW + "Z" + Residence.getLanguage().getPhrase("Size") + ":" + TextFormat.DARK_AQUA + " " + cuboidArea.getZSize());
        } else {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SelectPoints"));
        }
    }

    public void vert(Player player, boolean resadmin) {
        if (hasPlacedBoth(player.getName())) {
            this.sky(player, resadmin);
            this.bedrock(player, resadmin);
        } else {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SelectPoints"));
        }
    }

    public void sky(Player player, boolean resadmin) {
        if (hasPlacedBoth(player.getName())) {
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            int y1 = playerLoc1.get(player.getName()).getFloorY();
            int y2 = playerLoc2.get(player.getName()).getFloorY();
            if (y1 > y2) {
                int newy = MAX_HEIGHT;
                if (!resadmin) {
                    if (group.getMaxHeight() < newy) {
                        newy = group.getMaxHeight();
                    }
                    if (newy - y2 > (group.getMaxY() - 1)) {
                        newy = y2 + (group.getMaxY() - 1);
                    }
                }
                playerLoc1.get(player.getName()).y = newy;
            } else {
                int newy = MAX_HEIGHT;
                if (!resadmin) {
                    if (group.getMaxHeight() < newy) {
                        newy = group.getMaxHeight();
                    }
                    if (newy - y1 > (group.getMaxY() - 1)) {
                        newy = y1 + (group.getMaxY() - 1);
                    }
                }
                playerLoc2.get(player.getName()).y = newy;
            }
            player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SelectionSky"));
        } else {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SelectPoints"));
        }
    }

    public void bedrock(Player player, boolean resadmin) {
        if (hasPlacedBoth(player.getName())) {
            PermissionGroup group = Residence.getPermissionManager().getGroup(player);
            int y1 = playerLoc1.get(player.getName()).getFloorY();
            int y2 = playerLoc2.get(player.getName()).getFloorY();
            if (y1 < y2) {
                int newy = MIN_HEIGHT;
                if (!resadmin) {
                    if (newy < group.getMinHeight()) {
                        newy = group.getMinHeight();
                    }
                    if (y2 - newy > (group.getMaxY() - 1)) {
                        newy = y2 - (group.getMaxY() - 1);
                    }
                }
                playerLoc1.get(player.getName()).y = newy;
            } else {
                int newy = MIN_HEIGHT;
                if (!resadmin) {
                    if (newy < group.getMinHeight()) {
                        newy = group.getMinHeight();
                    }
                    if (y1 - newy > (group.getMaxY() - 1)) {
                        newy = y1 - (group.getMaxY() - 1);
                    }
                }
                playerLoc2.get(player.getName()).y = newy;
            }
            player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SelectionBedrock"));
        } else {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SelectPoints"));
        }
    }

    public void clearSelection(Player player) {
        playerLoc1.remove(player.getName());
        playerLoc2.remove(player.getName());
    }

    public void selectChunk(Player player) {
        FullChunk chunk = player.getLevel().getChunk(player.getFloorX() >> 4, player.getFloorZ() >> 4);
        int xcoord = chunk.getX() * 16;
        int zcoord = chunk.getZ() * 16;
        int ycoord = MIN_HEIGHT;
        int xmax = xcoord + 15;
        int zmax = zcoord + 15;
        int ymax = MAX_HEIGHT;
        this.playerLoc1.put(player.getName(), new Position(xcoord, ycoord, zcoord, player.getLevel()));
        this.playerLoc2.put(player.getName(), new Position(xmax, ymax, zmax, player.getLevel()));
        player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SelectionSuccess"));
    }

    public boolean worldEdit(Player player) {
        player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("WorldEditNotFound"));
        return false;
    }

    public void selectBySize(Player player, int xsize, int ysize, int zsize) {
        Position myloc = player.getLocation();
        Position loc1 = new Position(myloc.getFloorX() + xsize, myloc.getFloorY() + ysize, myloc.getFloorZ() + zsize, myloc.getLevel());
        Position loc2 = new Position(myloc.getFloorX() - xsize, myloc.getFloorY() - ysize, myloc.getFloorZ() - zsize, myloc.getLevel());
        placeLoc1(player, loc1);
        placeLoc2(player, loc2);
        player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("SelectionSuccess"));
        showSelectionInfo(player);
    }

    public void modify(Player player, boolean shift, int amount) {
        if (!this.hasPlacedBoth(player.getName())) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SelectPoints"));
            return;
        }
        Direction d = this.getDirection(player);
        if (d == null) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidDirection"));
        }
        CuboidArea area = new CuboidArea(playerLoc1.get(player.getName()), playerLoc2.get(player.getName()));
        if (d == Direction.UP) {
            int oldy = area.getHighLoc().getFloorY();
            oldy = oldy + amount;
            if (oldy > MAX_HEIGHT) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SelectTooHigh"));
                oldy = MAX_HEIGHT;
            }
            area.getHighLoc().y = oldy;
            if (shift) {
                int oldy2 = area.getLowLoc().getFloorY();
                oldy2 = oldy2 + amount;
                area.getLowLoc().y = oldy2;
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Shifting.Up") + "...");
            } else {
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Expanding.Up") + "...");
            }
        }
        if (d == Direction.DOWN) {
            int oldy = area.getLowLoc().getFloorY();
            oldy = oldy - amount;
            if (oldy < MIN_HEIGHT) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("SelectTooLow"));
                oldy = MIN_HEIGHT;
            }
            area.getLowLoc().y = oldy;
            if (shift) {
                int oldy2 = area.getHighLoc().getFloorY();
                oldy2 = oldy2 - amount;
                area.getHighLoc().y = oldy2;
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Shifting.Down") + "...");
            } else {
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Expanding.Down") + "...");
            }
        }
        if (d == Direction.MINUSX) {
            int oldx = area.getLowLoc().getFloorX();
            oldx = oldx - amount;
            area.getLowLoc().x = oldx;
            if (shift) {
                int oldx2 = area.getHighLoc().getFloorX();
                oldx2 = oldx2 - amount;
                area.getHighLoc().x = oldx2;
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Shifting") + " -X...");
            } else {
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Expanding") + " -X...");
            }
        }
        if (d == Direction.PLUSX) {
            int oldx = area.getHighLoc().getFloorX();
            oldx = oldx + amount;
            area.getHighLoc().x = oldx;
            if (shift) {
                int oldx2 = area.getLowLoc().getFloorX();
                oldx2 = oldx2 + amount;
                area.getLowLoc().x = oldx2;
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Shifting") + " +X...");
            } else {
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Expanding") + " +X...");
            }
        }
        if (d == Direction.MINUSZ) {
            int oldz = area.getLowLoc().getFloorZ();
            oldz = oldz - amount;
            area.getLowLoc().z = oldz;
            if (shift) {
                int oldz2 = area.getHighLoc().getFloorZ();
                oldz2 = oldz2 - amount;
                area.getHighLoc().z = oldz2;
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Shifting") + " -Z...");
            } else {
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Expanding") + " -Z...");
            }
        }
        if (d == Direction.PLUSZ) {
            int oldz = area.getHighLoc().getFloorZ();
            oldz = oldz + amount;
            area.getHighLoc().z = oldz;
            if (shift) {
                int oldz2 = area.getLowLoc().getFloorZ();
                oldz2 = oldz2 + amount;
                area.getLowLoc().z = oldz2;
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Shifting") + " +Z...");
            } else {
                player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("Expanding") + " +Z...");
            }
        }
        playerLoc1.put(player.getName(), area.getHighLoc());
        playerLoc2.put(player.getName(), area.getLowLoc());
    }

    private Direction getDirection(Player player) {
        double pitch = player.getLocation().getPitch();
        double yaw = player.getLocation().getYaw();
        if (pitch < -50) {
            return Direction.UP;
        }
        if (pitch > 50) {
            return Direction.DOWN;
        }
        if ((yaw > 45 && yaw < 135) || (yaw < -45 && yaw > -135)) {
            return Direction.MINUSX;
        }
        if ((yaw > 225 && yaw < 315) || (yaw < -225 && yaw > -315)) {
            return Direction.PLUSX;
        }
        if ((yaw > 135 && yaw < 225) || (yaw < -135 && yaw > -225)) {
            return Direction.MINUSZ;
        }
        if ((yaw < 45 || yaw > 315) || (yaw > -45 || yaw < -315)) {
            return Direction.PLUSZ;
        }
        return null;
    }

}

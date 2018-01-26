/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.protection;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.protection.ResidenceManager.ChunkRef;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public class CuboidArea {

    protected Position highPoints;
    protected Position lowPoints;

    protected CuboidArea() {
    }

    public CuboidArea(Position startLoc, Position endLoc) {
        int highx, highy, highz, lowx, lowy, lowz;
        if (startLoc.getFloorX() > endLoc.getFloorX()) {
            highx = startLoc.getFloorX();
            lowx = endLoc.getFloorX();
        } else {
            highx = endLoc.getFloorX();
            lowx = startLoc.getFloorX();
        }
        if (startLoc.getFloorY() > endLoc.getFloorY()) {
            highy = startLoc.getFloorY();
            lowy = endLoc.getFloorY();
        } else {
            highy = endLoc.getFloorY();
            lowy = startLoc.getFloorY();
        }
        if (startLoc.getFloorZ() > endLoc.getFloorZ()) {
            highz = startLoc.getFloorZ();
            lowz = endLoc.getFloorZ();
        } else {
            highz = endLoc.getFloorZ();
            lowz = startLoc.getFloorZ();
        }
        highPoints = new Position(highx, highy, highz, startLoc.getLevel());
        lowPoints = new Position(lowx, lowy, lowz, startLoc.getLevel());
    }

    public boolean isAreaWithinArea(CuboidArea area) {
        return (this.containsLoc(area.highPoints) && this.containsLoc(area.lowPoints));
    }

    public boolean containsLoc(Position loc) {
        if (loc == null) {
            return false;
        }
        if (loc.getLevel().getId() != highPoints.getLevel().getId()) {
            return false;
        }
        if (lowPoints.getFloorX() <= loc.getFloorX() && highPoints.getFloorX() >= loc.getFloorX()) {
            if (lowPoints.getFloorZ() <= loc.getFloorZ() && highPoints.getFloorZ() >= loc.getFloorZ()) {
                if (lowPoints.getFloorY() <= loc.getFloorY() && highPoints.getFloorY() >= loc.getFloorY()) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkCollision(CuboidArea area) {
        if (!area.getWorld().equals(this.getWorld())) {
            return false;
        }
        if (area.containsLoc(lowPoints) || area.containsLoc(highPoints) || this.containsLoc(area.highPoints) || this.containsLoc(area.lowPoints)) {
            return true;
        }
        return advCuboidCheckCollision(highPoints, lowPoints, area.highPoints, area.lowPoints);
    }

    private boolean advCuboidCheckCollision(Position A1High, Position A1Low, Position A2High, Position A2Low) {
        int A1HX = A1High.getFloorX();
        int A1LX = A1Low.getFloorX();
        int A1HY = A1High.getFloorY();
        int A1LY = A1Low.getFloorY();
        int A1HZ = A1High.getFloorZ();
        int A1LZ = A1Low.getFloorZ();
        int A2HX = A2High.getFloorX();
        int A2LX = A2Low.getFloorX();
        int A2HY = A2High.getFloorY();
        int A2LY = A2Low.getFloorY();
        int A2HZ = A2High.getFloorZ();
        int A2LZ = A2Low.getFloorZ();
        if ((A1HX >= A2LX && A1HX <= A2HX) || (A1LX >= A2LX && A1LX <= A2HX) || (A2HX >= A1LX && A2HX <= A1HX) || (A2LX >= A1LX && A2LX <= A1HX)) {
            if ((A1HY >= A2LY && A1HY <= A2HY) || (A1LY >= A2LY && A1LY <= A2HY) || (A2HY >= A1LY && A2HY <= A1HY) || (A2LY >= A1LY && A2LY <= A1HY)) {
                if ((A1HZ >= A2LZ && A1HZ <= A2HZ) || (A1LZ >= A2LZ && A1LZ <= A2HZ) || (A2HZ >= A1LZ && A2HZ <= A1HZ) || (A2LZ >= A1LZ && A2LZ <= A1HZ)) {
                    return true;
                }
            }
        }
        return false;
    }

    public long getSize() {
        int xsize = (highPoints.getFloorX() - lowPoints.getFloorX()) + 1;
        int ysize = (highPoints.getFloorY() - lowPoints.getFloorY()) + 1;
        int zsize = (highPoints.getFloorZ() - lowPoints.getFloorZ()) + 1;
        return xsize * ysize * zsize;
    }

    public int getXSize() {
        return (highPoints.getFloorX() - lowPoints.getFloorX()) + 1;
    }

    public int getYSize() {
        return (highPoints.getFloorY() - lowPoints.getFloorY()) + 1;
    }

    public int getZSize() {
        return (highPoints.getFloorZ() - lowPoints.getFloorZ()) + 1;
    }

    public Position getHighLoc() {
        return highPoints;
    }

    public Position getLowLoc() {
        return lowPoints;
    }

    public Level getWorld() {
        return highPoints.getLevel();
    }

    public void save(DataOutputStream out, int version) throws IOException {
        out.writeUTF(highPoints.getLevel().getName());
        out.writeInt(highPoints.getFloorX());
        out.writeInt(highPoints.getFloorY());
        out.writeInt(highPoints.getFloorZ());
        out.writeInt(lowPoints.getFloorX());
        out.writeInt(lowPoints.getFloorY());
        out.writeInt(lowPoints.getFloorZ());
    }

    public static CuboidArea load(DataInputStream in, int version) throws IOException {
        CuboidArea newArea = new CuboidArea();
        Server server = Residence.getServ();
        Level level = server.getLevelByName(in.readUTF());
        int highx = in.readInt();
        int highy = in.readInt();
        int highz = in.readInt();
        int lowx = in.readInt();
        int lowy = in.readInt();
        int lowz = in.readInt();
        newArea.highPoints = new Position(highx, highy, highz, level);
        newArea.lowPoints = new Position(lowx, lowy, lowz, level);
        return newArea;
    }

    public Map<String, Object> save() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("X1", this.highPoints.getFloorX());
        root.put("Y1", this.highPoints.getFloorY());
        root.put("Z1", this.highPoints.getFloorZ());
        root.put("X2", this.lowPoints.getFloorX());
        root.put("Y2", this.lowPoints.getFloorY());
        root.put("Z2", this.lowPoints.getFloorZ());
        return root;
    }

    public static CuboidArea load(Map<String, Object> root, Level world) throws Exception {
        if (root == null) {
            throw new Exception("Invalid residence physical location...");
        }
        CuboidArea newArea = new CuboidArea();
        int x1 = (Integer) root.get("X1");
        int y1 = (Integer) root.get("Y1");
        int z1 = (Integer) root.get("Z1");
        int x2 = (Integer) root.get("X2");
        int y2 = (Integer) root.get("Y2");
        int z2 = (Integer) root.get("Z2");
        newArea.highPoints = new Position(x1, y1, z1, world);
        newArea.lowPoints = new Position(x2, y2, z2, world);
        return newArea;
    }

    public List<ChunkRef> getChunks() {
        List<ChunkRef> chunks = new ArrayList<>();
        Position high = this.highPoints;
        Position low = this.lowPoints;
        int lowX = ChunkRef.getChunkCoord(low.getFloorX());
        int lowZ = ChunkRef.getChunkCoord(low.getFloorZ());
        int highX = ChunkRef.getChunkCoord(high.getFloorX());
        int highZ = ChunkRef.getChunkCoord(high.getFloorZ());

        for (int x = lowX; x <= highX; x++) {
            for (int z = lowZ; z <= highZ; z++) {
                chunks.add(new ChunkRef(x, z));
            }
        }
        return chunks;
    }
}

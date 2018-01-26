/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.economy;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import com.bekvon.bukkit.residence.Residence;
import com.bekvon.bukkit.residence.permissions.PermissionGroup;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.protection.ClaimedResidence;
import com.bekvon.bukkit.residence.protection.CuboidArea;
import com.bekvon.bukkit.residence.protection.ResidenceManager;

import java.util.*;
import java.util.Map.Entry;

/**
 * @author Administrator
 */
public class TransactionManager {

    ResidenceManager manager;
    private Map<String, Integer> sellAmount;
    PermissionManager gm;

    public static boolean chargeEconomyMoney(Player player, int amount) {
        EconomyInterface econ = Residence.getEconomyManager();
        if (econ == null) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
            return false;
        }
        if (!econ.canAfford(player.getName(), amount)) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NotEnoughMoney"));
            return false;
        }
        econ.subtract(player.getName(), amount);
        player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("MoneyCharged", TextFormat.YELLOW + String.format("%d", amount) + TextFormat.GREEN + "." + TextFormat.YELLOW + econ.getName() + TextFormat.GREEN));
        return true;
    }

    public TransactionManager(ResidenceManager m, PermissionManager g) {
        gm = g;
        manager = m;
        sellAmount = Collections.synchronizedMap(new HashMap<String, Integer>());
    }

    public void putForSale(String areaname, Player player, int amount, boolean resadmin) {
        if (Residence.getConfigManager().enabledRentSystem()) {
            if (Residence.getRentManager().isForRent(areaname)) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("RentSellFail"));
                return;
            }
        }
        if (!resadmin) {
            if (!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager() == null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
                return;
            }
            boolean cansell = Residence.getPermissionManager().getGroup(player).canSellLand() || player.hasPermission("residence.sell");
            if (!cansell && !resadmin) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                return;
            }
            if (amount <= 0) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidAmount"));
                return;
            }
        }
        String pname = player.getName();
        ClaimedResidence area = manager.getByName(areaname);
        if (area == null) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
            return;
        }
        if (!area.getPermissions().getOwner().equals(pname) && !resadmin) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            return;
        }
        if (sellAmount.containsKey(areaname)) {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("AlreadySellFail"));
            return;
        }
        sellAmount.put(areaname, amount);
        player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("ResidenceForSale", TextFormat.YELLOW + areaname + TextFormat.GREEN + "." + TextFormat.YELLOW + amount + TextFormat.GREEN));
    }

    public boolean putForSale(String areaname, int amount) {
        if (Residence.getConfigManager().enabledRentSystem()) {
            if (Residence.getRentManager().isForRent(areaname)) {
                return false;
            }
        }
        ClaimedResidence area = manager.getByName(areaname);
        if (area == null) {
            return false;
        }
        if (sellAmount.containsKey(areaname)) {
            return false;
        }
        sellAmount.put(areaname, amount);
        return true;
    }

    public void buyPlot(String areaname, Player player, boolean resadmin) {
        PermissionGroup group = gm.getGroup(player);
        if (!resadmin) {
            if (!Residence.getConfigManager().enableEconomy() || Residence.getEconomyManager() == null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
                return;
            }
            boolean canbuy = group.canBuyLand() || player.hasPermission("residence.buy");
            if (!canbuy && !resadmin) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
                return;
            }
        }
        if (isForSale(areaname)) {
            ClaimedResidence res = manager.getByName(areaname);
            if (res == null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidArea"));
                sellAmount.remove(areaname);
                return;
            }
            if (res.getPermissions().getOwner().equals(player.getName())) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("OwnerBuyFail"));
                return;
            }
            if (Residence.getResidenceManager().getOwnedZoneCount(player.getName()) >= group.getMaxZones() && !resadmin) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ResidenceTooMany"));
                return;
            }
            Server serv = Residence.getServ();
            int amount = sellAmount.get(areaname);
            if (!resadmin) {
                if (!group.buyLandIgnoreLimits()) {
                    CuboidArea[] areas = res.getAreaArray();
                    for (CuboidArea thisarea : areas) {
                        if (!group.inLimits(thisarea)) {
                            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ResidenceBuyTooBig"));
                            return;
                        }
                    }
                }
            }
            EconomyInterface econ = Residence.getEconomyManager();
            if (econ == null) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("MarketDisabled"));
                return;
            }
            String buyerName = player.getName();
            String sellerName = res.getPermissions().getOwner();
            Player sellerNameFix = Residence.getServ().getPlayer(sellerName);
            if (sellerNameFix != null) {
                sellerName = sellerNameFix.getName();
            }
            if (econ.canAfford(buyerName, amount)) {
                if (!econ.transfer(buyerName, sellerName, amount)) {
                    player.sendMessage(TextFormat.RED + "Error, could not transfer " + amount + " from " + buyerName + " to " + sellerName);
                    return;
                }
                res.getPermissions().setOwner(player.getName(), true);
                res.getPermissions().applyDefaultFlags();
                this.removeFromSale(areaname);
                player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("MoneyCharged", TextFormat.YELLOW + String.format("%d", amount) + TextFormat.GREEN + "." + TextFormat.YELLOW + econ.getName() + TextFormat.GREEN));
                player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("ResidenceBought", TextFormat.GREEN + areaname + TextFormat.YELLOW));
                Player seller = serv.getPlayer(sellerName);
                if (seller != null && seller.isOnline()) {
                    seller.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("ResidenceBuy", TextFormat.YELLOW + player.getName() + TextFormat.GREEN + "." + TextFormat.YELLOW + areaname + TextFormat.GREEN));
                    seller.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("MoneyCredit", TextFormat.YELLOW + String.format("%d", amount) + TextFormat.GREEN + "." + TextFormat.YELLOW + econ.getName() + TextFormat.GREEN));
                }
            } else {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NotEnoughMoney"));
            }
        } else {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidResidence"));
        }
    }

    public void removeFromSale(Player player, String areaname, boolean resadmin) {
        ClaimedResidence area = manager.getByName(areaname);
        if (area != null) {
            if (!isForSale(areaname)) {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("ResidenceNotForSale"));
                return;
            }
            if (area.getPermissions().getOwner().equals(player.getName()) || resadmin) {
                removeFromSale(areaname);
                player.sendMessage(TextFormat.GREEN + Residence.getLanguage().getPhrase("ResidenceStopSelling"));
            } else {
                player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("NoPermission"));
            }
        } else {
            player.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidArea"));
        }
    }

    public void removeFromSale(String areaname) {
        sellAmount.remove(areaname);
    }

    public boolean isForSale(String areaname) {
        return sellAmount.containsKey(areaname);
    }

    public void viewSaleInfo(String areaname, Player player) {
        if (sellAmount.containsKey(areaname)) {
            player.sendMessage("------------------------");
            player.sendMessage(TextFormat.YELLOW + "Name:" + TextFormat.DARK_GREEN + " " + areaname);
            player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("SellAmount") + ":" + TextFormat.RED + " " + sellAmount.get(areaname));
            if (Residence.getConfigManager().useLeases()) {
                Date etime = Residence.getLeaseManager().getExpireTime(areaname);
                if (etime != null) {
                    player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("LeaseExpire") + ":" + TextFormat.GREEN + " " + etime.toString());
                }
            }
            player.sendMessage("------------------------");
        }
    }

    public void printForSaleResidences(Player player) {
        Set<Entry<String, Integer>> set = sellAmount.entrySet();
        player.sendMessage(TextFormat.YELLOW + Residence.getLanguage().getPhrase("LandForSale") + ":");
        StringBuilder sbuild = new StringBuilder();
        sbuild.append(TextFormat.GREEN);
        boolean firstadd = true;
        for (Entry<String, Integer> land : set) {
            if (!firstadd) {
                sbuild.append(", ");
            } else {
                firstadd = false;
            }
            sbuild.append(land.getKey());
        }
        player.sendMessage(sbuild.toString());
    }

    public void clearSales() {
        sellAmount.clear();
        MainLogger.getLogger().info("[Residence] - ReInit land selling.");
    }

    public int getSaleAmount(String name) {
        return sellAmount.get(name);
    }

    public Map<String, Integer> save() {
        return sellAmount;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static TransactionManager load(Map root, PermissionManager p, ResidenceManager r) {
        TransactionManager tman = new TransactionManager(r, p);
        if (root != null) {
            tman.sellAmount = root;
        }
        return tman;
    }
}

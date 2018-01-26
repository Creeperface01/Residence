/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.itemlist;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.utils.ConfigSection;
import cn.nukkit.utils.TextFormat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
public class ItemList {

    protected List<Integer> list;
    protected ListType type;

    public ItemList(ListType listType) {
        this();
        type = listType;
    }

    protected ItemList() {
        list = new ArrayList<>();
    }

    public enum ListType {
        BLACKLIST, WHITELIST, IGNORELIST, OTHER
    }

    public ListType getType() {
        return type;
    }

    public boolean contains(int mat) {
        return list.contains(mat);
    }

    public void add(int mat) {
        if (!list.contains(mat)) {
            list.add(mat);
        }
    }

    public boolean toggle(int mat) {
        if (list.contains(mat)) {
            list.remove(mat);
            return false;
        } else {
            list.add(mat);
            return true;
        }
    }

    public void remove(int mat) {
        list.remove(mat);
    }

    public boolean isAllowed(int mat) {
        if (type == ListType.BLACKLIST) {
            if (list.contains(mat)) {
                return false;
            }
            return true;
        } else if (type == ListType.WHITELIST) {
            if (list.contains(mat)) {
                return true;
            }
            return false;
        }
        return true;
    }

    public boolean isIgnored(int mat) {
        if (type == ListType.IGNORELIST) {
            if (list.contains(mat)) {
                return true;
            }
        }
        return false;
    }

    public boolean isListed(int mat) {
        return this.contains(mat);
    }

    public int getListSize() {
        return list.size();
    }

    public static ItemList readList(ConfigSection node) {
        return ItemList.readList(node, new ItemList());
    }

    protected static ItemList readList(ConfigSection node, ItemList list) {
        ListType type = ListType.valueOf(node.getString("Type", "").toUpperCase());
        list.type = type;
        List<Integer> items = node.getIntegerList("Items");
        if (items != null) {
            for (int item : items) {
                list.add(item);
            }
        }
        return list;
    }

    public void printList(Player player) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (int mat : list) {
            if (!first) {
                builder.append(", ");
            } else {
                builder.append(TextFormat.YELLOW);
            }
            builder.append(getNameFromId(mat));
            first = false;
        }
        player.sendMessage(builder.toString());
    }

    public Item[] toArray() {
        Item mats[] = new Item[list.size()];
        int i = 0;
        for (int mat : list) {
            mats[i] = Item.get(mat);
            i++;
        }
        return mats;
    }

    public Map<String, Object> save() {
        Map saveMap = new LinkedHashMap<String, Object>();
        saveMap.put("Type", type.toString());
        List<Integer> saveList = new ArrayList<>(list);
        saveMap.put("ItemList", saveList);
        return saveMap;
    }

    public static ItemList load(Map<String, Object> map) {
        ItemList newlist = new ItemList();
        return load(map, newlist);
    }

    protected static ItemList load(Map<String, Object> map, ItemList newlist) {
        try {
            newlist.type = ListType.valueOf((String) map.get("Type"));
            List<Integer> list = (List<Integer>) map.get("ItemList");
            for (int item : list) {
                newlist.add(item);
            }
        } catch (Exception ex) {
        }
        return newlist;
    }

    private static String[] nameLookup = new String[Item.list.length];

    public static String getNameFromId(int id) {
        if (id < 0 || id >= nameLookup.length) {
            return null;
        }

        return nameLookup[id];
    }

    static {
        for (int i = 0; i < Item.list.length; i++) {
            nameLookup[i] = Item.get(i).getName();
        }
    }
}

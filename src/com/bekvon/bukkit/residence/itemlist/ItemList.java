/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.itemlist;

import cn.nukkit.Player;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.item.Item;
import cn.nukkit.utils.ConfigSection;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Administrator
 */
public class ItemList {

    protected List<Item> list;
    protected ListType type;

    public ItemList(ListType listType) {
        this();
        type = listType;
    }

    protected ItemList() {
        list = new ArrayList<Item>();
    }

    public static enum ListType {
        BLACKLIST, WHITELIST, IGNORELIST, OTHER
    }

    public ListType getType() {
        return type;
    }

    public boolean contains(Item mat) {
        return list.contains(mat);
    }

    public void add(Item mat) {
        if (!list.contains(mat)) {
            list.add(mat);
        }
    }

    public boolean toggle(Item mat) {
        if (list.contains(mat)) {
            list.remove(mat);
            return false;
        } else {
            list.add(mat);
            return true;
        }
    }

    public void remove(Item mat) {
        list.remove(mat);
    }

    public boolean isAllowed(Item mat) {
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

    public boolean isIgnored(Item mat) {
        if (type == ListType.IGNORELIST) {
            if (list.contains(mat)) {
                return true;
            }
        }
        return false;
    }

    public boolean isListed(Item mat) {
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
        List<String> items = node.getStringList("Items");
        if (items != null) {
            for (String item : items) {
                int parse = -1;
                try {
                    parse = Integer.parseInt(item);
                } catch (Exception ex) {
                }
                if (parse == -1) {
                    try {
                        list.add(Item.valueOf(item.toUpperCase()));
                    } catch (Exception ex) {
                    }
                } else {
                    try {
                        list.add(Item.getMaterial(parse));
                    } catch (Exception ex) {
                    }
                }
            }
        }
        return list;
    }

    public void printList(Player player) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (Item mat : list) {
            if (!first) {
                builder.append(", ");
            } else {
                builder.append(TextFormat.YELLOW);
            }
            builder.append(mat);
            first = false;
        }
        player.sendMessage(builder.toString());
    }

    public Item[] toArray() {
        Item mats[] = new Item[list.size()];
        int i = 0;
        for (Item mat : list) {
            mats[i] = mat;
            i++;
        }
        return mats;
    }

    public Map<String, Object> save() {
        Map saveMap = new LinkedHashMap<String, Object>();
        saveMap.put("Type", type.toString());
        List<String> saveList = new ArrayList<String>();
        for (Item mat : list) {
            saveList.add(mat.toString());
        }
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
            List<String> list = (List<String>) map.get("ItemList");
            for (String item : list) {
                newlist.add(Item.valueOf(item));
            }
        } catch (Exception ex) {
        }
        return newlist;
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence.text.help;

import cn.nukkit.command.CommandSender;
import cn.nukkit.utils.TextFormat;
import com.bekvon.bukkit.residence.Residence;

import java.util.Arrays;
import java.util.List;

/**
 * @author Administrator
 */
public class InformationPager {

    public static int linesPerPage = 7;

    public static int getLinesPerPage() {
        return linesPerPage;
    }

    public static void setLinesPerPage(int lines) {
        linesPerPage = lines;
    }

    public static void printInfo(CommandSender sender, String title, String[] lines, int page) {
        InformationPager.printInfo(sender, title, Arrays.asList(lines), page);
    }

    public static void printInfo(CommandSender sender, String title, List<String> lines, int page) {
        int perPage = 6;
        int start = (page - 1) * perPage;
        int end = start + perPage;
        int pagecount = (int) Math.ceil((double) lines.size() / (double) perPage);
        if (pagecount == 0) {
            pagecount = 1;
        }
        if (page > pagecount) {
            sender.sendMessage(TextFormat.RED + Residence.getLanguage().getPhrase("InvalidPage"));
            return;
        }
        sender.sendMessage(TextFormat.YELLOW + "---<" + TextFormat.GREEN + title + TextFormat.YELLOW + ">---");
        sender.sendMessage(TextFormat.YELLOW + "---<" + Residence.getLanguage().getPhrase("GenericPage", TextFormat.GREEN + String.format("%d", page) + TextFormat.YELLOW + "." + TextFormat.GREEN + pagecount + TextFormat.YELLOW) + ">---");
        for (int i = start; i < end; i++) {
            if (lines.size() > i) {
                sender.sendMessage(TextFormat.GREEN + lines.get(i));
            }
        }
        if (pagecount > page) {
            sender.sendMessage(TextFormat.GRAY + "---<" + Residence.getLanguage().getPhrase("NextPage") + ">---");
        } else {
            sender.sendMessage(TextFormat.GRAY + "-----------------------");
        }
    }
}

package com.bekvon.bukkit.residence.utils;

import cn.nukkit.network.protocol.SetTitlePacket;
import cn.nukkit.Player;

/**
 *
 * @author hamzaxx
 */
public class ActionBar {

    public static void send(Player receivingPacket, String msg) {
        SetTitlePacket packet = new SetTitlePacket();
        packet.title = msg;
        packet.type = SetTitlePacket.TYPE_ACTION_BAR;
        packet.fadeInDuration = 10;
        packet.fadeOutDuration = 5;
        packet.duration = 20;

        receivingPacket.dataPacket(packet);
    }
}

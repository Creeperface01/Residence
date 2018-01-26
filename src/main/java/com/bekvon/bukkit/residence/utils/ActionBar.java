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
        packet.text = msg;
        packet.type = SetTitlePacket.TYPE_ACTION_BAR;
        packet.fadeInTime = 10;
        packet.fadeOutTime = 5;
        packet.stayTime = 20;

        receivingPacket.dataPacket(packet);
    }
}

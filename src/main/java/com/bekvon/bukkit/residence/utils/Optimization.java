package com.bekvon.bukkit.residence.utils;

import cn.nukkit.plugin.Plugin;
import cn.nukkit.utils.Config;

import java.io.File;

/**
 * @author CreeperFace
 */
public class Optimization {

    public static Config faweConfig;

    public static Config getBasicFAWEConfig(Plugin plugin) {
        if (faweConfig == null) {
            faweConfig = new Config(new File(plugin.getDataFolder(), "config-basic.yml"), Config.YAML);
        }

        return faweConfig;
    }
}

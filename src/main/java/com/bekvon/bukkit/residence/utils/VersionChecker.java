package com.bekvon.bukkit.residence.utils;

import cn.nukkit.Player;
import com.bekvon.bukkit.residence.Residence;

public class VersionChecker {

    Residence plugin;

    public VersionChecker(Residence plugin) {
        this.plugin = plugin;
    }

    public void VersionCheck(final Player player) {
        if (!Residence.getConfigManager().versionCheck()) {
            return;
        }

        /*Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            public void run() {
                String readURL = "https://raw.githubusercontent.com/bekvon/Residence/master/src/plugin.yml";
                FileConfiguration config;
                String currentVersion = plugin.getDescription().getVersion();
                try {
                    URL url = new URL(readURL);
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            url.openStream()));
                    config = YamlConfiguration.loadConfiguration(br);
                    String newVersion = config.getString("version");
                    br.close();
                    if (!newVersion.equals(currentVersion)) {
                        String msg = TextFormat.GREEN + "Residence v" + newVersion + " is now available!\n"
                                + "Your version: " + currentVersion + "\n"
                                + "You can download new version from " + TextFormat.BLUE + plugin.getDescription().getWebsite();
                        if (player != null) {
                            player.sendMessage(msg);
                        } else {
                            plugin.consoleMessage(msg);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });*/
    }

}

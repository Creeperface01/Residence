/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.bekvon.bukkit.residence;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Position;
import cn.nukkit.plugin.Plugin;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.plugin.PluginManager;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.MainLogger;
import com.bekvon.bukkit.residence.chat.ChatManager;
import com.bekvon.bukkit.residence.economy.EconomyAPIAdapter;
import com.bekvon.bukkit.residence.economy.EconomyInterface;
import com.bekvon.bukkit.residence.economy.LlamaEconomyAdapter;
import com.bekvon.bukkit.residence.economy.TransactionManager;
import com.bekvon.bukkit.residence.economy.rent.RentManager;
import com.bekvon.bukkit.residence.itemlist.WorldItemManager;
import com.bekvon.bukkit.residence.listeners.ResidenceBlockListener;
import com.bekvon.bukkit.residence.listeners.ResidenceEntityListener;
import com.bekvon.bukkit.residence.listeners.ResidencePlayerListener;
import com.bekvon.bukkit.residence.permissions.PermissionManager;
import com.bekvon.bukkit.residence.persistance.YMLSaveHelper;
import com.bekvon.bukkit.residence.protection.*;
import com.bekvon.bukkit.residence.selection.SelectionManager;
import com.bekvon.bukkit.residence.selection.WorldEditSelectionManager;
import com.bekvon.bukkit.residence.text.Language;
import com.bekvon.bukkit.residence.text.help.HelpEntry;
import com.bekvon.bukkit.residence.text.help.InformationPager;
import com.bekvon.bukkit.residence.utils.VersionChecker;
import com.residence.zip.ZipLibrary;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Gary Smoak - bekvon
 */
public class Residence extends PluginBase {

    protected static ResidenceManager rmanager;
    protected static SelectionManager smanager;
    protected static PermissionManager gmanager;
    protected static ConfigManager cmanager;
    protected static ResidenceBlockListener blistener;
    protected static ResidencePlayerListener plistener;
    protected static ResidenceEntityListener elistener;
    protected static TransactionManager tmanager;
    protected static PermissionListManager pmanager;
    protected static LeaseManager leasemanager;
    protected static WorldItemManager imanager;
    protected static WorldFlagManager wmanager;
    protected static RentManager rentmanager;
    protected static ChatManager chatmanager;
    protected static Server server;
    protected static HelpEntry helppages;
    protected static Language language;
    protected boolean firstenable = true;
    protected static EconomyInterface economy;
    public final static int saveVersion = 1;
    protected static File dataFolder;
    protected static int leaseBukkitId = -1;
    protected static int rentBukkitId = -1;
    protected static int healBukkitId = -1;
    protected static int autosaveBukkitId = -1;
    protected static VersionChecker versionChecker;
    protected static boolean initsuccess = false;
    protected Map<String, String> deleteConfirm;
    protected static List<String> resadminToggle;
    private final static String[] validLanguages = {"English", "German", "French", "Hungarian", "Spanish", "Chinese", "Czech", "Brazilian", "Polish"};
    private Runnable doHeals = new Runnable() {
        public void run() {
            plistener.doHeals();
        }
    };
    private Runnable rentExpire = new Runnable() {
        public void run() {
            rentmanager.checkCurrentRents();
            if (cmanager.showIntervalMessages()) {
                MainLogger.getLogger().info("[Residence] - Rent Expirations checked!");
            }
        }
    };
    private Runnable leaseExpire = new Runnable() {
        public void run() {
            leasemanager.doExpirations();
            if (cmanager.showIntervalMessages()) {
                MainLogger.getLogger().info("[Residence] - Lease Expirations checked!");
            }
        }
    };
    private Runnable autoSave = new Runnable() {
        public void run() {
            try {
                if (initsuccess) {
                    saveYml();
                }
            } catch (Exception ex) {
                MainLogger.getLogger().logException(ex);
            }
        }
    };

    public Residence() {
    }

    public void reloadPlugin() {
        this.onDisable();
        this.reloadConfig();
        this.onEnable();

    }

    @Override
    public void onDisable() {
        server.getScheduler().cancelTask(autosaveBukkitId);
        server.getScheduler().cancelTask(healBukkitId);
        if (cmanager.useLeases()) {
            server.getScheduler().cancelTask(leaseBukkitId);
        }
        if (cmanager.enabledRentSystem()) {
            server.getScheduler().cancelTask(rentBukkitId);
        }
        if (initsuccess) {
            try {
                saveYml();
                ZipLibrary.backup();
            } catch (Exception ex) {
                MainLogger.getLogger().logException(ex);
            }
            MainLogger.getLogger().info("[Residence] Disabled!");
        }
    }

    @Override
    public void onEnable() {
        try {
            initsuccess = false;
            deleteConfirm = new HashMap<String, String>();
            resadminToggle = new ArrayList<String>();
            server = this.getServer();
            dataFolder = this.getDataFolder();
            if (!dataFolder.isDirectory()) {
                dataFolder.mkdirs();
            }

            saveDefaultConfig();

            cmanager = new ConfigManager(this.getConfig());
            String multiworld = cmanager.getMultiworldPlugin();
            if (multiworld != null) {
                Plugin plugin = server.getPluginManager().getPlugin(multiworld);
                if (plugin != null) {
                    if (!plugin.isEnabled()) {
                        this.getLogger().notice("[Residence] - Enabling multiworld plugin: " + multiworld);
                        server.getPluginManager().enablePlugin(plugin);
                    }
                }
            }
            gmanager = new PermissionManager(this.getConfig());
            imanager = new WorldItemManager(this.getConfig());
            wmanager = new WorldFlagManager(this.getConfig());
            chatmanager = new ChatManager();
            rentmanager = new RentManager();
            for (String lang : validLanguages) {
                try {
                    if (this.checkNewLanguageVersion(lang)) {
                        this.writeDefaultLanguageFile(lang);
                    }
                } catch (Exception ex) {
                    this.getLogger().error("[Residence] Failed to update language file: " + lang + ".yml");
                    helppages = new HelpEntry("");
                    language = new Language();
                }
            }
            try {
                File langFile = new File(new File(dataFolder, "Language"), cmanager.getLanguage() + ".yml");
                if (langFile.isFile()) {
                    Config langconfig = new Config(langFile);
                    helppages = HelpEntry.parseHelp(langconfig, "CommandHelp");
                    HelpEntry.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
                    InformationPager.setLinesPerPage(langconfig.getInt("HelpLinesPerPage", 7));
                    language = Language.parseText(langconfig, "Language");
                } else {
                    this.getLogger().error("[Residence] Language file does not exist...");
                }
            } catch (Exception ex) {
                this.getLogger().error("[Residence] Failed to load language file: " + cmanager.getLanguage() + ".yml, Error: " + ex.getMessage());
                MainLogger.getLogger().logException(ex);
                helppages = new HelpEntry("");
                language = new Language();
            }
            economy = null;
            if (this.getConfig().getBoolean("Global.EnableEconomy", false)) {
                MainLogger.getLogger().info("[Residence] Scanning for economy systems...");

                if (economy == null) {
                    this.loadLlamaEconomy();
                }

                if (economy == null) {
                    this.loadEconomyApi();
                }

                if (economy == null) {
                    this.getLogger().warning("[Residence] Unable to find an economy system...");
                }
            }
            try {
                this.loadYml();
            } catch (Exception e) {
                this.getLogger().alert("Unable to load save file", e);
                throw e;
            }
            if (rmanager == null) {
                rmanager = new ResidenceManager();
            }
            if (leasemanager == null) {
                leasemanager = new LeaseManager(rmanager);
            }
            if (tmanager == null) {
                tmanager = new TransactionManager(rmanager, gmanager);
            }
            if (pmanager == null) {
                pmanager = new PermissionListManager();
            }
            if (firstenable) {
                if (!this.isEnabled()) {
                    return;
                }
                FlagPermissions.initValidFlags();
                Plugin p = server.getPluginManager().getPlugin("FastAsyncWorldEdit");
                if (p != null) {
                    smanager = new WorldEditSelectionManager(server);
                    MainLogger.getLogger().notice("[Residence] Found WorldEdit");
                } else {
                    smanager = new SelectionManager(server);
                    MainLogger.getLogger().notice("[Residence] WorldEdit NOT found!");
                }

                blistener = new ResidenceBlockListener();
                plistener = new ResidencePlayerListener();
                elistener = new ResidenceEntityListener();
                PluginManager pm = getServer().getPluginManager();
                pm.registerEvents(blistener, this);
                pm.registerEvents(plistener, this);
                pm.registerEvents(elistener, this);

                // pm.registerEvent(Event.Type.WORLD_LOAD, wlistener,
                // Priority.NORMAL, this);
                /*if (cmanager.enableSpout()) {
                    slistener = new ResidenceSpoutListener();
                    pm.registerEvents(slistener, this);
                }*/
                firstenable = false;
            } else {
                plistener.reload();
            }
            int autosaveInt = cmanager.getAutoSaveInterval();
            if (autosaveInt < 1) {
                autosaveInt = 1;
            }
            autosaveInt = autosaveInt * 60 * 20;
            autosaveBukkitId = server.getScheduler().scheduleDelayedRepeatingTask(autoSave, autosaveInt, autosaveInt).getTaskId();
            healBukkitId = server.getScheduler().scheduleDelayedRepeatingTask(doHeals, 20, 20).getTaskId();
            if (cmanager.useLeases()) {
                int leaseInterval = cmanager.getLeaseCheckInterval();
                if (leaseInterval < 1) {
                    leaseInterval = 1;
                }
                leaseInterval = leaseInterval * 60 * 20;
                leaseBukkitId = server.getScheduler().scheduleDelayedRepeatingTask(leaseExpire, leaseInterval, leaseInterval).getTaskId();
            }
            if (cmanager.enabledRentSystem()) {
                int rentint = cmanager.getRentCheckInterval();
                if (rentint < 1) {
                    rentint = 1;
                }
                rentint = rentint * 60 * 20;
                rentBukkitId = server.getScheduler().scheduleDelayedRepeatingTask(rentExpire, rentint, rentint).getTaskId();
            }
            for (Player player : getServer().getOnlinePlayers().values()) {
                if (Residence.getPermissionManager().isResidenceAdmin(player)) {
                    turnResAdminOn(player);
                }
            }
            /*try {
                Metrics metrics = new Metrics(this);
                metrics.start();
            } catch (IOException e) {
                // Failed to submit the stats :-(
            }*/
            MainLogger.getLogger().notice("[Residence] Enabled! Version " + this.getDescription().getVersion() + " by bekvon");
            initsuccess = true;
        } catch (Exception ex) {
            initsuccess = false;
            getServer().getPluginManager().disablePlugin(this);
            this.getLogger().error("[Residence] - FAILED INITIALIZATION! DISABLED! ERROR:");
            MainLogger.getLogger().logException(ex);
        }
        versionChecker = new VersionChecker(this);
        versionChecker.VersionCheck(null);
    }

    public void consoleMessage(String message) {
        getLogger().info(message);
    }

    public static boolean validName(String name) {
        if (name.contains(":") || name.contains(".")) {
            return false;
        }
        if (cmanager.getResidenceNameRegex() == null) {
            return true;
        } else {
            String namecheck = name.replaceAll(cmanager.getResidenceNameRegex(), "");
            if (!name.equals(namecheck)) {
                return false;
            }
            return true;
        }
    }

    public static VersionChecker getVersionChecker() {
        return versionChecker;
    }

    public static File getDataLocation() {
        return dataFolder;
    }

    public static ResidenceManager getResidenceManager() {
        return rmanager;
    }

    public static SelectionManager getSelectionManager() {
        return smanager;
    }

    public static PermissionManager getPermissionManager() {
        return gmanager;
    }

    public static EconomyInterface getEconomyManager() {
        return economy;
    }

    public static Server getServ() {
        return server;
    }

    public static LeaseManager getLeaseManager() {
        return leasemanager;
    }

    public static ConfigManager getConfigManager() {
        return cmanager;
    }

    public static TransactionManager getTransactionManager() {
        return tmanager;
    }

    public static WorldItemManager getItemManager() {
        return imanager;
    }

    public static WorldFlagManager getWorldFlags() {
        return wmanager;
    }

    public static RentManager getRentManager() {
        return rentmanager;
    }

    public static ResidencePlayerListener getPlayerListener() {
        return plistener;
    }

    public static ResidenceBlockListener getBlockListener() {
        return blistener;
    }

    public static ResidenceEntityListener getEntityListener() {
        return elistener;
    }

    public static ChatManager getChatManager() {
        return chatmanager;
    }

    public static Language getLanguage() {
        if (language == null) {
            language = new Language();
        }
        return language;
    }

    public static FlagPermissions getPermsByLoc(Position loc) {
        ClaimedResidence res = rmanager.getByLoc(loc);
        if (res != null) {
            return res.getPermissions();
        } else {
            return wmanager.getPerms(loc.getLevel().getName());
        }
    }

    public static FlagPermissions getPermsByLocForPlayer(Position loc, Player player) {
        ClaimedResidence res = rmanager.getByLoc(loc);
        if (res != null) {
            return res.getPermissions();
        } else {
            if (player != null) {
                return wmanager.getPerms(player);
            } else {
                return wmanager.getPerms(loc.getLevel().getName());
            }
        }
    }

    private void loadLlamaEconomy() {
        Plugin p = getServer().getPluginManager().getPlugin("LlamaEconomy");
        if (p != null) {
            economy = new LlamaEconomyAdapter();
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with LlamaEconomy!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] LlamaEconomy NOT found!");
        }
    }

    private void loadEconomyApi() {
        Plugin p = getServer().getPluginManager().getPlugin("EconomyAPI");
        if (p != null) {
            economy = new EconomyAPIAdapter();
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] Successfully linked with EconomyAPI!");
        } else {
            Logger.getLogger("Minecraft").log(Level.INFO, "[Residence] EconomyAPI NOT found!");
        }
    }

    public static boolean isResAdminOn(Player player) {
        return resadminToggle.contains(player.getName());
    }

    public static void turnResAdminOn(Player player) {
        resadminToggle.add(player.getName());
    }

    public static boolean isResAdminOn(String player) {
        return resadminToggle.contains(player.toLowerCase());
    }

    private void saveYml() throws IOException {
        File saveFolder = new File(dataFolder, "Save");
        File worldFolder = new File(saveFolder, "Worlds");
        worldFolder.mkdirs();
        YMLSaveHelper yml;
        Map<String, Object> save = rmanager.save();
        for (Entry<String, Object> entry : save.entrySet()) {
            File ymlSaveLoc = new File(worldFolder, "res_" + entry.getKey() + ".yml");
            File tmpFile = new File(worldFolder, "tmp_res_" + entry.getKey() + ".yml");
            yml = new YMLSaveHelper(tmpFile);
            yml.getRoot().put("Version", saveVersion);
            cn.nukkit.level.Level world = server.getLevelByName(entry.getKey());
            if (world != null) {
                yml.getRoot().put("Seed", world.getSeed());
            }
            yml.getRoot().put("Residences", (Map) entry.getValue());
            yml.save();
            if (ymlSaveLoc.isFile()) {
                File backupFolder = new File(worldFolder, "Backup");
                backupFolder.mkdirs();
                File backupFile = new File(backupFolder, "res_" + entry.getKey() + ".yml");
                if (backupFile.isFile()) {
                    backupFile.delete();
                }
                ymlSaveLoc.renameTo(backupFile);
            }
            tmpFile.renameTo(ymlSaveLoc);
        }

        // For Sale save
        File ymlSaveLoc = new File(saveFolder, "forsale.yml");
        File tmpFile = new File(saveFolder, "tmp_forsale.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.save();
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("Economy", tmanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "forsale.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        // Leases save
        ymlSaveLoc = new File(saveFolder, "leases.yml");
        tmpFile = new File(saveFolder, "tmp_leases.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("Leases", leasemanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "leases.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        // permlist save
        ymlSaveLoc = new File(saveFolder, "permlists.yml");
        tmpFile = new File(saveFolder, "tmp_permlists.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("PermissionLists", pmanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "permlists.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        // rent save
        ymlSaveLoc = new File(saveFolder, "rent.yml");
        tmpFile = new File(saveFolder, "tmp_rent.yml");
        yml = new YMLSaveHelper(tmpFile);
        yml.getRoot().put("Version", saveVersion);
        yml.getRoot().put("RentSystem", rentmanager.save());
        yml.save();
        if (ymlSaveLoc.isFile()) {
            File backupFolder = new File(saveFolder, "Backup");
            backupFolder.mkdirs();
            File backupFile = new File(backupFolder, "rent.yml");
            if (backupFile.isFile()) {
                backupFile.delete();
            }
            ymlSaveLoc.renameTo(backupFile);
        }
        tmpFile.renameTo(ymlSaveLoc);

        if (cmanager.showIntervalMessages()) {
            MainLogger.getLogger().info("[Residence] - Saved Residences...");
        }
    }

    protected boolean loadYml() throws Exception {
        File saveFolder = new File(dataFolder, "Save");
        saveFolder.mkdirs();

        try {
            File worldFolder = new File(saveFolder, "Worlds");
            if (!saveFolder.isDirectory()) {
                this.getLogger().warning("Save directory does not exist...");
                this.getLogger().warning("Please restart server");
                return true;
            }
            long time;
            YMLSaveHelper yml;
            File loadFile;
            HashMap<String, Object> worlds = new HashMap<>();
            for (cn.nukkit.level.Level world : getServ().getLevels().values()) {
                loadFile = new File(worldFolder, "res_" + world.getName() + ".yml");
                if (loadFile.isFile()) {
                    time = System.currentTimeMillis();
                    this.getLogger().info("Loading save data for world " + world.getName() + "...");
                    yml = new YMLSaveHelper(loadFile);
                    yml.load();
                    worlds.put(world.getName(), yml.getRoot().get("Residences"));
                    this.getLogger().info("Save data for world " + world.getName() + " loaded. (" + ((float) (System.currentTimeMillis() - time) / 1000) + " secs)");
                }
            }
            rmanager = ResidenceManager.load(worlds);
            loadFile = new File(saveFolder, "forsale.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                tmanager = TransactionManager.load((Map) yml.getRoot().get("Economy"), gmanager, rmanager);
            }
            loadFile = new File(saveFolder, "leases.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                leasemanager = LeaseManager.load((Map) yml.getRoot().get("Leases"), rmanager);
            }
            loadFile = new File(saveFolder, "permlists.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                pmanager = PermissionListManager.load((Map) yml.getRoot().get("PermissionLists"));
            }
            loadFile = new File(saveFolder, "rent.yml");
            if (loadFile.isFile()) {
                yml = new YMLSaveHelper(loadFile);
                yml.load();
                rentmanager = RentManager.load((Map) yml.getRoot().get("RentSystem"));
            }
            // System.out.print("[Residence] Loaded...");
            return true;
        } catch (Exception ex) {
            MainLogger.getLogger().logException(ex);
            throw ex;
        }
    }

    private void writeDefaultConfigFromJar() {
        if (this.writeDefaultFileFromJar(new File(this.getDataFolder(), "resources/config.yml"), "resources/config.yml", true)) {
            MainLogger.getLogger().info("[Residence] Wrote default config...");
        }
    }

    private void writeDefaultLanguageFile(String lang) {
        //this.saveResource("languagefiles/"+lang+".yml", true);
        File outFile = new File(new File(this.getDataFolder(), "Language"), lang + ".yml");
        outFile.getParentFile().mkdirs();
        if (this.writeDefaultFileFromJar(outFile, "languagefiles/" + lang + ".yml", true)) {
            MainLogger.getLogger().notice("[Residence] Wrote default " + lang + " Language file...");
        }
    }

    private boolean checkNewLanguageVersion(String lang) throws IOException, FileNotFoundException {
        File outFile = new File(new File(this.getDataFolder(), "Language"), lang + ".yml");
        File checkFile = new File(new File(this.getDataFolder(), "Language"), "temp-" + lang + ".yml");
        if (outFile.isFile()) {
            Config testconfig = new Config();
            testconfig.load(new FileInputStream(outFile));
            int oldversion = testconfig.getInt("FieldsVersion", 0);
            if (!this.writeDefaultFileFromJar(checkFile, "languagefiles/" + lang + ".yml", false)) {
                return false;
            }
            Config testconfig2 = new Config();
            testconfig2.load(new FileInputStream(checkFile));
            int newversion = testconfig2.getInt("FieldsVersion", oldversion);
            if (checkFile.isFile()) {
                checkFile.delete();
            }
            if (newversion > oldversion) {
                return true;
            }
            return false;
        }
        return true;
    }

    private boolean writeDefaultFileFromJar(File writeName, String jarPath, boolean backupOld) {
        try {
            File fileBackup = new File(this.getDataFolder(), "backup-" + writeName);
            File jarloc = new File(getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).getCanonicalFile();
            if (jarloc.isFile()) {
                JarFile jar = new JarFile(jarloc);
                JarEntry entry = jar.getJarEntry(jarPath);
                if (entry != null && !entry.isDirectory()) {
                    InputStream in = jar.getInputStream(entry);
                    InputStreamReader isr = new InputStreamReader(in, "UTF8");
                    if (writeName.isFile()) {
                        if (backupOld) {
                            if (fileBackup.isFile()) {
                                fileBackup.delete();
                            }
                            writeName.renameTo(fileBackup);
                        } else {
                            writeName.delete();
                        }
                    }
                    FileOutputStream out = new FileOutputStream(writeName);
                    OutputStreamWriter osw = new OutputStreamWriter(out, "UTF8");
                    char[] tempbytes = new char[512];
                    int readbytes = isr.read(tempbytes, 0, 512);
                    while (readbytes > -1) {
                        osw.write(tempbytes, 0, readbytes);
                        readbytes = isr.read(tempbytes, 0, 512);
                    }
                    osw.close();
                    isr.close();
                    return true;
                }
            }
            return false;
        } catch (Exception ex) {
            MainLogger.getLogger().error("[Residence] Failed to write file: " + writeName);
            return false;
        }
    }

    /*public static UUID getPlayerUUID(String playername)
    {
        Player p = Residence.getServ().getPlayer(playername);
        if(p == null)
        {
            OfflinePlayer pl = Residence.getServ().getOfflinePlayer(playername);
            return pl.get
        }
        else
            return p.getUniqueId();
        return null;
    }

    public static String getPlayerName(UUID uuid)
    {
        OfflinePlayer p = Residence.getServ().getPlayer(uuid);
        if(p==null)
            p = Residence.getServ().getOfflinePlayer(uuid);
        if(p!=null)
            return p.getName();
        else
            return null;
    }*/
}

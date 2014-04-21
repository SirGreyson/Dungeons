package net.shadowraze.dungeons.utils;

import net.shadowraze.dungeons.Dungeons;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Configuration {

    private static Dungeons plugin;
    private static Map<String, YamlConfiguration> loadedConfigs = new TreeMap<String, YamlConfiguration>(String.CASE_INSENSITIVE_ORDER);

    public static List<String> LOBBY_SIGN_FORMAT;
    public static Location SPAWN_LOCATION;

    public static int DEFAULT_MAX_PLAYERS;

    public static int DEFUALT_SPAWNRATE;
    public static int DEFAULT_SPAWNED;
    public static int DEFAULT_SPAWNED_TOTAL;

    public static int LOBBY_START_DELAY;
    public static int NEXT_STAGE_DELAY;
    public static int RESPAWN_DELAY;

    public static void loadConfigurations(Dungeons instance) {
        plugin = instance;
        loadConfig("dungeons");
        loadConfig("lobbies");
        loadConfigVars();
    }

    private static void loadConfigVars() {
        loadConfig("config");
        LOBBY_SIGN_FORMAT = plugin.getConfig().getStringList("lobbySign");
        SPAWN_LOCATION = StringsUtil.parseLocString(plugin.getConfig().getString("spawnLocation"));
        DEFAULT_MAX_PLAYERS = plugin.getConfig().getInt("defaultMaxPlayers");
        DEFUALT_SPAWNRATE = plugin.getConfig().getInt("defaultSpawnRate");
        DEFAULT_SPAWNED = plugin.getConfig().getInt("defaultSpawnedMax");
        DEFAULT_SPAWNED_TOTAL = plugin.getConfig().getInt("defaultSpawnedTotal");
        LOBBY_START_DELAY = plugin.getConfig().getInt("lobbyStartDelay");
        NEXT_STAGE_DELAY = plugin.getConfig().getInt("nextStageDelay");
        RESPAWN_DELAY = plugin.getConfig().getInt("respawnDelay");
    }

    public static void saveConfigurations() {
        saveConfig("dungeons");
        saveConfig("lobbies");
    }

    public static boolean isConfigLoaded(String fileName) { return loadedConfigs.containsKey(fileName); }

    public static YamlConfiguration getConfig(String fileName) {
        return loadedConfigs.get(fileName);
    }

    public static void loadConfig(String fileName) {
        File configF = new File(plugin.getDataFolder(), fileName + ".yml");
        if(!configF.exists()) {
            if(plugin.getResource(fileName + ".yml") != null) plugin.saveResource(fileName + ".yml", false);
            else {
                try {
                    configF.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().severe("Error! Could not create custom configuration: " + fileName);
                    e.printStackTrace();
                }
            }
        }
        if(!isConfigLoaded(fileName)) loadedConfigs.put(fileName, YamlConfiguration.loadConfiguration(configF));
    }

    public static void reloadConfig(String fileName) {
        File configF = new File(plugin.getDataFolder(), fileName + ".yml");
        if(!isConfigLoaded(fileName)) plugin.getLogger().severe("Error! Tried to reload non-existent config file: " + fileName);
        else try {
            loadedConfigs.get(fileName).load(configF);
        } catch (IOException e) {
            plugin.getLogger().severe("Error! Could not reload custom configuration: " + fileName);
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().severe("Error! Could not reload custom configuration: " + fileName);
            e.printStackTrace();
        }
    }

    public static void saveConfig(String fileName) {
        File configF = new File(plugin.getDataFolder(), fileName + ".yml");
        if(!isConfigLoaded(fileName)) plugin.getLogger().severe("Error! Tried to save non-existent config file: " + fileName);
        else try {
            loadedConfigs.get(fileName).save(configF);
        } catch (IOException e) {
            plugin.getLogger().severe("Error! Could not save custom configuration: " + fileName);
            e.printStackTrace();
        }
    }
}

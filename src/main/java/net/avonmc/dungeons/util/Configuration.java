/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.util;

import net.avonmc.dungeons.Dungeons;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class Configuration {

    private Dungeons plugin;
    private static Map<String, YamlConfiguration> loadedConfigs = new TreeMap<String, YamlConfiguration>(String.CASE_INSENSITIVE_ORDER);

    public Configuration(Dungeons plugin) {
        this.plugin = plugin;
    }

    public void loadConfigurations() {
        loadConfig("config");
        loadConfig("dungeonMobs");
        loadConfig("dungeons");
        loadConfig("lobbies");
        new Settings().loadSettings();
    }

    public void saveConfigurations() {
        saveConfig("dungeons");
        saveConfig("lobbies");
    }

    public boolean isConfigLoaded(String fileName) {
        return loadedConfigs.containsKey(fileName);
    }

    public static YamlConfiguration getConfig(String fileName) {
        return loadedConfigs.get(fileName);
    }

    public void loadConfig(String fileName) {
        File configF = new File(plugin.getDataFolder(), fileName + ".yml");
        if (!configF.exists()) {
            if (plugin.getResource(fileName + ".yml") != null) plugin.saveResource(fileName + ".yml", false);
            else {
                try {
                    configF.createNewFile();
                } catch (IOException e) {
                    Messaging.printErr("Error! Could not create custom configuration: " + fileName);
                    e.printStackTrace();
                }
            }
        }
        if (!isConfigLoaded(fileName)) loadedConfigs.put(fileName, YamlConfiguration.loadConfiguration(configF));
    }

    public void reloadConfig(String fileName) {
        File configF = new File(plugin.getDataFolder(), fileName + ".yml");
        if (!isConfigLoaded(fileName))
            Messaging.printErr("Error! Tried to reload non-existent config file: " + fileName);
        else try {
            loadedConfigs.get(fileName).load(configF);
        } catch (IOException e) {
            Messaging.printErr("Error! Could not reload custom configuration: " + fileName);
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            Messaging.printErr("Error! Could not reload custom configuration: " + fileName);
            e.printStackTrace();
        }
    }

    public void saveConfig(String fileName) {
        File configF = new File(plugin.getDataFolder(), fileName + ".yml");
        if (!isConfigLoaded(fileName))
            Messaging.printErr("Error! Tried to save non-existent config file: " + fileName);
        else try {
            loadedConfigs.get(fileName).save(configF);
        } catch (IOException e) {
            Messaging.printErr("Error! Could not save custom configuration: " + fileName);
            e.printStackTrace();
        }
    }
}

/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.dungeon;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.selections.Selection;
import net.avonmc.dungeons.Dungeons;
import net.avonmc.dungeons.util.Configuration;
import net.avonmc.dungeons.util.Messaging;
import net.avonmc.dungeons.util.Settings;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

public class DungeonHandler {

    private static YamlConfiguration dungeonC = Configuration.getConfig("dungeons");

    private static Map<String, Dungeon> loadedDungeons = new TreeMap<String, Dungeon>(String.CASE_INSENSITIVE_ORDER);

    public static void loadDungeons() {
        for(String dungeonID : dungeonC.getKeys(false)) loadDungeon(dungeonID);
        Messaging.printInfo("Dungeons successfully loaded!");
    }

    private static void loadDungeon(String dungeonID) {
        loadedDungeons.put(dungeonID, Dungeon.deserialize(dungeonC.getConfigurationSection(dungeonID)));
        Dungeon dungeon = loadedDungeons.get(dungeonID);
        ConfigurationSection stageS = dungeonC.getConfigurationSection(dungeonID + ".stages");
        for(int i = 0; i < stageS.getKeys(false).size(); i++) dungeon.addStage(Stage.deserialize(dungeon, stageS.getConfigurationSection(String.valueOf(i))));
        if(dungeon.getLoadedStages().size() == 0) Messaging.printErr("Error! " + dungeonID + " has no Stages loaded! Errors may occur!");
    }

    public static void saveDungeons() {
        for(String dungeonID : loadedDungeons.keySet()) dungeonC.set(dungeonID, loadedDungeons.get(dungeonID).serialize());
        Messaging.printInfo("Dungeons successfully saved!");
    }

    public static String getDungeonID(Dungeon dungeon) {
        for(String dungeonID : loadedDungeons.keySet())
            if(loadedDungeons.get(dungeonID) == dungeon) return dungeonID;
        return null;
    }

    public static Map<String, Dungeon> getDungeons() {
        return loadedDungeons;
    }

    public static boolean dungeonExists(String dungeonID) {
        return loadedDungeons.containsKey(dungeonID);
    }

    public static Dungeon getDungeon(String dungeonID) {
        return loadedDungeons.get(dungeonID);
    }

    public static Dungeon getRandomDungeon(boolean isVIP) {
        for(Dungeon dungeon : loadedDungeons.values())
            if(dungeon.getActiveLobby() != null) continue;
            else if(isVIP != dungeon.isVIP()) continue;
            else return dungeon;
        return null;
    }

    public static void createDungeon(String dungeonID, String displayName, boolean isVIP) {
        loadedDungeons.put(dungeonID, new Dungeon(displayName, isVIP, new ArrayList<Stage>()));
    }

    public static void removeDungeon(String dungeonID) {
        dungeonC.set(dungeonID, null);
        loadedDungeons.remove(dungeonID);
    }

    public static Dungeon dungeonFromMobSpawn(Location mobSpawn) {
        for(Dungeon dungeon : loadedDungeons.values())
            if(dungeon.stageFromMobSpawn(mobSpawn) != null) return dungeon;
        return null;
    }

    public static void addMobSpawns(Player sender, Stage stage) {
        Selection sel = Dungeons.getWorldEdit().getSelection(sender);
        if (sel == null) { Messaging.send(sender, "&cYou have not made a WorldEdit selection!"); return; }
        Vector min = sel.getNativeMinimumPoint();
        Vector max = sel.getNativeMaximumPoint();
        for (int x = min.getBlockX(); x < max.getBlockX(); x++)
            for (int y = min.getBlockY(); y < max.getBlockY(); y++)
                for (int z = min.getBlockZ(); z < max.getBlockZ(); z++) {
                    Block block = sel.getWorld().getBlockAt(x, y, z);
                    if (block.getType() != Settings.MOB_SPAWN_MARKER) continue;
                    stage.addMobSpawn(block.getLocation());
                    Messaging.send(sender, "&aSuccessfully added mob spawn at &b" + StringUtil.parseLoc(block.getLocation()));
                }
    }
}

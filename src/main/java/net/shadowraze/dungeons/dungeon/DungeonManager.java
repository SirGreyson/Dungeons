package net.shadowraze.dungeons.dungeon;

import net.shadowraze.dungeons.utils.Configuration;
import net.shadowraze.dungeons.utils.Messaging;
import net.shadowraze.dungeons.utils.StringsUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class DungeonManager {

    private static Map<String, Dungeon> loadedDungeons = new TreeMap<String, Dungeon>(String.CASE_INSENSITIVE_ORDER);
    private static Map<String, DungeonMob> dungeonMobs = new TreeMap<String, DungeonMob>(String.CASE_INSENSITIVE_ORDER);

    public static void loadDungeons() {
        loadDungeonMobs();
        YamlConfiguration dungeonC = Configuration.getConfig("dungeons");
        if(dungeonC.getConfigurationSection("dungeons") == null) return;
        for(String dungeonID : dungeonC.getConfigurationSection("dungeons").getKeys(false)) {
            loadedDungeons.put(dungeonID, new Dungeon(dungeonID));
            loadStages(loadedDungeons.get(dungeonID));
        }
    }

    public static void loadDungeonMobs() {
        ConfigurationSection mobS = Configuration.getConfig("dungeons").getConfigurationSection("dungeonMobs");
        if(mobS == null) return;
        for(String mobID : mobS.getKeys(false))
            dungeonMobs.put(mobID, new DungeonMob(mobID, EntityType.valueOf(mobS.getString(mobID + ".mobType")), mobS.getString(mobID + ".displayName"), mobS.getDouble(mobID + ".health"),
                    mobS.getString(mobID + ".inHand").equalsIgnoreCase("none") ? null : new ItemStack(Material.valueOf(mobS.getString(mobID + ".inHand")), 1), mobS.getInt(mobID + ".abilityTargetRange"),
                    mobS.getInt(mobID + ".abilityInterval"), mobS.getStringList(mobID + ".abilities"), StringsUtil.parsePotionEffectStrings(mobS.getStringList(mobID + ".potionEffects"))));
    }

    public static void loadStages(Dungeon dungeon) {
        ConfigurationSection stageS = Configuration.getConfig("dungeons").getConfigurationSection("dungeons." + dungeon.getDungeonID() + ".stages");
        if(stageS == null) return;
        for(String stageID : stageS.getKeys(false)) {
            dungeon.addStage(new Stage(dungeon, StringsUtil.asInt(stageID), StringsUtil.parseLocString(stageS.getString(stageID + ".spawnLocation")), stageS.getInt(stageID + ".spawnRate"),
                    stageS.getInt(stageID + ".spawnedMax"), stageS.getInt(stageID + ".totalSpawnedMax"), StringsUtil.parseLocStringList(stageS.getStringList(stageID + ".mobSpawns")),
                    stageS.getString(stageID + ".completionCmd"), StringsUtil.parseDungeonMobStrings(stageS.getStringList(stageID + ".dungeonMobs"))));
        }
    }

    public static void saveDungeons() {
        for(Dungeon dungeon : loadedDungeons.values()) dungeon.save();
    }

    public static List<Dungeon> getDungeons() {
        return new ArrayList<Dungeon>(loadedDungeons.values());
    }

    public static boolean dungeonExists(String dungeonID) {
        return loadedDungeons.containsKey(dungeonID);
    }

    public static Dungeon getDungeon(String dungeonID) {
        return loadedDungeons.get(dungeonID);
    }

    public static Dungeon getDungeon(UUID mob) {
        for(Dungeon dungeon : loadedDungeons.values()) if(dungeon.hasSpawnedMob(mob)) return dungeon;
        return null;
    }

    public static boolean createDungeon(CommandSender sender, String dungeonID) {
        if(dungeonExists(dungeonID)) Messaging.send(sender, "&cThere is already a dungeon with that name!");
        else {
            loadedDungeons.put(dungeonID, new Dungeon(dungeonID));
            loadedDungeons.get(dungeonID).save();
        }
        return loadedDungeons.containsKey(dungeonID);
    }

    public static boolean removeDungeon(CommandSender sender, String dungeonID) {
        if(!dungeonExists(dungeonID)) Messaging.send(sender, "&cThere is no dungeon with that name!");
        else {
            loadedDungeons.remove(dungeonID);
            Configuration.getConfig("dungeons").set("dungeons." + dungeonID, null);
        }
        return !loadedDungeons.containsKey(dungeonID);
    }

    public static DungeonMob getDungeonMob(String mobID) {
        if(dungeonMobs.containsKey(mobID)) return dungeonMobs.get(mobID);
        return null;
    }

    public static boolean isMobSpawn(Location location) {
        return getDungeon(location) != null;
    }

    public static Dungeon getDungeon(Location mobSpawn) {
        for(Dungeon dungeon : getDungeons()) if(dungeon.getStage(mobSpawn) != null) return dungeon;
        return null;
    }
}

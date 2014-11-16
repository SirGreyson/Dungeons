/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.mob;

import net.avonmc.dungeons.dungeon.Stage;
import net.avonmc.dungeons.lobby.Lobby;
import net.avonmc.dungeons.lobby.LobbyHandler;
import net.avonmc.dungeons.util.Configuration;
import net.avonmc.dungeons.util.Messaging;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class MobHandler {

    private static YamlConfiguration mobC = Configuration.getConfig("dungeonMobs");

    private static Map<String, DungeonMob> loadedMobs = new TreeMap<String, DungeonMob>(String.CASE_INSENSITIVE_ORDER);

    public static void loadDungeonMobs() {
        for(String mobID : mobC.getKeys(false)) loadedMobs.put(mobID, DungeonMob.deserialize(mobC.getConfigurationSection(mobID)));
        Messaging.printInfo("Dungeon Mobs successfully loaded!");
    }

    public static DungeonMob getDungeonMob(Entity mob) {
        if(!(mob instanceof LivingEntity)) return null;
        for(String mobID :  loadedMobs.keySet())
            if(loadedMobs.get(mobID).isDungeonMob((LivingEntity) mob)) return loadedMobs.get(mobID);
        return null;
    }

    public static boolean canSpawnMob(Stage stage) {
        return stage.getCurrentSpawned() < stage.getSpawnedLimit() && stage.getTotalSpawned() < stage.getTotalSpawnedLimit();
    }

    private static LivingEntity spawnMob(Stage stage, String mobID, Location location) {
        if(loadedMobs.containsKey(mobID)) return loadedMobs.get(mobID).spawnMob(stage.getDungeon().getActiveLobby(), location);
        else if(EntityType.valueOf(mobID) != null) {
            LivingEntity mob = (LivingEntity) location.getWorld().spawnEntity(location, EntityType.valueOf(mobID));
            if(mob instanceof PigZombie) ((PigZombie) mob).setAngry(true);
            else if(mob instanceof Wolf) ((Wolf) mob).setAngry(true);
            mob.setRemoveWhenFarAway(false);
            mob.setMaxHealth(mob.getMaxHealth() + ((mob.getMaxHealth() * 0.5) * stage.getDungeon().getStageID(stage)));
            mob.setHealth(mob.getMaxHealth());
            mob.setCanPickupItems(false);
            return mob;
        }
        else Messaging.printErr("Tried to spawn invalid Dungeon Mob: " + mobID);
        return null;
    }

    public static void spawnRandomMob(Stage stage) {
        LivingEntity mob = spawnMob(stage, stage.getRandomDungeonMob(), stage.getRandomMobSpawn());
        if(mob != null) stage.addMob(mob);
    }

    public static Stage getDungeonMobStage(UUID mob) {
        for(Lobby lobby : LobbyHandler.getLobbies().values()) {
            if (lobby.getActiveDungeon() == null || lobby.getActiveDungeon().getActiveStage() == null) continue;
            if(lobby.getActiveDungeon().getActiveStage().hasMob(mob)) return lobby.getActiveDungeon().getActiveStage();
        } return null;
    }
}

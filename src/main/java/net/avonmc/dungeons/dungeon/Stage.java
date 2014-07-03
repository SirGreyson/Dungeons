/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package net.avonmc.dungeons.dungeon;

import net.avonmc.dungeons.Dungeons;
import net.avonmc.dungeons.game.GameStage;
import net.avonmc.dungeons.util.Settings;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;

import java.util.*;

public class Stage {

    private Dungeon dungeon;
    private Location spawnLocation;
    private int spawnedLimit;
    private int totalSpawnedLimit;
    private List<String> completionCommands;
    private Set<Location> mobSpawnLocations;
    private List<String> dungeonMobs;

    private int totalSpawned = 0;
    private List<UUID> currentSpawned = new ArrayList<UUID>();

    public Stage(Dungeon dungeon, Location spawnLocation) {
        this.dungeon = dungeon;
        this.spawnLocation = spawnLocation;
        this.spawnedLimit = Settings.DEFAULT_SPAWNED_LIMIT;
        this.totalSpawnedLimit = Settings.DEFAULT_TOTAL_SPAWNED_LIMIT;
        this.mobSpawnLocations = new HashSet<Location>();
    }

    public Stage(Dungeon dungeon, Location spawnLocation, int spawnedLimit, int totalSpawnedLimit, List<String> completionCommand, Set<Location> mobSpawnLocations, List<String> dungeonMobs) {
        this.dungeon = dungeon;
        this.spawnLocation = spawnLocation;
        this.spawnedLimit = spawnedLimit;
        this.totalSpawnedLimit = totalSpawnedLimit;
        this.completionCommands = completionCommand;
        this.mobSpawnLocations = mobSpawnLocations;
        this.dungeonMobs = dungeonMobs;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public String getID() { return String.valueOf(dungeon.getStageID(this) + 1); }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public int getSpawnedLimit() {
        return spawnedLimit;
    }

    public int getTotalSpawnedLimit() {
        return totalSpawnedLimit;
    }

    public void runCompletionCommand() {
        if(completionCommands == null || completionCommands.isEmpty()) return;
        for(OfflinePlayer player : dungeon.getActiveLobby().getPlayers())
            for(String command : completionCommands) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.replace("%p%", player.getName()));
    }

    public boolean hasMobSpawn(Location mobSpawn) {
        return mobSpawnLocations.contains(mobSpawn);
    }

    public Location getRandomMobSpawn() { return (Location) mobSpawnLocations.toArray()[Dungeons.random.nextInt(mobSpawnLocations.size())]; }

    public void addMobSpawn(Location mobSpawn) {
        mobSpawnLocations.add(mobSpawn);
    }

    public void removeMobSpawn(Location mobSpawn) {
        mobSpawnLocations.remove(mobSpawn);
    }

    public void showMobSpawns() {
        for(Location loc : mobSpawnLocations) loc.getBlock().setType(Settings.MOB_SPAWN_MARKER);
    }

    public void hideMobSpawns() {
        for(Location loc : mobSpawnLocations) loc.getBlock().setType(Material.AIR);
    }

    public String getRandomDungeonMob() {
        return dungeonMobs.get(Dungeons.random.nextInt(dungeonMobs.size()));
    }

    public int getCurrentSpawned() { return currentSpawned.size(); }

    public boolean hasMob(UUID mob) { return currentSpawned.contains(mob); }

    public void addMob(LivingEntity mob) {
        currentSpawned.add(mob.getUniqueId());
        totalSpawned++;
    }

    public void removeMob(UUID mob) {
        currentSpawned.remove(mob);
        dungeon.getActiveLobby().getGameBoard().updateBoard();
        if(canContinue()) finish();
    }

    public int getTotalSpawned() { return totalSpawned; }

    public int getLeftToKill() { return (totalSpawnedLimit - totalSpawned) + currentSpawned.size(); }

    public boolean canContinue() { return currentSpawned.size() == 0 && totalSpawned >= totalSpawnedLimit; }

    public void reset() {
        for(LivingEntity e : spawnLocation.getWorld().getLivingEntities())
            if(hasMob(e.getUniqueId())) e.remove();
        this.totalSpawned = 0;
        this.currentSpawned = new ArrayList<UUID>();
    }

    public void finish() {
        runCompletionCommand();
        dungeon.resetDeaths();
        dungeon.getActiveLobby().resetContinueVotes();
        if(!dungeon.hasNextStage()) dungeon.getActiveLobby().finish(false);
        else {
            dungeon.getActiveLobby().broadcast("&aStage complete! Cycling in &e" + Settings.STAGE_ROTATE_DELAY + " &aseconds! \nVote to skip the delay with &6/continue");
            dungeon.getActiveLobby().setStage(GameStage.CONTINUING);
        }
        reset();
    }

    public Map<String, Object> serialize() {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("spawnLocation", StringUtil.parseLoc(spawnLocation));
        output.put("spawnedLimit", spawnedLimit);
        output.put("totalSpawnedLimit", totalSpawnedLimit);
        output.put("completionCmd", completionCommands);
        output.put("mobSpawnLocations", StringUtil.parseLocList(mobSpawnLocations));
        output.put("dungeonMobs", dungeonMobs);
        return output;
    }

    public static Stage deserialize(Dungeon dungeon, ConfigurationSection c) {
        return new Stage(dungeon, StringUtil.parseLocString(c.getString("spawnLocation")), c.getInt("spawnedLimit"), c.getInt("totalSpawnedLimit"), c.getStringList("completionCmd"),
                StringUtil.parseLocStringList(c.getStringList("mobSpawnLocations")), c.getStringList("dungeonMobs"));
    }
}

/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.dungeon;

import net.avonmc.dungeons.game.GameStage;
import net.avonmc.dungeons.lobby.Lobby;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Dungeon {

    private String displayName;
    private boolean isVIP;
    private List<Stage> loadedStages;

    private Lobby activeLobby;
    private Stage activeStage;
    private int deadCount;

    public Dungeon(String displayName, boolean isVIP, List<Stage> loadedStages) {
        this.displayName = displayName;
        this.isVIP = isVIP;
        this.loadedStages = loadedStages;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Stage> getLoadedStages() {
        return loadedStages;
    }

    public boolean hasStage(int stageID) {
        return stageID < loadedStages.size() && stageID >= 0;
    }

    public Stage getStage(int stageID) { return loadedStages.get(stageID); }

    public int getStageID(Stage stage) {
        return loadedStages.indexOf(stage);
    }

    public void addStage(Stage stage) {
        loadedStages.add(stage);
    }

    public boolean isVIP() {
        return isVIP;
    }

    public Lobby getActiveLobby() {
        return activeLobby;
    }

    public void setActiveLobby(Lobby activeLobby) {
        this.activeLobby = activeLobby;
    }

    public Stage getActiveStage() {
        return activeStage;
    }

    public boolean hasNextStage() {
        return getStageID(activeStage) != loadedStages.size() - 1;
    }

    public void startNextStage() {
        if(activeStage == null) this.activeStage = loadedStages.get(0);
        else activeStage = loadedStages.get(getStageID(activeStage) + 1);
        activeLobby.broadcast("&aTeleporting to Stage &b" + activeStage.getID() + "&a/&b" + loadedStages.size() + "&a...");
        if(!hasNextStage()) activeLobby.broadcast("&6&lBoss Stage! Good luck!");
        for(OfflinePlayer player : activeLobby.getPlayers()) player.getPlayer().teleport(activeStage.getSpawnLocation());
        activeLobby.setStage(GameStage.RUNNING);
        activeLobby.resetContinueVotes();
    }

    public Stage stageFromMobSpawn(Location mobSpawn) {
        for(Stage stage : loadedStages) if(stage.hasMobSpawn(mobSpawn)) return stage;
        return null;
    }

    public boolean allPlayersDead() { return deadCount >= activeLobby.getPlayers().size(); }

    public int survivorCount() { return activeLobby.getPlayers().size() - deadCount; }

    public void addDeath() { deadCount += 1; }

    public void resetDeaths() { deadCount = 0; }

    public void reset() {
        if(activeStage != null) activeStage.reset();
        this.activeLobby = null;
        this.activeStage = null;
        this.deadCount = 0;
    }

    public Map<String, Object> serialize() {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("displayName", displayName);
        output.put("isVIP", isVIP);
        output.put("stages", serializeStages());
        return output;
    }

    private Map<String, Object> serializeStages() {
        Map<String, Object> output = new HashMap<String, Object>();
        for(Stage stage : loadedStages) output.put(String.valueOf(getStageID(stage)), stage.serialize());
        return output;
    }

    public static Dungeon deserialize(ConfigurationSection c) {
        return new Dungeon(StringUtil.colorize(c.getString("displayName")), c.getBoolean("isVIP"), new ArrayList<Stage>());
    }
}

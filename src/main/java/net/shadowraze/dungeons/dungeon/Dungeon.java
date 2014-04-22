package net.shadowraze.dungeons.dungeon;

import net.shadowraze.dungeons.lobby.Lobby;
import net.shadowraze.dungeons.utils.Configuration;
import net.shadowraze.dungeons.utils.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class Dungeon {

    private String dungeonID;
    private List<Stage> loadedStages;

    private Lobby inLobby;
    private Stage activeStage;
    private Scoreboard scoreboard;
    private Map<UUID, Long> deadPlayers;
    private List<UUID> spawnedMobs;

    public Dungeon(String dungeonID) {
        this.dungeonID = dungeonID;
        this.loadedStages = new ArrayList<Stage>();
        this.scoreboard = createDungeonBoard();
        this.deadPlayers = new HashMap<UUID, Long>();
        this.spawnedMobs = new ArrayList<UUID>();
    }

    public String getDungeonID() {
        return dungeonID;
    }

    public List<Stage> getLoadedStages() {
        return loadedStages;
    }

    public boolean hasStage(int stageID) {
        return stageID < loadedStages.size();
    }

    public Stage getStage(int stageID) {
        return loadedStages.get(stageID);
    }

    public Stage getStage(Location mobSpawn) {
        for(Stage stage : loadedStages) if(stage.getMobSpawns().contains(mobSpawn)) return stage;
        return null;
    }

    public void addStage(Stage stage) {
        loadedStages.add(stage);
    }

    public void removeStage(Stage stage) {
        loadedStages.remove(stage);
    }

    public void removeAllStages() { loadedStages = new ArrayList<Stage>(); }

    public boolean isActive() {
        return inLobby != null;
    }

    public Lobby getLobby() {
        return inLobby;
    }

    public void setLobby(Lobby inLobby) {
        this.inLobby = inLobby;
    }

    public Scoreboard createDungeonBoard() {
        Scoreboard dBoard = Bukkit.getScoreboardManager().getNewScoreboard();
        Team dTeam = dBoard.registerNewTeam(dungeonID);
        dTeam.setAllowFriendlyFire(false);
        return dBoard;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public Stage getActiveStage() {
        return activeStage;
    }

    public void setActiveStage(Stage activeStage) {
        this.activeStage = activeStage;
    }

    public boolean allPlayersDead() {
        return deadPlayers.keySet().size() >= inLobby.getPlayers().size();
    }

    public boolean isPlayerDead(UUID deadPlayer) {
        return deadPlayers.containsKey(deadPlayer);
    }

    public List<UUID> getDeadPlayers() {
        return new ArrayList<UUID>(deadPlayers.keySet());
    }

    public void addDeadPlayer(Player deadPlayer) {
        if(allPlayersDead()) inLobby.finish();
        else {
            Messaging.send(deadPlayer, "&aYou have died! You must wait &b" + Configuration.RESPAWN_DELAY * (activeStage.getStageID() + 1) + " &aseconds before respawning!");
            deadPlayers.put(deadPlayer.getUniqueId(), System.currentTimeMillis());
        }
    }

    public boolean respawnDeadPlayer(Player deadPlayer, boolean forced) {
        if(System.currentTimeMillis() - deadPlayers.get(deadPlayer.getUniqueId()) < Configuration.RESPAWN_DELAY * (activeStage.getStageID() + 1) * 1000 && !forced)
            Messaging.send(deadPlayer, "&cYou cannot respawn yet!");
        else {
            deadPlayers.remove(deadPlayer.getUniqueId());
            inLobby.broadcast("&b" + deadPlayer.getName() + " &ahas respawned!");
        } return !deadPlayers.containsKey(deadPlayer.getUniqueId());
    }

    public boolean hasSpawnedMob(UUID mob) {
        return spawnedMobs.contains(mob);
    }

    public List<UUID> getSpawnedMobs() {
        return spawnedMobs; }

    public void resetDungeon() {
        this.inLobby = null;
        if(activeStage != null) activeStage.resetStage();
        this.activeStage = null;
        scoreboard.getTeam(dungeonID).unregister();
        this.scoreboard = createDungeonBoard();
        this.deadPlayers = new HashMap<UUID, Long>();
        this.spawnedMobs = new ArrayList<UUID>();
    }

    public void save() {
        YamlConfiguration dungeonC = Configuration.getConfig("dungeons");
        dungeonC.set("dungeons." + dungeonID + ".stages", new ArrayList<String>());
        for(Stage stage : loadedStages) stage.save(loadedStages.indexOf(stage));
        Configuration.saveConfig("dungeons");
    }
}

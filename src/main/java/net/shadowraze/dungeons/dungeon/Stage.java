package net.shadowraze.dungeons.dungeon;

import net.shadowraze.dungeons.Dungeons;
import net.shadowraze.dungeons.utils.Configuration;
import net.shadowraze.dungeons.utils.StringsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class Stage {

    private Dungeon dungeon;
    private int stageID;
    private Location spawnLocation;
    private int spawnRate;
    private int spawnedMax;
    private int totalSpawnedMax;
    private String completionCmd;
    private List<Location> mobSpawns;
    private List<DungeonMob> dungeonMobs;

    private int voteContinue;
    private int totalSpawnedCount;
    private BukkitTask gameTask;
    private int stageCountdown;
    private boolean isFinished;

    public Stage(Dungeon dungeon, int stageID, Location spawnLocation) {
        this.dungeon = dungeon;
        this.stageID = stageID;
        this.spawnLocation = spawnLocation;
        this.spawnRate = Configuration.DEFUALT_SPAWNRATE;
        this.spawnedMax = Configuration.DEFAULT_SPAWNED;
        this.totalSpawnedMax = Configuration.DEFAULT_SPAWNED_TOTAL;
        this.completionCmd = "NONE";
        this.mobSpawns = new ArrayList<Location>();
        this.dungeonMobs = new ArrayList<DungeonMob>();
    }

    public Stage(Dungeon dungeon, int stageID, Location spawnLocation, int spawnRate, int spawnedMax, int totalSpawnedMax, List<Location> mobSpawns, String completionCmd, List<DungeonMob> dungeonMobs) {
        this.dungeon = dungeon;
        this.stageID = stageID;
        this.spawnLocation = spawnLocation;
        this.spawnRate = spawnRate;
        this.spawnedMax = spawnedMax;
        this.totalSpawnedMax = totalSpawnedMax;
        this.mobSpawns = mobSpawns;
        this.completionCmd = completionCmd;
        this.dungeonMobs = dungeonMobs;
    }

    public Dungeon getDungeon() {
        return dungeon;
    }

    public int getStageID() {
        return stageID;
    }

    public boolean isLastStage() {
        return !dungeon.hasStage(stageID + 1);
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public int getSpawnRate() {
        return spawnRate;
    }

    public int getSpawnedMax() {
        return spawnedMax;
    }

    public int getTotalSpawnedMax() {
        return totalSpawnedMax;
    }

    public String getCompletionCmd() {
        return completionCmd;
    }

    public void runCompletionCmd() {
        if(completionCmd.equalsIgnoreCase("NONE")) return;
        for(UUID uuid : dungeon.getLobby().getPlayers()) {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null) Bukkit.dispatchCommand(Bukkit.getConsoleSender(), completionCmd.replace("%p%", player.getName()));
        }
    }

    public List<Location> getMobSpawns() {
        return mobSpawns;
    }

    public void addMobSpawn(Location location) {
        mobSpawns.add(location);
    }

    public void removeMobSpawn(Location location) {
        mobSpawns.remove(location);
    }

    public void runMobSpawnTask() {
        this.totalSpawnedCount = 0;
        this.gameTask = Bukkit.getScheduler().runTaskTimer(Dungeons.getPlugin(), new Runnable() {
            @Override
            public void run() {
                for(int i = dungeon.getSpawnedMobs().size(); i <= spawnedMax && totalSpawnedCount < totalSpawnedMax; i++) {
                    dungeonMobs.get(new Random().nextInt(dungeonMobs.size())).spawnMob(dungeon, mobSpawns.get(new Random().nextInt(mobSpawns.size())));
                    totalSpawnedCount++;
                } if(totalSpawnedCount >= totalSpawnedMax && dungeon.getSpawnedMobs().size() == 0) finishStage();
            }
        }, 20L, spawnRate * 20);
    }

    public List<DungeonMob> getDungeonMobs() {
        return dungeonMobs;
    }

    public void addContinueVote(CommandSender sender) {
        voteContinue++;
        dungeon.getLobby().broadcast("&b" + sender.getName() + " &ahas voted to continue! " +
                (dungeon.getLobby().getPlayers().size() - voteContinue == 0 ? "" : "&b" + String.valueOf(dungeon.getLobby().getPlayers().size() - voteContinue) + " &amore votes needed!"));
        if(voteContinue == dungeon.getLobby().getPlayers().size()) {
            if(gameTask == null) return;
            gameTask.cancel();
            if(dungeon.getLoadedStages().indexOf(this) != dungeon.getLoadedStages().size() - 1)
                dungeon.getLoadedStages().get(dungeon.getLoadedStages().indexOf(this) + 1).startStage();
            else dungeon.getLobby().finish();
        }
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void startStage() {

        if(gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }

        dungeon.getLobby().broadcast("&aTeleporting to Stage&b " + (stageID + 1) + "&a/&b" + dungeon.getLoadedStages().size() + "&a...");dungeon.getDeadPlayers().clear();
        for(UUID deadPlayer : dungeon.getDeadPlayers()) if(Bukkit.getPlayer(deadPlayer) != null) dungeon.respawnDeadPlayer(Bukkit.getPlayer(deadPlayer), true);
        for(UUID player : dungeon.getLobby().getPlayers()) if(Bukkit.getPlayer(player) != null) Bukkit.getPlayer(player).teleport(spawnLocation);
        runMobSpawnTask();
        dungeon.setActiveStage(this);
        isFinished = false;
    }

    public void resetStage() {
        if(!dungeon.getSpawnedMobs().isEmpty())
            for(LivingEntity entity : spawnLocation.getWorld().getLivingEntities())
                if(dungeon.hasSpawnedMob(entity.getUniqueId())) dungeon.getSpawnedMobs().remove(entity.getUniqueId());
        voteContinue = 0;
        totalSpawnedCount = 0;
        if(gameTask != null) gameTask.cancel();;
        gameTask = null;
    }

    public void finishStage() {
        gameTask.cancel();
        this.gameTask = null;
        this.isFinished = true;
        runCompletionCmd();
        startNextStage();
    }

    public void startNextStage() {
        stageCountdown = Configuration.NEXT_STAGE_DELAY + 1;
        gameTask = Bukkit.getScheduler().runTaskTimer(Dungeons.getPlugin(), new Runnable() {
            @Override
            public void run() {
                stageCountdown--;
                if (stageCountdown % 5 == 0 && stageCountdown > 0) {
                    if (isLastStage()) dungeon.getLobby().broadcast("&aTeleporting to lobby in &b" + stageCountdown + " &aseconds...");
                    else dungeon.getLobby().broadcast("&aNext stage starting in &b" + stageCountdown + " &aseconds...");
                }
                else if (stageCountdown <= 0) {
                    if (!isLastStage()) {
                        dungeon.getLoadedStages().get(stageID + 1).startStage();
                        resetStage();
                    }
                    else dungeon.getLobby().finish();
                }
            }
        }, 20, 20);
    }

    public void save(int saveID) {
        YamlConfiguration dungeonC = Configuration.getConfig("dungeons");
        String path = "dungeons." + dungeon.getDungeonID() + ".stages." + saveID;
        dungeonC.set(path + ".spawnLocation", StringsUtil.parseLoc(spawnLocation));
        dungeonC.set(path + ".completionCmd", completionCmd);
        dungeonC.set(path + ".spawnRate", spawnRate);
        dungeonC.set(path + ".spawnedMax", spawnedMax);
        dungeonC.set(path + ".totalSpawnedMax", totalSpawnedMax);
        dungeonC.set(path + ".mobSpawns", StringsUtil.parseLocList(mobSpawns));
        dungeonC.set(path + ".dungeonMobs", StringsUtil.parseDungeonMobs(dungeonMobs));
    }
}

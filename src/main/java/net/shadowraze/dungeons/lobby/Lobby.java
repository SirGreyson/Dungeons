package net.shadowraze.dungeons.lobby;

import com.sk89q.worldedit.util.YAMLConfiguration;
import net.shadowraze.dungeons.Dungeons;
import net.shadowraze.dungeons.dungeon.Dungeon;
import net.shadowraze.dungeons.utils.Configuration;
import net.shadowraze.dungeons.utils.Messaging;
import net.shadowraze.dungeons.utils.StringsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class Lobby {

    private String lobbyID;
    private boolean isVIP;
    private int maxPlayers;
    private Location spawnLocation;
    private List<Dungeon> dungeons;
    private List<Location> lobbySigns;

    private BukkitTask startTask;
    private int startCountdown;
    private boolean inProgress;
    private Dungeon activeDungeon;
    private List<UUID> players;

    public Lobby(String lobbyID, boolean isVIP, int maxPlayers, Location spawnLocation) {
        this.lobbyID = lobbyID;
        this.isVIP = isVIP;
        this.maxPlayers = maxPlayers;
        this.spawnLocation = spawnLocation;
        this.dungeons = new ArrayList<Dungeon>();
        this.lobbySigns = new ArrayList<Location>();
    }

    public Lobby(String lobbyID, boolean isVIP, int maxPlayers, Location spawnLocation, List<Dungeon> dungeons, List<Location> lobbySigns) {
        this.lobbyID = lobbyID;
        this.isVIP = isVIP;
        this.maxPlayers = maxPlayers;
        this.spawnLocation = spawnLocation;
        this.dungeons = dungeons;
        this.lobbySigns = lobbySigns;
        this.players = new ArrayList<UUID>();
        if(getAvaialableDungeons().size() > 0) setActiveDungeon(getAvaialableDungeons().get(new Random().nextInt(getAvaialableDungeons().size())));
        else Messaging.printErr("Please add some dungeons for lobby " + lobbyID + " or else there may be errors!");
        updateLobbySigns();
    }

    public String getLobbyID() {
        return lobbyID;
    }

    public boolean isVIP() {
        return isVIP;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isFull() {
        return players.size() >= maxPlayers;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public Dungeon getActiveDungeon() {
        return activeDungeon;
    }

    public void setActiveDungeon(Dungeon dungeon) {
        this.activeDungeon = dungeon;
        dungeon.setLobby(this);
    }

    public List<Dungeon> getAvaialableDungeons() {
        List<Dungeon> dungeonList = new ArrayList<Dungeon>();
        for(Dungeon dungeon : dungeons) if(!dungeon.isActive()) dungeonList.add(dungeon);
        return dungeonList;
    }

    public boolean hasDungeon(Dungeon dungeon) {
        return dungeons.contains(dungeon);
    }

    public Sign getLobbySign(Location location) {
        try {
            return (Sign) location.getBlock().getState();
        } catch (ClassCastException e) {
            Messaging.printErr("Removing lobby sign at location " + StringsUtil.parseLoc(location) + ". Sign not found!");
            return null;
        }
    }

    public List<Location> getLobbySigns() {
        return lobbySigns;
    }

    public void removeLobbySign(Location location) {
        lobbySigns.remove(location);
    }

    public void updateLobbySigns() {
        Iterator<Location> lobbySignIt = lobbySigns.iterator();
        while(lobbySignIt.hasNext()) {
            Location signLoc = lobbySignIt.next();
            if(getLobbySign(signLoc) == null) lobbySignIt.remove();
            else updateLobbySign((Sign) signLoc.getBlock().getState());
        }
    }

    public void updateLobbySign(Sign lobbySign) {
        for(int i = 0; i < 4; i++) lobbySign.setLine(i, StringsUtil.parseLobbyVars(Configuration.LOBBY_SIGN_FORMAT.get(i), this));
        lobbySign.update();
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public String getLeader() {
        if(!isVIP) return "NOT_VIP";
        else return players.size() > 0 ? Bukkit.getPlayer(players.get(0)).getName() : "NONE";
    }

    public boolean hasPlayer(UUID player) {
        return players.contains(player);
    }

    public boolean addPlayer(Player player) {
        if(isVIP && !player.hasPermission("dungeons.vip") && !LobbyManager.hasInvite(player.getUniqueId(), lobbyID)) Messaging.send(player, "&cYou cannot join this lobby unless invited by a VIP!");
        else if(isVIP && players.size() > 0 && !LobbyManager.hasInvite(player.getUniqueId(), lobbyID)) Messaging.send(player, "&cSorry, this lobby is already claimed by &6" + getLeader() + "&c!");
        else if(isFull()) Messaging.send(player, "&cSorry, this lobby is already full!");
        else if(isInProgress()) Messaging.send(player, "&cSorry, this lobby is already in progress and cannot be joined!");
        else {
            if(LobbyManager.hasInvite(player.getUniqueId(), lobbyID)) LobbyManager.getLobbyInvites().remove(player.getUniqueId());
            if(activeDungeon != null) activeDungeon.getScoreboard().getTeam(activeDungeon.getDungeonID()).addPlayer(player);
            players.add(player.getUniqueId());
            Messaging.send(player, "&aSuccessfully joined &6" + lobbyID);
            broadcast("&b" + player.getName() + "&a has joined the lobby!");
            updateLobbySigns();
            if(isFull()) tryStart(false);
        }
        return players.contains(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        player.teleport(Configuration.SPAWN_LOCATION);
        Messaging.send(player, "&aYou have left your lobby!");
        broadcast("&b" + player.getName() + "&c has left the lobby");
        updateLobbySigns();
    }

    public void broadcast(String message) {
        for(UUID player : players) Messaging.send(Bukkit.getServer().getPlayer(player), message);
    }

    public boolean isStarting() {
        return startTask != null;
    }

    public void tryStart(final boolean forced) {
        startCountdown = Configuration.LOBBY_START_DELAY + 1;
        startTask = Bukkit.getScheduler().runTaskTimer(Dungeons.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if(isFull() || forced) {
                    startCountdown--;
                    if(startCountdown % 5 == 0 && startCountdown != 0) broadcast("&aGame starting in &b" + startCountdown + " &aseconds...");
                    else if(startCountdown <= 0) start();
                } else if((!isFull() && !forced) || players.size() == 0) {
                    broadcast("&aGame start cancelled...");
                    startTask.cancel();
                    startTask = null;
                }
            }
        }, 20, 20);
    }

    public void start() {

        if(startTask != null) {
            startTask.cancel();
            startTask = null;
        }

        broadcast("&aGame starting! Prepare for battle!");
        activeDungeon.getStage(0).startStage();
        setInProgress(true);
    }

    public void finish() {
        broadcast("&aGame over! Thanks for playing!");
        for(UUID player : players) if(Bukkit.getPlayer(player) != null) Bukkit.getPlayer(player).teleport(Configuration.SPAWN_LOCATION);
        players = new ArrayList<UUID>();
        List<Dungeon> avaiableDungeons = getAvaialableDungeons();
        activeDungeon.resetDungeon();
        setActiveDungeon(avaiableDungeons.get(new Random().nextInt(avaiableDungeons.size())));
        setInProgress(false);
    }

    public void save() {
        YamlConfiguration lobbyC = Configuration.getConfig("lobbies");
        String path = (isVIP ? "vip."  : "default.") + lobbyID.toLowerCase();
        lobbyC.set(path + ".maxPlayers", maxPlayers);
        lobbyC.set(path + ".spawnLocation", StringsUtil.parseLoc(spawnLocation));
        lobbyC.set(path + ".dungeons", StringsUtil.parseDungeons(dungeons));
        lobbyC.set(path + ".lobbySigns", StringsUtil.parseLocList(lobbySigns));
        Configuration.saveConfig("lobbies");
    }
}

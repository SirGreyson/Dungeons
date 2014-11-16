/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.lobby;/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

import me.confuser.barapi.BarAPI;
import net.avonmc.dungeons.dungeon.Dungeon;
import net.avonmc.dungeons.dungeon.DungeonHandler;
import net.avonmc.dungeons.game.GameBoard;
import net.avonmc.dungeons.game.GameStage;
import net.avonmc.dungeons.game.GameTask;
import net.avonmc.dungeons.lobby.type.Default;
import net.avonmc.dungeons.lobby.type.VIP;
import net.avonmc.dungeons.util.Messaging;
import net.avonmc.dungeons.util.Settings;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public abstract class Lobby {

    private int maxPlayers;
    private Location lobbySpawn;
    private Set<Location> lobbySigns;

    private GameTask lobbyTask;
    private GameBoard gameBoard;
    private Dungeon activeDungeon;

    private List<UUID> continueVotes = new ArrayList<UUID>();

    public Lobby(int maxPlayers, Location lobbySpawn, Set<Location> lobbySigns) {
        this.maxPlayers = maxPlayers;
        this.lobbySpawn = lobbySpawn;
        this.lobbySigns = lobbySigns;
        this.lobbyTask = new GameTask(this);
        this.gameBoard = new GameBoard(this);
    }

    public abstract boolean isVIP();

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public Location getLobbySpawn() {
        return lobbySpawn;
    }

    public void setLobbySpawn(Location lobbySpawn) {
        this.lobbySpawn = lobbySpawn;
    }

    public boolean isLobbySign(Location location) {
        return lobbySigns.contains(location) && location.getBlock().getState() instanceof Sign;
    }

    public boolean hasLobbySign(Location signLoc) { return lobbySigns.contains(signLoc); }

    public void addLobbySign(Location signLoc) {
        lobbySigns.add(signLoc);
    }

    public void removeLobbySign(Location signLoc) {
        lobbySigns.remove(signLoc);
    }

    public abstract void updateLobbySign(Sign lobbySign);

    public void updateLobbySigns() {
        for(Location signLoc : lobbySigns)
            if(isLobbySign(signLoc)) updateLobbySign((Sign) signLoc.getBlock().getState());
    }

    public GameStage getStage() {
        return lobbyTask.getGameStage();
    }

    public void setStage(GameStage lobbyGameStage) {
        lobbyTask.setGameStage(lobbyGameStage);
        updateLobbySigns();
        if(lobbyGameStage != GameStage.DISABLING) gameBoard.updateBoard();
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public Dungeon getActiveDungeon() {
        return activeDungeon;
    }

    public void setActiveDungeon(Dungeon activeDungeon) {
        if(this.activeDungeon != null) this.activeDungeon.reset();
        this.activeDungeon = activeDungeon;
        if(activeDungeon != null) activeDungeon.setActiveLobby(this);
        else Messaging.printErr("Error! Tried to set active Dungeon as null!");
        updateLobbySigns();
    }

    public boolean isFull() {
        return getPlayers().size() >= maxPlayers;
    }

    public Set<OfflinePlayer> getPlayers() {
        return gameBoard.getPlayers();
    }

    public Player getPlayer(UUID player) { return Bukkit.getPlayer(player); }

    public boolean hasPlayer(Player player) { return gameBoard.hasPlayer(player); }

    public abstract void addPlayer(Player player);

    public abstract void removePlayer(Player player);

    private void removeAllPlayers() {
        for(OfflinePlayer player : getPlayers()) {
            gameBoard.removePlayer(player.getPlayer());
            player.getPlayer().setHealth(player.getPlayer().getMaxHealth());
            for(PotionEffect pe : player.getPlayer().getActivePotionEffects()) player.getPlayer().removePotionEffect(pe.getType());
            player.getPlayer().teleport(Settings.SPAWN_LOCATION);
            if(BarAPI.hasBar(player.getPlayer())) BarAPI.removeBar(player.getPlayer());
            if((activeDungeon.hasNextStage() || !activeDungeon.getActiveStage().canContinue()) && getStage() != GameStage.DISABLING) {
                player.getPlayer().getInventory().clear();
                player.getPlayer().getInventory().setArmorContents(null);
            }
        }
    }

    public boolean canContinue() { return continueVotes.size() >= getPlayers().size(); }

    public boolean hasContinueVote(Player player) { return continueVotes.contains(player.getUniqueId()); }

    public void addContinueVote(Player player) {
        continueVotes.add(player.getUniqueId());
        broadcast("&b" + player.getName() + " &ahas voted to continue! " + (canContinue() ? "&aContinuing..." : "&b" + (getPlayers().size() - continueVotes.size()) + " &amore votes needed to continue!"));
        if (canContinue()) activeDungeon.startNextStage();
    }

    public void resetContinueVotes() { this.continueVotes = new ArrayList<UUID>(); }

    public void broadcast(String message) {
        for(OfflinePlayer player : getPlayers()) Messaging.send(player.getPlayer(), message);
    }

    public void start() {
        broadcast("&aGame starting! Prepare for battle!");
        activeDungeon.startNextStage();
    }

    public void finish(boolean isDisabling) {
        setStage(isDisabling ? GameStage.DISABLING : GameStage.RESETTING);
        if(activeDungeon == null || activeDungeon.getActiveStage() == null) return;
        broadcast("&aGame over! " + (!activeDungeon.hasNextStage() && activeDungeon.getActiveStage().canContinue() ? "You win!" : "You made it to Stage &b" + activeDungeon.getActiveStage().getID() + "&a/&b" + activeDungeon.getLoadedStages().size()));
        removeAllPlayers();
        if(!isDisabling) {
            setActiveDungeon(DungeonHandler.getRandomDungeon(isVIP()));
            if(isVIP()) ((VIP) this).reset();
            setStage(GameStage.WAITING);
        }
    }

    public Map<String, Object> serialize() {
        Map<String, Object> output = new HashMap<String, Object>();
        output.put("maxPlayers", maxPlayers);
        output.put("lobbySpawn", StringUtil.parseLoc(lobbySpawn));
        output.put("lobbySigns", StringUtil.parseLocList(lobbySigns));
        return output;
    }

    public static Lobby deserialize(boolean isVIP, ConfigurationSection c) {
        if(isVIP) return new VIP(c.getInt("maxPlayers"), StringUtil.parseLocString(c.getString("lobbySpawn")), StringUtil.parseLocStringList(c.getStringList("lobbySigns")));
        return new Default(c.getInt("maxPlayers"), StringUtil.parseLocString(c.getString("lobbySpawn")), StringUtil.parseLocStringList(c.getStringList("lobbySigns")));
    }
}
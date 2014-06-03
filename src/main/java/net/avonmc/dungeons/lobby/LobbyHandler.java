/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package net.avonmc.dungeons.lobby;

import net.avonmc.dungeons.dungeon.DungeonHandler;
import net.avonmc.dungeons.game.GameStage;
import net.avonmc.dungeons.lobby.type.Default;
import net.avonmc.dungeons.lobby.type.VIP;
import net.avonmc.dungeons.util.Configuration;
import net.avonmc.dungeons.util.Messaging;
import net.avonmc.dungeons.util.Settings;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

public class LobbyHandler {

    private static YamlConfiguration lobbyC = Configuration.getConfig("lobbies");

    private static Map<String, Lobby> loadedLobbies = new TreeMap<String, Lobby>(String.CASE_INSENSITIVE_ORDER);

    public static void loadLobbies() {
        for(String lobbyType : lobbyC.getKeys(false))
            for(String lobbyID : lobbyC.getConfigurationSection(lobbyType).getKeys(false)) loadLobby(lobbyID, lobbyType.equalsIgnoreCase("VIP"));
        Messaging.printInfo("Lobbies successfully loaded!");
    }

    private static void loadLobby(String lobbyID, boolean isVIP) {
        Lobby lobby = Lobby.deserialize(isVIP, lobbyC.getConfigurationSection((isVIP ? "VIP." : "DEFAULT.") + lobbyID));
        loadedLobbies.put(lobbyID, lobby);
        lobby.setActiveDungeon(DungeonHandler.getRandomDungeon(isVIP));
        if(lobby.getActiveDungeon() == null) Messaging.printErr(lobbyID + " does not have a Dungeon!");
    }

    public static void saveLobbies() {
        for(String lobbyID : loadedLobbies.keySet())
            lobbyC.set((loadedLobbies.get(lobbyID).isVIP() ? "VIP." : "DEFAULT.") + lobbyID, loadedLobbies.get(lobbyID).serialize());
        Messaging.printInfo("Lobbies successfully saved!");
    }

    public static void handleDisable() {
        for(Lobby lobby : loadedLobbies.values()) lobby.finish(true);
    }

    public static Map<String, Lobby> getLobbies() { return loadedLobbies; }

    public static boolean lobbyExists(String lobbyID) { return loadedLobbies.containsKey(lobbyID); }

    public static Lobby getLobby(String lobbyID) {
        return loadedLobbies.get(lobbyID);
    }

    public static String getLobbyID(Lobby lobby) {
        for(String lobbyID : loadedLobbies.keySet())
            if(loadedLobbies.get(lobbyID) == lobby) return lobbyID;
        return null;
    }

    public static void createLobby(String lobbyID, boolean isVIP, Location spawnLocation) {
        loadedLobbies.put(lobbyID, isVIP ? new VIP(Settings.DFEAULT_MAX_PLAYERS, spawnLocation, new HashSet<Location>()) : new Default(Settings.DFEAULT_MAX_PLAYERS, spawnLocation, new HashSet<Location>()));
    }

    public static void removeLobby(String lobbyID) {
        lobbyC.set((loadedLobbies.get(lobbyID).isVIP() ? "VIP." : "DEFAULT.") + lobbyID, null);
        loadedLobbies.remove(lobbyID);
    }

    public static Lobby getSignLobby(Location signLoc) {
        for(Lobby lobby : loadedLobbies.values())
            if(lobby.hasLobbySign(signLoc)) return lobby;
        return null;
    }

    public static Lobby getPlayerLobby(Player player) {
        for(Lobby lobby : loadedLobbies.values())
            if(lobby.hasPlayer(player)) return lobby;
        return null;
    }

    public static void addLobbyPlayer(Player player, Lobby lobby) {
        if(getPlayerLobby(player) != null) Messaging.send(player, "&cYou are already in a Lobby! Type &6/lobby leave &cto leave");
        else if(lobby.getActiveDungeon() == null) Messaging.send(player, "&cThis Lobby has no active Dungeon and cannot be joined!");
        else lobby.addPlayer(player);
    }

    public static void removeLobbyPlayer(Player player) {
        Lobby lobby = getPlayerLobby(player);
        if(lobby == null) Messaging.send(player, "&cYou are not currently in a Lobby!");
        else if(lobby.getStage() == GameStage.RUNNING) Messaging.send(player, "&cYou cannot leave once the Lobby has started!");
        else lobby.removePlayer(player);
    }

    public static void inviteLobbyPlayer(Player sender, String playerName) {
        Lobby lobby = getPlayerLobby(sender);
        if(lobby == null) Messaging.send(sender, "&cYou are not currently in a Lobby!");
        else if(!lobby.isVIP()) Messaging.send(sender, "&cSorry, this command can only be used in VIP Lobbies!");
        else if(((VIP) lobby).getLeader() != sender) Messaging.send(sender, "&cSorry, only the Lobby leader can send invites!");
        else if(Bukkit.getPlayer(playerName) == null) Messaging.send(sender, "&cError! There is no player online with that name!");
        else ((VIP) lobby).invitePlayer(Bukkit.getPlayer(playerName));
    }
}

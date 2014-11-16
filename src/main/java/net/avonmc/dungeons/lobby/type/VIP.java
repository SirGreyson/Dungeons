/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.lobby.type;

import net.avonmc.dungeons.game.GameStage;
import net.avonmc.dungeons.lobby.Lobby;
import net.avonmc.dungeons.lobby.LobbyHandler;
import net.avonmc.dungeons.util.Messaging;
import net.avonmc.dungeons.util.Settings;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class VIP extends Lobby {

    private UUID lobbyLeader;
    private List<UUID> invites = new ArrayList<UUID>();

    public VIP(int maxPlayers, Location lobbySpawn, Set<Location> lobbySigns) {
        super(maxPlayers, lobbySpawn, lobbySigns);
    }

    @Override
    public boolean isVIP() {
        return true;
    }

    @Override
    public void updateLobbySign(Sign lobbySign) {
        lobbySign.setLine(0, StringUtil.colorize("&2[VIP Dungeon]"));
        lobbySign.setLine(1, getActiveDungeon() == null ? "NONE" : getActiveDungeon().getDisplayName());
        lobbySign.setLine(2, getLeaderString());
        lobbySign.setLine(3, getStage().toString());
        lobbySign.update();
    }

    @Override
    public void addPlayer(Player player) {
        if(!player.hasPermission("dungeons.vip") && !hasInvite(player.getUniqueId())) Messaging.send(player, "&cYou cannot join this Dungeon unless invited by " + (hasLeader() ? getLeaderString() : "a VIP!"));
        else if(isFull()) Messaging.send(player, "&cSorry, this Lobby is already full!");
        else if(getStage() != GameStage.WAITING) Messaging.send(player, "&cSorry, this Lobby is already in progress!");
        else {
            if(hasInvite(player.getUniqueId())) invites.remove(player.getUniqueId());
            broadcast("&b" + player.getName() + " &7has joined the Lobby!");
            player.teleport(getLobbySpawn());
            if(getPlayers().size() == 0) setLeader(player.getUniqueId());
            getGameBoard().addPlayer(player);
            updateLobbySigns();
        }
    }

    @Override
    public void removePlayer(Player player) {
        broadcast("&b" + player.getName() + " &7has left the Lobby!");
        getGameBoard().removePlayer(player);
        updateLobbySigns();
        if(isLeader(player)) removeLeader();
        if(player != null && player.isOnline()) {
            player.teleport(Settings.SPAWN_LOCATION);
            Messaging.send(player, "&aYou have left your Lobby!");
        }
        if(getStage() == GameStage.RUNNING || getStage() == GameStage.CONTINUING) {
            if(getPlayers().size() <= 0) finish(false);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
        }
    }

    public boolean hasLeader() {
        return lobbyLeader != null;
    }

    public boolean isLeader(Player player) {
        return lobbyLeader == player.getUniqueId();
    }

    public Player getLeader() {
        return Bukkit.getPlayer(lobbyLeader);
    }

    public String getLeaderString() {
        return hasLeader() ? ChatColor.AQUA + getPlayer(lobbyLeader).getName() : ChatColor.RED + "NO LEADER";
    }

    public void setLeader(UUID lobbyLeader) {
        this.lobbyLeader = lobbyLeader;
        if(lobbyLeader != null) Messaging.send(getLeader(), "&aUse &b/lobby invite <player> &ato invite players to this Lobby!");
        updateLobbySigns();
    }

    public void removeLeader() {
        broadcast("&aLobby leader has left... Disbanding Lobby!");
        for(OfflinePlayer player : getPlayers()) removePlayer(player.getPlayer());
        setLeader(null);
    }

    public boolean hasInvite(UUID player) {
        return invites.contains(player);
    }

    public boolean invitePlayer(Player player) {
        if(LobbyHandler.getPlayerLobby(player) != null) Messaging.send(getLeader(), "&cSorry, that player is already in a Lobby!");
        else if(hasInvite(player.getUniqueId())) Messaging.send(getLeader(), "&cThat player already has a pending invite!");
        else {
            invites.add(player.getUniqueId());
            Messaging.send(player, getLeaderString() + " &ahas invited you to join their Lobby! Type &b/lobby join " + LobbyHandler.getLobbyID(this) + " &ato accept!");
            broadcast(getLeaderString() + " &ahas invited &b" + player.getName() + " &ato join the Lobby!");
        }
        return invites.contains(player.getUniqueId());
    }

    public void reset() {
        this.lobbyLeader = null;
        this.invites = new ArrayList<UUID>();
    }
}

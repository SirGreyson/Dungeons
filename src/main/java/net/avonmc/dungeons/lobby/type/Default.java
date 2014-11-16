/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.lobby.type;

import net.avonmc.dungeons.game.GameStage;
import net.avonmc.dungeons.lobby.Lobby;
import net.avonmc.dungeons.util.Messaging;
import net.avonmc.dungeons.util.Settings;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;

import java.util.Set;

public class Default extends Lobby {

    public Default(int maxPlayers, Location lobbySpawn, Set<Location> lobbySigns) {
        super(maxPlayers, lobbySpawn, lobbySigns);
    }

    @Override
    public boolean isVIP() {
        return false;
    }

    @Override
    public void updateLobbySign(Sign lobbySign) {
        lobbySign.setLine(0, StringUtil.colorize("&9[Dungeon]"));
        lobbySign.setLine(1, getActiveDungeon() == null ? "NONE" : getActiveDungeon().getDisplayName());
        lobbySign.setLine(2, StringUtil.colorize("&a" + getPlayers().size() + "&b/&a" + getMaxPlayers()));
        lobbySign.setLine(3, getStage().toString());
        lobbySign.update();
    }

    @Override
    public void addPlayer(Player player) {
        if(isFull()) Messaging.send(player, "&cSorry, this Lobby is already full!");
        else if(getStage() != GameStage.WAITING) Messaging.send(player, "&cSorry, this Lobby is already in progress!");
        else {
            broadcast("&b" + player.getName() + " &7has joined the Lobby!");
            player.teleport(getLobbySpawn());
            getGameBoard().addPlayer(player);
            updateLobbySigns();
            if(isFull()) setStage(GameStage.STARTING);
        }
    }

    @Override
    public void removePlayer(Player player) {
        broadcast("&b" + player.getName() + " &7has left the Lobby!");
        getGameBoard().removePlayer(player);
        updateLobbySigns();
        if(player.isOnline()) {
            player.teleport(Settings.SPAWN_LOCATION);
            Messaging.send(player, "&aYou have left your Lobby!");
        }
        if(getStage() == GameStage.RUNNING || getStage() == GameStage.CONTINUING) {
            if(getPlayers().size() <= 0) finish(false);
            player.getInventory().clear();
            player.getInventory().setArmorContents(null);
        }
    }
}

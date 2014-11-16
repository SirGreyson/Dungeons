/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Settings {

    public static String PREFIX;
    public static String BROADCAST_PREFIX;
    public static Location SPAWN_LOCATION;
    public static String DUNGEON_WORLD;
    public static List<String> LOBBY_SIGN_FORMAT;
    public static int LOBBY_START_DELAY;
    public static int MOB_SPAWN_DELAY;
    public static int STAGE_ROTATE_DELAY;
    public static Material MOB_SPAWN_MARKER;

    public static int DFEAULT_MAX_PLAYERS;
    public static int DEFAULT_SPAWNED_LIMIT;
    public static int DEFAULT_TOTAL_SPAWNED_LIMIT;

    public static String LOBBY_BOARD_TITLE;
    public static List<String> DEFAULT_LOBBY_BOARD;
    public static List<String> VIP_LOBBY_BOARD;
    public static List<String> DEFAULT_DUNGEON_BOARD;

    public void loadSettings() {
        FileConfiguration c = Configuration.getConfig("config");
        PREFIX = StringUtil.colorize(c.getString("prefix"));
        BROADCAST_PREFIX = StringUtil.colorize(c.getString("broadcastPrefix"));
        SPAWN_LOCATION = StringUtil.parseLocString(c.getString("spawnLocation"));
        DUNGEON_WORLD = c.getString("dungeonWorld");
        LOBBY_SIGN_FORMAT = c.getStringList("lobbySignFormat");
        LOBBY_START_DELAY = c.getInt("lobbyStartDelay");
        MOB_SPAWN_DELAY = c.getInt("mobSpawnDelay");
        STAGE_ROTATE_DELAY = c.getInt("stageRotateDelay");
        MOB_SPAWN_MARKER = Material.valueOf(c.getString("mobSpawnMarker"));
        DFEAULT_MAX_PLAYERS = c.getInt("maxPlayers");
        DEFAULT_SPAWNED_LIMIT = c.getInt("spawnedLimit");
        DEFAULT_TOTAL_SPAWNED_LIMIT = c.getInt("totalSpawnedLimit");
        LOBBY_BOARD_TITLE = StringUtil.colorize(c.getString("lobbyBoard.title"));
        DEFAULT_LOBBY_BOARD = c.getStringList("lobbyBoard.default");
        VIP_LOBBY_BOARD = c.getStringList("lobbyBoard.vip");
        DEFAULT_DUNGEON_BOARD = c.getStringList("dungeonBoard");
    }
}

/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.game;

import net.avonmc.dungeons.lobby.Lobby;
import net.avonmc.dungeons.lobby.type.VIP;
import net.avonmc.dungeons.util.Settings;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;
import java.util.Set;

public class GameBoard {

    private Lobby gameLobby;
    private ScoreboardManager scoreboardManager;
    private Scoreboard gameBoard;

    private Team gameTeam;
    private Objective gameObj;

    public GameBoard(Lobby gameLobby) {
        this.gameLobby = gameLobby;
        this.scoreboardManager = Bukkit.getScoreboardManager();
        this.gameBoard = scoreboardManager.getNewScoreboard();

        this.gameTeam = gameBoard.registerNewTeam("gameTeam");
        this.gameTeam.setAllowFriendlyFire(false);

        this.gameObj = gameBoard.registerNewObjective("gameObj", "dummy");
        this.gameObj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    public Lobby getGameLobby() {
        return gameLobby;
    }

    public Scoreboard getGameBoard() {
        return gameBoard;
    }

    public String getTitle() { return gameObj.getDisplayName(); }

    public void setTitle(String newTitle) {
        gameObj.setDisplayName(StringUtil.colorize(newTitle));
    }

    public void updateBoard() {
        registerGameObj();
        if(gameLobby.getStage() != GameStage.RUNNING && gameLobby.getStage() != GameStage.CONTINUING) {
            gameObj.setDisplayName(Settings.LOBBY_BOARD_TITLE);
            List<String> boardList = gameLobby.isVIP() ? Settings.VIP_LOBBY_BOARD : Settings.DEFAULT_LOBBY_BOARD;
            int counter = boardList.size();
            for(String boardLine : boardList)
                gameObj.getScore(fLine(boardLine, true)).setScore(counter--);
        } else {
            gameObj.setDisplayName(gameLobby.getActiveDungeon().getDisplayName());
            int counter = Settings.DEFAULT_DUNGEON_BOARD.size();
            for(String boardLine : Settings.DEFAULT_DUNGEON_BOARD)
                gameObj.getScore(fLine(boardLine, false)).setScore(counter--);
        }
    }

    public boolean hasPlayer(Player player) {
        return gameTeam.hasPlayer(player);
    }

    public Set<OfflinePlayer> getPlayers() {
        return gameTeam.getPlayers();
    }

    public void addPlayer(Player player) {
        gameTeam.addPlayer(player);
        player.setScoreboard(gameBoard);
        updateBoard();
    }

    public void removePlayer(Player player) {
        gameTeam.removePlayer(player);
        player.setScoreboard(scoreboardManager.getNewScoreboard());
        updateBoard();
    }

    private void registerGameObj() {
        if(gameObj != null) gameObj.unregister();
        gameObj = gameBoard.registerNewObjective("gameObj", "dummy");
        gameObj.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    private String fLine(String boardLine, boolean isLobbyBoard) {
        String fLine = isLobbyBoard ?
            boardLine.
                replace("%dungeon%", gameLobby.getActiveDungeon() == null ? "" : gameLobby.getActiveDungeon().getDisplayName()).
                replace("%players%", "&b" + gameLobby.getPlayers().size() + "&a/&b" + gameLobby.getMaxPlayers()).
                replace("%leader%", gameLobby.isVIP() ? ((VIP) gameLobby).getLeaderString() : "")
            : boardLine.
                replace("%dungeon%", gameLobby.getActiveDungeon() == null ? "" : gameLobby.getActiveDungeon().getDisplayName()).
                replace("%stage%", gameLobby.getActiveDungeon().getActiveStage().getID()).
                replace("%maxstage%", String.valueOf(gameLobby.getActiveDungeon().getLoadedStages().size())).
                replace("%dtokens%", "PLACEHOLDER"). //TODO
                replace("%mobcount%", String.valueOf(gameLobby.getActiveDungeon().getActiveStage().getLeftToKill())).
                replace("%survivors%", String.valueOf(gameLobby.getActiveDungeon().survivorCount()));
        return StringUtil.colorize(fLine).substring(0, Math.min(fLine.length(), 17));
    }
}

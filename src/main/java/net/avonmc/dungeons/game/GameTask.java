/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.game;

import net.avonmc.dungeons.Dungeons;
import net.avonmc.dungeons.dungeon.Stage;
import net.avonmc.dungeons.lobby.Lobby;
import net.avonmc.dungeons.mob.MobHandler;
import net.avonmc.dungeons.util.Settings;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class GameTask {

    private Dungeons plugin;
    private Lobby gameLobby;
    private GameStage gameGameStage;

    private BukkitTask gameTask;
    private int countdown;

    public GameTask(Lobby gameLobby) {
        this.gameLobby = gameLobby;
        this.gameGameStage = GameStage.WAITING;
        this.plugin = (Dungeons) Bukkit.getServer().getPluginManager().getPlugin("Dungeons");
    }

    private void run() {
        this.countdown = gameGameStage == GameStage.CONTINUING ? Settings.STAGE_ROTATE_DELAY : Settings.LOBBY_START_DELAY + 1;

        if(gameGameStage == GameStage.STARTING || gameGameStage == GameStage.FORCE_STARTING) {
            this.gameTask = new BukkitRunnable() {
                @Override
                public void run() {
                    if((gameGameStage == GameStage.STARTING && !gameLobby.isFull()) || gameLobby.getPlayers().size() == 0) {
                        gameLobby.setStage(GameStage.WAITING);
                        gameLobby.broadcast("&aSorry, there are no longer enough players to start. Waiting for &b" +
                                (gameLobby.getMaxPlayers() - gameLobby.getPlayers().size()) + " &amore players...");
                    } else {
                        countdown -= 1;
                        if(countdown <= 0) gameLobby.start();
                        else if(countdown % 5 == 0) gameLobby.broadcast("&aGame starting in &b" + countdown + " &aseconds...");
                        gameLobby.getGameBoard().setTitle(Settings.LOBBY_BOARD_TITLE + " &6-&b " + countdown);
                    }
                }
            }.runTaskTimer(plugin, 20, 20);

        } else if (gameGameStage == GameStage.RUNNING) {
            final Stage stage = gameLobby.getActiveDungeon().getActiveStage();
            this.gameTask = new BukkitRunnable() {
                @Override
                public void run() {
                    while(MobHandler.canSpawnMob(stage)) MobHandler.spawnRandomMob(stage);
                    gameLobby.getGameBoard().updateBoard();
                }
            }.runTaskTimer(plugin, Settings.MOB_SPAWN_DELAY * 20, Settings.MOB_SPAWN_DELAY * 20);

        } else if (gameGameStage == GameStage.CONTINUING) {
            this.gameTask = new BukkitRunnable() {
                @Override
                public void run() {
                    countdown -= 1;
                    if(countdown <= 0) gameLobby.getActiveDungeon().startNextStage();
                    else if(countdown % 5 == 0) gameLobby.broadcast("&aCyling to next Stage in &b" + countdown + " &aseconds... \nType &6/continue&a to vote to skip the delay");
                    gameLobby.getGameBoard().setTitle(gameLobby.getGameBoard().getTitle().split(" ")[0] + " &6-&b " + countdown);
                }
            }.runTaskTimer(plugin, 20, 20);
        }
    }

    private void cancel() {
        gameTask.cancel();
        this.gameTask = null;
    }

    public GameStage getGameStage() {
        return gameGameStage;
    }

    public void setGameStage(GameStage gameGameStage) {
        if(gameTask != null) cancel();
        this.gameGameStage = gameGameStage;
        if(gameGameStage.isRunnable()) run();
    }
}

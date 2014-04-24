package net.shadowraze.dungeons;

import net.shadowraze.dungeons.dungeon.Dungeon;
import net.shadowraze.dungeons.dungeon.DungeonManager;
import net.shadowraze.dungeons.lobby.Lobby;
import net.shadowraze.dungeons.lobby.LobbyManager;
import net.shadowraze.dungeons.utils.Messaging;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class DungeonListener implements Listener {

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if(!e.getLine(0).equalsIgnoreCase("[Dungeons]")) return;

        if(!e.getPlayer().hasPermission("dungeons.admin")) Messaging.send(e.getPlayer(), "&cYou do not have permission to place this sign");
        else if(!LobbyManager.lobbyExists(e.getLine(1))) Messaging.send(e.getPlayer(), "&cThere is no lobby with that name!");
        else {
            LobbyManager.getLobby(e.getLine(1)).getLobbySigns().add(e.getBlock().getLocation());
            return;
        }
        e.getBlock().breakNaturally();
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(e.getClickedBlock().getType() == Material.SIGN_POST || e.getClickedBlock().getType() == Material.WALL_SIGN)
            if(LobbyManager.getSignLobby(e.getClickedBlock().getLocation()) != null)
                e.getPlayer().performCommand("lobby join " + LobbyManager.getSignLobby(e.getClickedBlock().getLocation()).getLobbyID());
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Dungeon dungeon = DungeonManager.getDungeon(e.getEntity().getUniqueId());
        if(dungeon == null || e.getEntity().getCustomName() == null || !e.getEntity().getCustomName().equalsIgnoreCase("Minion")) return;
        e.setDroppedExp(0);
        e.getDrops().clear();
        if(dungeon != null) dungeon.getSpawnedMobs().remove(e.getEntity().getUniqueId());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Lobby lobby = LobbyManager.getLobby(e.getEntity().getUniqueId());
        if(lobby == null || !lobby.isInProgress()) return;
        lobby.broadcast("&b" + e.getEntity().getName() + " &chas died! Be cautious until they can respawn!");
        lobby.getActiveDungeon().addDeadPlayer(e.getEntity());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent e) {
        Lobby lobby = LobbyManager.getLobby(e.getPlayer().getUniqueId());
        if(lobby == null || !lobby.isInProgress()) return;
        if(lobby.getActiveDungeon().isPlayerDead(e.getPlayer().getUniqueId())) e.setRespawnLocation(lobby.getSpawnLocation());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.getBlock().getType() == Material.WALL_SIGN || e.getBlock().getType() == Material.SIGN_POST)
            if(LobbyManager.getSignLobby(e.getBlock().getLocation()) != null) {
                LobbyManager.getSignLobby(e.getBlock().getLocation()).removeLobbySign(e.getBlock().getLocation());
                Messaging.send(e.getPlayer(), "&aLobby sign removed!");
            }
        else if(e.getBlock().getType() == Material.SPONGE)
                if(DungeonManager.isMobSpawn(e.getBlock().getLocation())) {
                    DungeonManager.getDungeon(e.getBlock().getLocation()).getStage(e.getBlock().getLocation()).removeMobSpawn(e.getBlock().getLocation());
                    Messaging.send(e.getPlayer(), "&aMob spawn removed!");
                }
    }
}

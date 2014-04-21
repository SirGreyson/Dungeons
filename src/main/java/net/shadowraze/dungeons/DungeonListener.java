package net.shadowraze.dungeons;

import net.shadowraze.dungeons.dungeon.Dungeon;
import net.shadowraze.dungeons.dungeon.DungeonManager;
import net.shadowraze.dungeons.lobby.LobbyManager;
import net.shadowraze.dungeons.utils.Messaging;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;

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

    /*FIXME*/
    @EventHandler
    public void onSignClick(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if(e.getClickedBlock().getType() == Material.SIGN || e.getClickedBlock().getType() == Material.WALL_SIGN)
            if(LobbyManager.getSignLobby(e.getClickedBlock().getLocation()) != null) {
                e.getPlayer().performCommand("/lobby join " + LobbyManager.getSignLobby(e.getClickedBlock().getLocation()).getLobbyID());
                Messaging.send(e.getPlayer(), "&aLobby sign removed!");
            }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        Dungeon dungeon = DungeonManager.getDungeon(e.getEntity().getUniqueId());
        if(dungeon == null) return;
        e.setDroppedExp(0);
        e.getDrops().clear();
        dungeon.getSpawnedMobs().remove(e.getEntity().getUniqueId());
    }

    /*FIXME*/
    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.getBlock().getType() == Material.SIGN || e.getBlock().getType() == Material.WALL_SIGN || e.getBlock().getType() == Material.SIGN_POST)
            if(LobbyManager.getSignLobby(e.getBlock().getLocation()) != null) {
                LobbyManager.getSignLobby(e.getBlock().getLocation()).removeLobbySign(e.getBlock().getLocation());
                Messaging.send(e.getPlayer(), "&aLobby sign removed!");
            }
    }
}

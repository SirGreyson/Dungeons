/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package net.avonmc.dungeons;

import net.avonmc.dungeons.dungeon.Dungeon;
import net.avonmc.dungeons.dungeon.DungeonHandler;
import net.avonmc.dungeons.dungeon.Stage;
import net.avonmc.dungeons.lobby.Lobby;
import net.avonmc.dungeons.lobby.LobbyHandler;
import net.avonmc.dungeons.mob.DungeonMob;
import net.avonmc.dungeons.mob.MobHandler;
import net.avonmc.dungeons.util.Messaging;
import net.avonmc.dungeons.util.Settings;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;

public class EventListener implements Listener {

    private Dungeons plugin;

    public EventListener(Dungeons plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        if(!e.getPlayer().isOp() && e.getPlayer().getWorld().getName().equalsIgnoreCase(Settings.DUNGEON_WORLD))
            e.getPlayer().teleport(Settings.SPAWN_LOCATION);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Lobby lobby = LobbyHandler.getPlayerLobby(e.getPlayer());
        if(lobby == null) return;
        lobby.removePlayer(e.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent e) {
        final Lobby lobby = LobbyHandler.getPlayerLobby(e.getEntity());
        if(lobby == null) return;
        e.getEntity().setHealth(e.getEntity().getMaxHealth());
        for(PotionEffect pe : e.getEntity().getActivePotionEffects()) e.getEntity().removePotionEffect(pe.getType());
        e.getEntity().teleport(lobby.getLobbySpawn());
        lobby.getActiveDungeon().addDeath();
        if (!lobby.getActiveDungeon().allPlayersDead()) {
            Messaging.send(e.getEntity(), "&aYou have died! You must wait until the end of this Stage to respawn!");
            lobby.broadcast("&b" + e.getEntity().getName() + " &ahas died! They will respawn next round... if you make it!");
        } else lobby.finish(false);
    }

    @EventHandler
    public void onSignChange(SignChangeEvent e) {
        if(!e.getLine(0).equalsIgnoreCase("[Dungeons]")) return;
        if(!e.getPlayer().hasPermission("dungeons.admin")) Messaging.send(e.getPlayer(), "&cYou do not have permission to place this sign");
        else if(!LobbyHandler.lobbyExists(e.getLine(1))) Messaging.send(e.getPlayer(), "&cThere is no Lobby with that ID!");
        else LobbyHandler.getLobby(e.getLine(1)).addLobbySign(e.getBlock().getLocation());
        if(!LobbyHandler.lobbyExists(e.getLine(1)) || !LobbyHandler.getLobby(e.getLine(1)).isLobbySign(e.getBlock().getLocation())) e.getBlock().breakNaturally();
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction() != Action.RIGHT_CLICK_BLOCK || !(e.getClickedBlock().getState() instanceof Sign)) return;
        Sign sign = (Sign) e.getClickedBlock().getState();
        if(!sign.getLine(0).equalsIgnoreCase(StringUtil.colorize("&9[Dungeon]")) && !sign.getLine(0).equalsIgnoreCase(StringUtil.colorize("&2[VIP Dungeon]"))) return;
        Lobby signLobby = LobbyHandler.getSignLobby(e.getClickedBlock().getLocation());
        if(signLobby != null) LobbyHandler.addLobbyPlayer(e.getPlayer(), signLobby);
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    public void onBlockBreak(BlockBreakEvent e) {
        if(e.isCancelled() || !e.getPlayer().isOp()) return;
        else if(e.getBlock().getType() == Settings.MOB_SPAWN_MARKER) {
            Dungeon dungeon = DungeonHandler.dungeonFromMobSpawn(e.getBlock().getLocation());
            if(dungeon == null) return;
            dungeon.stageFromMobSpawn(e.getBlock().getLocation()).removeMobSpawn(e.getBlock().getLocation());
            Messaging.send(e.getPlayer(), "&aSuccesfully removed mob spawn location!");
        } else if (e.getBlock().getState() instanceof Sign) {
            Lobby signLobby = LobbyHandler.getSignLobby(e.getBlock().getLocation());
            if(signLobby != null) {
                signLobby.removeLobbySign(e.getBlock().getLocation());
                Messaging.send(e.getPlayer(), "&aSuccesfully removed Lobby sign!");
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent e) {
        if(e.getEntity() instanceof Player || !e.getEntity().getWorld().getName().equalsIgnoreCase(Settings.DUNGEON_WORLD)) return;
        e.setDroppedExp(0);
        e.getDrops().clear();
        if(e.getEntity().getCustomName() != null && e.getEntity().getCustomName().equalsIgnoreCase(StringUtil.colorize("&cMinion"))) return;
        Stage dStage = MobHandler.getDungeonMobStage(e.getEntity().getUniqueId());
        if(dStage == null) return;
        dStage.removeMob(e.getEntity().getUniqueId());
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent e) {
        if(!e.getEntity().getWorld().getName().equalsIgnoreCase(Settings.DUNGEON_WORLD)) return;
        else if(e.getEntity() instanceof Player) {
            DungeonMob dungeonMob = MobHandler.getDungeonMob(e.getDamager());
            if (dungeonMob == null) return;
            e.setCancelled(true);
            ((Player) e.getEntity()).damage(dungeonMob.getAttackDamage());
        } else {
            if(!(e.getDamager() instanceof Player) && !(e.getDamager() instanceof Projectile)) e.setCancelled(true);
            else if(e.getDamager() instanceof Projectile && !(((Projectile) e.getDamager()).getShooter() instanceof Player)) e.setCancelled(true);
            DungeonMob dMob = MobHandler.getDungeonMob(e.getEntity());
            if(dMob != null) dMob.handleDamage();
        }
    }
}

/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import net.avonmc.dungeons.cmd.CommandHandler;
import net.avonmc.dungeons.dungeon.DungeonHandler;
import net.avonmc.dungeons.lobby.LobbyHandler;
import net.avonmc.dungeons.mob.MobHandler;
import net.avonmc.dungeons.util.Configuration;
import net.avonmc.dungeons.util.Messaging;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public class Dungeons extends JavaPlugin {

    public static Economy economy;
    public static Permission permission;
    public static Random random = new Random();

    private static Configuration configuration;
    private static WorldEditPlugin worldEditPlugin;

    private CommandHandler commandHandler;

    public void onEnable() {
        getConfiguration().loadConfigurations();
        if(!canEnable()) getServer().getPluginManager().disablePlugin(this);
        if(!isEnabled()) return;
        MobHandler.loadDungeonMobs();
        DungeonHandler.loadDungeons();
        LobbyHandler.loadLobbies();
        getCommandHandler().registerCommands();
        getServer().getPluginManager().registerEvents(new EventListener(this), this);
        getLogger().info("has been enabled");
    }

    public void onDisable() {
        LobbyHandler.handleDisable();
        DungeonHandler.saveDungeons();
        LobbyHandler.saveLobbies();
        getConfiguration().saveConfigurations();
        getLogger().info("has been disabled");
    }

    private boolean canEnable() {
        if(!getServer().getPluginManager().isPluginEnabled("Vault")) Messaging.printErr("Could not hook into Vault! Disabling...");
        else if(getEconomy() == null) Messaging.printErr("Could not hook into Economy! Disabling...");
        else if(getPermission() == null) Messaging.printErr("Could not hook into Permissions! Disabling...");
        else if(getWorldEdit() == null) Messaging.printErr("Could not hook into WorldGuard! Disabling...");
        return economy != null && permission != null && worldEditPlugin != null;
    }

    private Economy getEconomy() {
        if(economy == null && getServer().getServicesManager().getRegistration(Economy.class) != null)
            economy = getServer().getServicesManager().getRegistration(Economy.class).getProvider();
        return economy;
    }

    private Permission getPermission() {
        if(permission == null && getServer().getServicesManager().getRegistration(Permission.class) != null)
            permission = getServer().getServicesManager().getRegistration(Permission.class).getProvider();
        return permission;
    }

    public Configuration getConfiguration() {
        if(configuration == null) configuration = new Configuration(this);
        return configuration;
    }

    public static WorldEditPlugin getWorldEdit() {
        if(worldEditPlugin == null && Bukkit.getPluginManager().isPluginEnabled("WorldEdit"))
            worldEditPlugin = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        return worldEditPlugin;
    }

    public CommandHandler getCommandHandler() {
        if(commandHandler == null) commandHandler = new CommandHandler(this);
        return commandHandler;
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
        return getCommandHandler().onCommand(sender, cmd, commandLabel, args);
    }
}

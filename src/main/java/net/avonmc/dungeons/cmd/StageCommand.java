/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.cmd;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.avonmc.dungeons.dungeon.DungeonHandler;
import net.avonmc.dungeons.dungeon.Stage;
import net.avonmc.dungeons.util.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StageCommand {

    @Command(aliases = {"setspawn"}, desc = "Stage spawn setting command", usage = "<dungeonID> <stageID>", min = 2, max = 2)
    @CommandPermissions("dungeons.admin")
    public static void setStageSpawn(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!DungeonHandler.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no Dungeon with that ID!");
        else if(!DungeonHandler.getDungeon(args.getString(0)).hasStage(args.getInteger(1))) Messaging.send(sender, "&cThere is no Stage with that ID in " + args.getString(0));
        else {
            DungeonHandler.getDungeon(args.getString(0)).getStage(args.getInteger(1)).setSpawnLocation(((Player) sender).getLocation());
            Messaging.send(sender, "&aSuccesfully set Stage spawn to your location!");
        }
    }

    @Command(aliases = {"addmobspawn"}, desc = "Stage mob spawn adding command", usage = "<dungeonID> <stageID>", flags = "m", min = 2, max = 2)
    @CommandPermissions("dungeons.admin")
    public static void addMobSpawn(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!DungeonHandler.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no Dungeon with that ID!");
        else if(!DungeonHandler.getDungeon(args.getString(0)).hasStage(args.getInteger(1))) Messaging.send(sender, "&cThere is no Stage with that ID in " + args.getString(0));
        else if(!args.hasFlag('m')) {
            DungeonHandler.getDungeon(args.getString(0)).getStage(args.getInteger(1)).addMobSpawn(((Player) sender).getLocation());
            Messaging.send(sender, "&aSuccesfully added mob spawn at your location!");
        } else {
            Messaging.send(sender, "&aAdding mob spawns from your selection...");
            DungeonHandler.addMobSpawns((Player) sender, DungeonHandler.getDungeon(args.getString(0)).getStage(args.getInteger(1)));
        }
    }

    @Command(aliases = {"showmobspawns"}, desc = "Dungeon mob spawn viewing command", usage = "<dungeonID>", flags = "s:", min = 1)
    @CommandPermissions("dungeons.admin")
    public static void showMobSpawns(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!DungeonHandler.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no Dungeon with that ID!");
        else if(args.hasFlag('s') && DungeonHandler.getDungeon(args.getString(0)).hasStage(args.getInteger(1))) Messaging.send(sender, "&cThere is no Stage with that ID in " + args.getString(0));
        else if(args.hasFlag('s')) {
            DungeonHandler.getDungeon(args.getString(0)).getStage(args.getInteger(1)).showMobSpawns();
            Messaging.send(sender, "&aMarked all mob spawn locations for Stage " + args.getInteger(1));
        } else {
            for(Stage stage : DungeonHandler.getDungeon(args.getString(0)).getLoadedStages()) stage.showMobSpawns();
            Messaging.send(sender, "&aMarked all mob spawn locations for Dungeon " + args.getString(0));
        }
    }

    @Command(aliases = {"hidemobspawns"}, desc = "Dungeon mob spawn hiding command", usage = "<dungeonID>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void hideMobSpawns(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!DungeonHandler.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no Dungeon with that ID!");
        else {
            for(Stage stage : DungeonHandler.getDungeon(args.getString(0)).getLoadedStages()) stage.hideMobSpawns();
            Messaging.send(sender, "&aAll mob spawns hidden in Dungeon " + args.getString(0));
        }
    }
}

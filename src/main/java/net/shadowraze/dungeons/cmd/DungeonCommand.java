package net.shadowraze.dungeons.cmd;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.shadowraze.dungeons.dungeon.Dungeon;
import net.shadowraze.dungeons.dungeon.DungeonManager;
import net.shadowraze.dungeons.dungeon.Stage;
import net.shadowraze.dungeons.lobby.Lobby;
import net.shadowraze.dungeons.lobby.LobbyManager;
import net.shadowraze.dungeons.utils.Messaging;
import net.shadowraze.dungeons.utils.StringsUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class DungeonCommand {

    @Command(aliases = {"create"}, desc = "Dungeon creation command", usage = "<name>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void createDungeon(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(DungeonManager.createDungeon(sender, args.getString(0))) Messaging.send(sender, "&aSuccessfully created new Dungeon with ID &b" + args.getString(0));
    }

    @Command(aliases = {"remove"}, desc = "Dungeon removal command", usage = "<dungeonID>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void removeDungeon(CommandContext args, CommandSender sender) throws CommandException {
        if(DungeonManager.removeDungeon(sender, args.getString(0))) Messaging.send(sender, "&aSuccessfully removed &b" + args.getString(0));
    }

    @Command(aliases = {"list"}, desc = "Dungeon listing command", max = 0)
    @CommandPermissions("dungeons.admin")
    public static void listDungeons(CommandContext args, CommandSender sender) throws CommandException {
        List<String> activeDungeons = new ArrayList<String>();
        List<String> waitingDungeons = new ArrayList<String>();
        for(Dungeon dungeon : DungeonManager.getDungeons())
            if (dungeon.isActive()) activeDungeons.add(dungeon.getDungeonID());
            else waitingDungeons.add(dungeon.getDungeonID());
        Messaging.send(sender, "&7Fetching dungeons... \n&aACTIVE:&7 " + StringsUtil.listString(activeDungeons) + "\n&cWAITING: " + StringsUtil.listString(waitingDungeons));
    }

    @Command(aliases = {"info"}, desc = "Dungeon info command", usage = "<dungeonID>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void viewDungeonInfo(CommandContext args, CommandSender sender) throws CommandException {
        if(!DungeonManager.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no dungeon with that name!");
        else {
            Dungeon iDungeon = DungeonManager.getDungeon(args.getString(0));
            Messaging.send(sender, "&aFetching info for &b" + args.getString(0) +
                    "\n&7Lobby: &b" + (iDungeon.isActive() ? iDungeon.getLobby().getLobbyID() : "&cNONE") +
                    "\n&7Current Stage: &b" + (iDungeon.getActiveStage() == null ? "&cNONE" : iDungeon.getActiveStage().getStageID()) +
                    "\n&7Number of Stages: &b" + iDungeon.getLoadedStages().size() +
                    "\n&7Number of Spawned Mobs: &b" + iDungeon.getSpawnedMobs().size());
        }
    }

    @Command(aliases = {"addstage"}, desc = "Dungeon stage adding command", usage = "<dungeonID>", min = 1)
    @CommandPermissions("dungeons.admin")
    public static void addStage(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(DungeonManager.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no dungeon with that name!");
        else {
            Dungeon dungeon = DungeonManager.getDungeon(args.getString(0));
            dungeon.addStage(new Stage(dungeon, dungeon.getLoadedStages().size(), ((Player) sender).getLocation()));
            Messaging.send(sender, "&aSuccesfully added new stage to &b" + args.getString(0));
        }
    }

    @Command(aliases = {"delstage"}, desc = "Dungeon stage removal command", usage = "<dungeonID> <stageID>", flags = "a", min = 2)
    @CommandPermissions("dungeons.admin")
    public static void delStage(CommandContext args, CommandSender sender) throws CommandException {
        if (DungeonManager.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no dungeon with that name!");
        else {
            Dungeon dungeon = DungeonManager.getDungeon(args.getString(0));
            if(!dungeon.hasStage(args.getInteger(1))) Messaging.send(sender, "&cThere is no stage with that ID in " + args.getString(0));
            else {
                if(!args.hasFlag('a')) {
                    dungeon.removeStage(dungeon.getStage(args.getInteger(1)));
                    Messaging.send(sender, "&aSuccesfully removed stage from &b" + args.getString(0));
                } else {
                    dungeon.removeAllStages();
                    Messaging.send(sender, "&aSuccesfully removed all stages from &b" + args.getString(0));
                }
            }
        }
    }

    @Command(aliases = {"setspawn"}, desc = "Stage spawn setting command", usage = "<dungeonID> <stageID>", min = 2, max = 2)
    @CommandPermissions("dungeons.admin")
    public static void setStageSpawn(CommandContext args, CommandSender sender) throws CommandException {
        if (DungeonManager.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no dungeon with that name!");
        else {
            Dungeon dungeon = DungeonManager.getDungeon(args.getString(0));
            if(!dungeon.hasStage(args.getInteger(1))) Messaging.send(sender, "&cThere is no stage with that ID in " + args.getString(0));
            else {
                dungeon.getStage(args.getInteger(1)).setSpawnLocation(((Player) sender).getLocation());
                Messaging.send(sender, "&aSuccessfully set spawnpoint to your location for stage &b" + args.getInteger(1) + "&a in dungeon &b" + args.getString(0));
            }
        }
    }

    @Command(aliases = {"addspawn"}, desc = "Stage mob spawn addition command", usage = "<dungeonID> <stageID>", min = 2, max = 2)
    @CommandPermissions("dungeons.admin")
    public static void addMobSpawn(CommandContext args, CommandSender sender) throws CommandException {
        if (DungeonManager.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no dungeon with that name!");
        else {
            Dungeon dungeon = DungeonManager.getDungeon(args.getString(0));
            if(!dungeon.hasStage(args.getInteger(1))) Messaging.send(sender, "&cThere is no stage with that ID in " + args.getString(0));
            else {
                dungeon.getStage(args.getInteger(1)).addMobSpawn(((Player) sender).getLocation());
                Messaging.send(sender, "&aSuccessfully add mob spawn at your location for stage &b" + args.getInteger(1) + "&a in dungeon &b" + args.getString(0));
            }
        }
    }
}

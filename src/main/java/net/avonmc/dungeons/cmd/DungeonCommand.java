/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package net.avonmc.dungeons.cmd;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.avonmc.dungeons.dungeon.Dungeon;
import net.avonmc.dungeons.dungeon.DungeonHandler;
import net.avonmc.dungeons.dungeon.Stage;
import net.avonmc.dungeons.lobby.LobbyHandler;
import net.avonmc.dungeons.util.Messaging;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DungeonCommand {

    @Command(aliases = {"create"}, desc = "Dungeon creation command", usage = "<ID> [-d <displayName>]", flags = "d:v", min = 1)
    @CommandPermissions("dungeons.admin")
    public static void createDungeon(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(DungeonHandler.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is already a Dungeon with that ID!");
        else {
            DungeonHandler.createDungeon(args.getString(0), args.hasFlag('d') ? args.getFlag('d') : args.getString(0), args.hasFlag('v'));
            Messaging.send(sender, "&aSuccessfully created new Dungeon with ID &b" + args.getString(0));
        }
    }

    @Command(aliases = {"remove"}, desc = "Dungeon removal command", usage = "<ID>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void removeDungeon(CommandContext args, CommandSender sender) throws CommandException {
        if(!DungeonHandler.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no Dungeon with that ID!");
        else {
            DungeonHandler.removeDungeon(args.getString(0));
            Messaging.send(sender, "&aSuccessfully removed Dungeon with ID &b" + args.getString(0));
        }
    }

    @Command(aliases = {"list"}, desc = "Dungeon listing command", max = 0)
    @CommandPermissions("dungeons.admin")
    public static void listDungeons(CommandContext args, CommandSender sender) throws CommandException {
        String dungeonList = StringUtil.colorize("&9===  &3Dungeons  &9===");
        for(String dungeonID : DungeonHandler.getDungeons().keySet())
            dungeonList += StringUtil.colorize("\n&8[" + DungeonHandler.getDungeon(dungeonID).getDisplayName() + "&8]&7 " + dungeonID);
        dungeonList += StringUtil.colorize("\n&9===  ===  ===  ===");
        Messaging.send(sender, dungeonList);
    }

    @Command(aliases = {"info"}, desc = "Dungeon info command", usage = "<ID>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void viewDungeonInfo(CommandContext args, CommandSender sender) throws CommandException {
        if(!DungeonHandler.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no Dungeon with that ID!");
        else {
            Dungeon dungeon = DungeonHandler.getDungeon(args.getString(0));
            Messaging.send(sender, StringUtil.colorize("&7Current Settings for Dungeon: " + args.getString(0) +
                    "\n&7Display Name: " + dungeon.getDisplayName() +
                    "\n&7VIP: &b" + dungeon.isVIP() +
                    "\n&7Active Lobby: &b" + (dungeon.getActiveLobby() == null ? "NONE" : LobbyHandler.getLobbyID(dungeon.getActiveLobby())) +
                    "\n&7Active Stage: &b") + (dungeon.getActiveStage() == null ? "NONE" : dungeon.getStageID(dungeon.getActiveStage())));
        }
    }

    @Command(aliases = {"addstage"}, desc = "Dungeon stage adding command", usage = "<dungeonID>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void addDungeonStage(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!DungeonHandler.dungeonExists(args.getString(0))) Messaging.send(sender, "&cThere is no Dungeon with that ID!");
        else {
            Dungeon dungeon = DungeonHandler.getDungeon(args.getString(0));
            dungeon.addStage(new Stage(dungeon, ((Player) sender).getLocation()));
            Messaging.send(sender, "&aSuccessfully added Stage &b" + dungeon.getLoadedStages().size() + "&a to &b" + dungeon.getDisplayName());
        }
    }
}

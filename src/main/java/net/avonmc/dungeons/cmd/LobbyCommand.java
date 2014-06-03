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
import net.avonmc.dungeons.game.GameStage;
import net.avonmc.dungeons.lobby.Lobby;
import net.avonmc.dungeons.lobby.LobbyHandler;
import net.avonmc.dungeons.lobby.type.VIP;
import net.avonmc.dungeons.util.Messaging;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LobbyCommand {

    @Command(aliases = {"create"}, desc = "Lobby creation command", usage = "<ID>", flags = "v", min = 1)
    @CommandPermissions("dungeons.admin")
    public static void createLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(LobbyHandler.lobbyExists(args.getString(0))) Messaging.send(sender, "&cThere is already a Lobby with that ID!");
        else {
            LobbyHandler.createLobby(args.getString(0), args.hasFlag('v'), ((Player) sender).getLocation());
            Messaging.send(sender, "&aSuccessfully created new " + (args.hasFlag('v') ? "VIP" : "") + " Lobby with ID &b" + args.getString(0));
        }
    }

    @Command(aliases = {"remove"}, desc = "Lobby removal command", usage = "<ID>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void removeLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!LobbyHandler.lobbyExists(args.getString(0))) Messaging.send(sender, "&cThere is no Lobby with that ID!");
        else {
            LobbyHandler.removeLobby(args.getString(0));
            Messaging.send(sender, "&aSuccessfully removed Lobby with ID &b" + args.getString(0));
        }
    }

    @Command(aliases = {"list"}, desc = "Lobby listing command", max = 0)
    @CommandPermissions("dungeons.admin")
    public static void listLobbies(CommandContext args, CommandSender sender) throws CommandException {
        String lobbyList = StringUtil.colorize("&9===  &6Lobbies  &9===");
        for(String lobbyID : LobbyHandler.getLobbies().keySet())
            lobbyList += StringUtil.colorize("\n&8[" + LobbyHandler.getLobby(lobbyID).getStage().toString() + "&8]&7 " + lobbyID);
        lobbyList += StringUtil.colorize("\n&9===  ===  ===  ===");
        Messaging.send(sender, lobbyList);
    }

    @Command(aliases = {"info"}, desc = "Lobby info command", usage = "<ID>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void viewLobbyInfo(CommandContext args, CommandSender sender) throws CommandException {
        if(!LobbyHandler.lobbyExists(args.getString(0))) Messaging.send(sender, "&cThere is no Lobby with that ID!");
        else {
            Lobby lobby = LobbyHandler.getLobby(args.getString(0));
            Messaging.send(sender, StringUtil.colorize("&7Current Settings for Lobby: " + args.getString(0) +
                    "\n&7VIP: &b" + lobby.isVIP() +
                    "\n&7Status: " + lobby.getStage().toString() +
                    "\n&7Max Players: &b" + lobby.getMaxPlayers() +
                    "\n&7Spawn Location: &b" + StringUtil.parseLoc(lobby.getLobbySpawn()) +
                    "\n&7Active Dungeon: &b" + (lobby.getActiveDungeon() == null ? "NONE" : lobby.getActiveDungeon().getDisplayName())));
        }
    }

    @Command(aliases = {"setspawn"}, desc = "Lobby spawn setting command", usage = "<ID>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void setLobbySpawn(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!LobbyHandler.lobbyExists(args.getString(0))) Messaging.send(sender, "&cThere is no Lobby with that ID!");
        else {
            LobbyHandler.getLobby(args.getString(0)).setLobbySpawn(((Player) sender).getLocation());
            Messaging.send(sender, "&aSuccessfully set Lobby spawn to your current location!");
        }
    }

    @Command(aliases = {"join"}, desc = "Lobby joining command", usage = "<ID>", min = 1, max = 1)
    public static void joinLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!LobbyHandler.lobbyExists(args.getString(0))) Messaging.send(sender, "&cThere is no Lobby with that ID!");
        else LobbyHandler.addLobbyPlayer((Player) sender, LobbyHandler.getLobby(args.getString(0)));
    }

    @Command(aliases = {"leave"}, desc = "Lobby leaving command", max = 0)
    public static void leaveLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else LobbyHandler.removeLobbyPlayer((Player) sender);
    }

    @Command(aliases = {"invite"}, desc = "Lobby invitation command", usage = "<player>", min = 1, max = 1)
    @CommandPermissions("dungeons.vip")
    public static void inviteToLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else LobbyHandler.inviteLobbyPlayer((Player) sender, args.getString(0));
    }

    @Command(aliases = {"start"}, desc = "Lobby force-starting command", max = 0)
    @CommandPermissions("dungeons.vip")
    public static void startLobby(CommandContext args, CommandSender sender) throws CommandException {
        Lobby lobby = sender instanceof Player ? LobbyHandler.getPlayerLobby((Player) sender) : null;
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(lobby == null) Messaging.send(sender, "&cYou are not currently in a Lobby!");
        else if(!lobby.isVIP()) Messaging.send(sender, "&cSorry, this command can only be used in a VIP Lobby!");
        else if(!((VIP) lobby).isLeader((Player) sender)) Messaging.send(sender, "&cSorry, only the Lobby leader can force-start the game!");
        else if(lobby.getStage() != GameStage.WAITING) Messaging.send(sender, "&cYour Lobby is already starting!");
        else lobby.setStage(GameStage.FORCE_STARTING);
    }

    @Command(aliases = {"continue"}, desc = "Lobby continuing command", max = 0)
    public static void continueLobby(CommandContext args, CommandSender sender) throws CommandException {
        Lobby lobby = sender instanceof Player ? LobbyHandler.getPlayerLobby((Player) sender) : null;
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(lobby == null) Messaging.send(sender, "&cYou are not currently in a Lobby!");
        else if(lobby.getStage() != GameStage.CONTINUING) Messaging.send(sender, "&cYou cannot use this command right now!");
        else lobby.addContinueVote((Player) sender);
    }
}

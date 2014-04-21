package net.shadowraze.dungeons.cmd;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandException;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.shadowraze.dungeons.lobby.Lobby;
import net.shadowraze.dungeons.lobby.LobbyManager;
import net.shadowraze.dungeons.utils.Configuration;
import net.shadowraze.dungeons.utils.Messaging;
import net.shadowraze.dungeons.utils.StringsUtil;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class LobbyCommand {

    @Command(aliases = {"create"}, desc = "Lobby creation command", usage = "<name>", flags = "vm:", min = 1)
    @CommandPermissions("dungeons.admin")
    public static void createLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(LobbyManager.createLobby(sender, args.getString(0), args.hasFlag('v'), args.hasFlag('m') ? args.getFlagInteger('m') : Configuration.DEFAULT_MAX_PLAYERS, ((Player) sender).getLocation()))
            Messaging.send(sender, "&aSuccesfully created new lobby with ID &b" + args.getString(0));
    }

    @Command(aliases = {"remove"}, desc = "Lobby removal command", usage = "<name>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void removeLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(LobbyManager.removeLobby(sender, args.getString(0))) Messaging.send(sender, "&aSuccessfully removed &b" + args.getString(0));
    }

    @Command(aliases = {"setspawn"}, desc = "Lobby spawn setting command", usage = "<name>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void setSpawn(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!LobbyManager.lobbyExists(args.getString(0))) Messaging.send(sender, "&cThere is no lobby with that name!");
        else {
            LobbyManager.getLobby(args.getString(0)).setSpawnLocation(((Player) sender).getLocation());
            Messaging.send(sender, "&aSuccessfully set spawnpoint to your location for lobby &b" + args.getString(0));
        }
    }

    @Command(aliases = {"list"}, desc = "Lobby listing command", max = 0)
    @CommandPermissions("dungeons.admin")
    public static void listLobbies(CommandContext args, CommandSender sender) throws CommandException {
        List<String> activeLobbies = new ArrayList<String>();
        List<String> waitingLobbies = new ArrayList<String>();
        for(Lobby lobby : LobbyManager.getLobbies())
            if (lobby.isInProgress()) activeLobbies.add(lobby.getLobbyID());
            else waitingLobbies.add(lobby.getLobbyID());
        Messaging.send(sender, "&7Fetching lobbies... \n&aACTIVE:&7 " + StringsUtil.listString(activeLobbies) + "\n&cWAITING: " + StringsUtil.listString(waitingLobbies));
    }

    @Command(aliases = {"info"}, desc = "Lobby info command", usage = "<name>", min = 1, max = 1)
    @CommandPermissions("dungeons.admin")
    public static void viewLobbyInfo(CommandContext args, CommandSender sender) throws CommandException {
        if(!LobbyManager.lobbyExists(args.getString(0))) Messaging.send(sender, "&cThere is no lobby with that name!");
        else {
            Lobby iLobby = LobbyManager.getLobby(args.getString(0));
            Messaging.send(sender, "&aFetching info for &b" + args.getString(0) +
                    "\n&7Status: " + (iLobby.isInProgress() ? "&cIN PROGRESS" : "&aWAITING") +
                    "\n&7Max Players: &b" + iLobby.getMaxPlayers() +
                    "\n&7Spawn Location: &b" + StringsUtil.parseLoc(iLobby.getSpawnLocation()) +
                    "\n&7Active Dungeon: &b" + (iLobby.getActiveDungeon() == null ? "&cNONE" : iLobby.getActiveDungeon().getDungeonID()));
        }
    }

    @Command(aliases = {"join"}, desc = "Lobby joining command", usage = "<name>", min = 1, max = 1)
    public static void joinLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!LobbyManager.lobbyExists(args.getString(0))) Messaging.send(sender, "&cThere is no lobby with that name!");
        else if(LobbyManager.getLobby(args.getString(0)).getActiveDungeon() == null) Messaging.send(sender, "&cThis lobby has no Dungeon and cannot be joined!");
        else if(LobbyManager.getLobby(((Player) sender).getUniqueId()) != null) Messaging.send(sender, "&cYou are already in a lobby! Type &6/lobby leave&c to leave");
        else if(LobbyManager.getLobby(args.getString(0)).addPlayer((Player) sender)) ((Player) sender).teleport(LobbyManager.getLobby(args.getString(0)).getSpawnLocation());
    }

    @Command(aliases = {"leave"}, desc = "Lobby leaving command", max = 0)
    public static void leaveLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(LobbyManager.getLobby(((Player) sender).getUniqueId()) == null) Messaging.send(sender, "&cYou are not currently in a lobby!");
        else if(LobbyManager.getLobby(((Player) sender).getUniqueId()).isInProgress()) Messaging.send(sender, "&cYou cannot leave once the lobby has started!");
        else LobbyManager.getLobby(((Player) sender).getUniqueId()).removePlayer((Player) sender);
    }

    @Command(aliases = {"start"}, desc = "Lobby force-start command", max = 0)
    @CommandPermissions("dungeons.vip")
    public static void startVIPLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(LobbyManager.getLobby(((Player) sender).getUniqueId()) == null) Messaging.send(sender, "&cYou are not currently in a lobby!");
        else {
            Lobby inLobby = LobbyManager.getLobby(((Player) sender).getUniqueId());
            if(!inLobby.isVIP()) Messaging.send(sender, "&cSorry, this command can only be used in VIP lobbies!");
            else if(!inLobby.getLeader().equalsIgnoreCase(sender.getName())) Messaging.send(sender, "&cSorry, only the lobby leader can start the lobby!");
            else if(inLobby.isStarting() || inLobby.isInProgress()) Messaging.send(sender, "&cYour lobby is already " + (inLobby.isStarting() ? "starting" : "started") + "!");
            else inLobby.tryStart(true);
        }
    }

    @Command(aliases = {"continue"}, desc = "Lobby progression command", max = 0)
    public static void continueLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(LobbyManager.getLobby(((Player) sender).getUniqueId()) == null) Messaging.send(sender, "&cYou are not currently in a lobby!");
        else {
            Lobby inLobby = LobbyManager.getLobby(((Player) sender).getUniqueId());
            if(inLobby.getActiveDungeon().getActiveStage() == null || !inLobby.getActiveDungeon().getActiveStage().isFinished()) Messaging.send(sender, "&cYou cannot use this command right now!");
            else inLobby.getActiveDungeon().getActiveStage().addContinueVote(sender);
        }
    }

    @Command(aliases = {"respawn"}, desc = "Lobby respawning command", max = 0)
    public static void respawnInLobby(CommandContext args, CommandSender sender) throws CommandException {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(LobbyManager.getLobby(((Player) sender).getUniqueId()) == null) Messaging.send(sender, "&cYou are not currently in a lobby!");
        else {
            Lobby inLobby = LobbyManager.getLobby(((Player) sender).getUniqueId());
            if(!inLobby.isInProgress() || inLobby.getActiveDungeon() == null) Messaging.send(sender, "&cYou cannot use this command right now!");
            else if(!inLobby.getActiveDungeon().isPlayerDead(((Player) sender).getUniqueId())) Messaging.send(sender, "&cYou can't use this command unless you're dead!");
            else if(inLobby.getActiveDungeon().respawnDeadPlayer((Player) sender)) ((Player) sender).teleport(inLobby.getActiveDungeon().getActiveStage().getSpawnLocation());
        }
    }
}

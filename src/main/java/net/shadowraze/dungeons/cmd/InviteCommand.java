package net.shadowraze.dungeons.cmd;

import com.sk89q.minecraft.util.commands.Command;
import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.minecraft.util.commands.CommandPermissions;
import net.shadowraze.dungeons.lobby.Lobby;
import net.shadowraze.dungeons.lobby.LobbyManager;
import net.shadowraze.dungeons.utils.Messaging;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InviteCommand {

    @Command(aliases = {"send"}, desc = "Player invitation command", usage = "<player>", min = 1, max = 1)
    @CommandPermissions("dungeons.vip")
    public static void sendInvite(CommandContext args, CommandSender sender) {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(!LobbyManager.isInLobby(((Player) sender).getUniqueId())) Messaging.send(sender, "&cYou are not in a lobby!");
        else {
            Lobby lobby = LobbyManager.getLobby(((Player) sender).getUniqueId());
            if(!lobby.isVIP()) Messaging.send(sender, "&cSorry, this command can only be used in VIP lobbies!");
            else if(!lobby.getLeader().equalsIgnoreCase(sender.getName())) Messaging.send(sender, "&cSorry, only the lobby leader can send invites!");
            else if(LobbyManager.invitePlayer(sender, args.getString(0), lobby)) Messaging.send(sender, "&aSuccessfully sent invite to &b" + args.getString(0));
        }
    }

    @Command(aliases = {"accept"}, desc = "Invitation acceptance command", max = 0)
    public static void acceptInvite(CommandContext args, CommandSender sender) {
        if(!(sender instanceof Player)) Messaging.send(sender, "&cThis command cannot be run from the console!");
        else if(LobbyManager.getLobby(((Player) sender).getUniqueId()) != null) Messaging.send(sender, "&cYou are already in a lobby! Type &6/lobby leave&c to leave");
        else if(LobbyManager.acceptInvite((Player) sender)) ((Player) sender).teleport(LobbyManager.getLobby(((Player) sender).getUniqueId()).getSpawnLocation());
    }
}

package net.shadowraze.dungeons.cmd;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import net.shadowraze.dungeons.Dungeons;
import net.shadowraze.dungeons.utils.Messaging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class RootCommands {

    @Command(aliases = {"lobby"}, desc = "Lobby management command")
    @NestedCommand(LobbyCommand.class)
    public static void lobbyComand(CommandContext args, CommandSender sender) {}

    @Command(aliases = {"dungeon"}, desc = "Dungeon management command")
    @NestedCommand(DungeonCommand.class)
    public static void dungeonCommand(CommandContext args, CommandSender sender) {}

    @Command(aliases = {"invite"}, desc = "Invitation management command")
    @NestedCommand(InviteCommand.class)
    public static void inviteCommand(CommandContext args, CommandSender sender) {}
}

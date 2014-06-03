/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package net.avonmc.dungeons.cmd;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import net.avonmc.dungeons.Dungeons;
import net.avonmc.dungeons.util.Messaging;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CommandHandler {

    private Dungeons plugin;
    private CommandsManager<CommandSender> commands;

    public CommandHandler(Dungeons plugin) {
        this.plugin = plugin;
    }

    public void registerCommands() {
        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String s) {
                return sender.isOp() || sender.hasPermission(s);
            }
        };
        CommandsManagerRegistration reg = new CommandsManagerRegistration(plugin, commands);
        reg.register(CommandHandler.class);
        Messaging.printInfo("Commands successfully registered! Commands: " + commands.getCommands().keySet());
    }

    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String commandLabel, String[] args) {
        try {
            commands.execute(cmd.getName(), args, sender, sender);
        } catch (CommandPermissionsException e) {
            Messaging.send(sender, "&cYou do not have permission to use this command!");
        } catch (MissingNestedCommandException e) {
            Messaging.send(sender, ChatColor.RED + e.getUsage());
        } catch (CommandUsageException e) {
            Messaging.send(sender, ChatColor.RED + e.getMessage());
            Messaging.send(sender, ChatColor.RED + e.getUsage());
        } catch (WrappedCommandException e) {
            if (e.getCause() instanceof NumberFormatException) {
                Messaging.send(sender, "&cNumber expected, string received instead.");
            } else {
                Messaging.send(sender, "&cAn error has occurred. See console.");
                e.printStackTrace();
            }
        } catch (CommandException e) {
            Messaging.send(sender, ChatColor.RED + e.getMessage());
        }
        return true;
    }

    /*@Command(aliases = {"dungeons"}, desc = "Main plugin command")
    @CommandPermissions("dungeons.admin")
    @NestedCommand(DungeonsCommand.class)
    public static void dungeonsCommand(CommandContext args, CommandSender sender) {}*/ //TODO

    @Command(aliases = {"lobby"}, desc = "Lobby management command")
    @NestedCommand(LobbyCommand.class)
    public static void lobbyCommand(CommandContext args, CommandSender sender) {}

    @Command(aliases = {"dungeon"}, desc = "Dungeon management command")
    @CommandPermissions("dungeons.admin")
    @NestedCommand(DungeonCommand.class)
    public static void dungeonCommand(CommandContext args, CommandSender sender) {}

    @Command(aliases = {"stage"}, desc = "Stage management command")
    @CommandPermissions("dungeons.admin")
    @NestedCommand(StageCommand.class)
    public static void stageCommand(CommandContext args, CommandSender sender) {}

}

package net.shadowraze.dungeons;

import com.sk89q.bukkit.util.CommandsManagerRegistration;
import com.sk89q.minecraft.util.commands.*;
import net.shadowraze.dungeons.cmd.RootCommands;
import net.shadowraze.dungeons.dungeon.DungeonManager;
import net.shadowraze.dungeons.lobby.LobbyManager;
import net.shadowraze.dungeons.utils.Configuration;
import net.shadowraze.dungeons.utils.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class Dungeons extends JavaPlugin {

    private CommandsManager<CommandSender> commands;

    public void onEnable() {
        Configuration.loadConfigurations(this);
        DungeonManager.loadDungeons();
        LobbyManager.loadLobbies();

        commands = new CommandsManager<CommandSender>() {
            @Override
            public boolean hasPermission(CommandSender sender, String s) {
                return sender.hasPermission(s);
            }
        };
        CommandsManagerRegistration reg = new CommandsManagerRegistration(this, commands);
        reg.register(RootCommands.class);

        getServer().getPluginManager().registerEvents(new DungeonListener(), this);
        getLogger().info("has been enabled");
    }

    public void onDisable() {
        DungeonManager.saveDungeons();
        LobbyManager.saveLobbies();
        Configuration.saveConfigurations();
        getLogger().info("has been disabled");
    }

    public static Dungeons getPlugin() {
        return (Dungeons) Bukkit.getPluginManager().getPlugin("Dungeons");
    }

    @Override
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
}

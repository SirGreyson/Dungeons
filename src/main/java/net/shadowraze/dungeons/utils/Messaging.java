package net.shadowraze.dungeons.utils;

import com.google.common.base.Strings;
import net.shadowraze.dungeons.Dungeons;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.lang.reflect.Field;

public class Messaging {

    private static String PREFIX = "&9[Dungeons]&r ";

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(StringsUtil.colorize(PREFIX + message));
    }

    public static void broadcast(String message) {
        Bukkit.getServer().broadcastMessage(StringsUtil.colorize(PREFIX + message));
    }

    public static void printErr(String message) {
        Dungeons.getPlugin().getLogger().severe(ChatColor.RED + message);
    }
}

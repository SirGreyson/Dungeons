/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.util;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.logging.Logger;

public class Messaging {

    private static Logger log = Logger.getLogger("Dungeons");
    private static String prefix = Settings.PREFIX;
    private static String broadcastPrefix = Settings.BROADCAST_PREFIX;

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(StringUtil.colorize(prefix + message));
    }

    public static void broadcast(String message) {
        Bukkit.getServer().broadcastMessage(StringUtil.colorize(broadcastPrefix + message));
    }

    public static void printInfo(String message) {
        log.info("§e" + message);
    }

    public static void printErr(String message) {
        log.severe("§c" + message);
    }
}
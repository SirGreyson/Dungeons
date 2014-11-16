/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StringUtil {

    public static String colorize(String original) {
        return ChatColor.translateAlternateColorCodes('&', original);
    }

    public static int asInt(String intString) {
        return Integer.parseInt(intString);
    }

    public static byte asByte(String byteString) { return Byte.valueOf(byteString); }

    public static String parseLoc(Location loc) {
        return loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "," + loc.getWorld().getName();
    }

    public static Location parseLocString(String locString) {
        String coords[] = locString.split(",");
        return new Location(Bukkit.getWorld(coords[3]), asInt(coords[0]), asInt(coords[1]), asInt(coords[2]));
    }

    public static List<String> parseLocList(Set<Location> locList) {
        List<String> output = new ArrayList<String>();
        for(Location loc : locList) output.add(parseLoc(loc));
        return output;
    }

    public static Set<Location> parseLocStringList(List<String> locStringList) {
        Set<Location> output = new HashSet<Location>();
        for(String locString : locStringList) output.add(parseLocString(locString));
        return output;
    }

    public static ItemStack parseItemStack(String stackString) {
        if(stackString.equalsIgnoreCase("NONE")) return new ItemStack(Material.AIR);
        String[] vars = stackString.split(":");
        return new ItemStack(Material.valueOf(vars[0]), vars.length >= 2 ? asInt(vars[1]) : 1, (short) 0, vars.length >= 3 ? asByte(vars[2]) : 0);
    }

    public static ItemStack[] parseItemStackList(List<String> stackList) {
        List<ItemStack> output = new ArrayList<ItemStack>();
        for(String itemStack : stackList) output.add(parseItemStack(itemStack));
        return output.toArray(new ItemStack[output.size()]);
    }

    public static PotionEffect parsePotionEffect(String effectString) {
        String[] vars = effectString.split(":");
        return new PotionEffect(PotionEffectType.getByName(vars[0]), Integer.MAX_VALUE, vars.length >= 2 ? asInt(vars[1]) : 0);
    }

    public static List<PotionEffect> parsePotionEffectList(List<String> effectList) {
        List<PotionEffect> output = new ArrayList<PotionEffect>();
        for(String potionEffect : effectList) output.add(parsePotionEffect(potionEffect));
        return output;
    }
}

package net.shadowraze.dungeons.utils;

import com.avaje.ebeaninternal.server.deploy.BeanDescriptor;
import net.shadowraze.dungeons.dungeon.Dungeon;
import net.shadowraze.dungeons.dungeon.DungeonManager;
import net.shadowraze.dungeons.dungeon.DungeonMob;
import net.shadowraze.dungeons.lobby.Lobby;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class StringsUtil {

    public static String colorize(String original) {
        return ChatColor.translateAlternateColorCodes('&', original);
    }

    public static int asInt(String intString) {
        return Integer.parseInt(intString);
    }

    public static String listString(List<String> list) {
        return list.toString().substring(list.toString().indexOf("[") + 1, list.toString().indexOf("]"));
    }

    public static String parseLobbyVars(String lobbyString, Lobby lobby) {
        return colorize(lobbyString.replace("%dungeon%", lobby.getActiveDungeon() != null ? lobby.getActiveDungeon().getDungeonID() : "&cNONE")
                .replace("%playercount%", String.valueOf(lobby.getPlayers().size()))
                .replace("%maxplayers%", String.valueOf(lobby.getMaxPlayers()))
                .replace("%status%", lobby.isInProgress() ? "&cIN PROGRESS" : "&aWAITING")
                .replace("%leader%", lobby.getLeader()));
    }

    public static String parseLoc(Location loc) {
        return loc.getBlockX() + "," + loc.getBlockY() + "," + loc.getBlockZ() + "," + loc.getWorld().getName();
    }

    public static Location parseLocString(String locString) {
        String coords[] = locString.split(",");
        return new Location(Bukkit.getWorld(coords[3]), asInt(coords[0]), asInt(coords[1]), asInt(coords[2]));
    }

    public static List<String> parseLocList(List<Location> locList) {
        List<String> parsedList = new ArrayList<String>();
        for(Location location : locList) parsedList.add(parseLoc(location));
        return parsedList;
    }

    public static List<Location> parseLocStringList(List<String> locList) {
        List<Location> parsedList = new ArrayList<Location>();
        for(String location : locList) parsedList.add(parseLocString(location));
        return parsedList;
    }

    public static List<PotionEffect> parsePotionEffectStrings(List<String> potionEffects) {
        List<PotionEffect> parsedList = new ArrayList<PotionEffect>();
        for(String potionEffect : potionEffects) {
            String[] args = potionEffect.split(":");
            if(PotionEffectType.getByName(args[0]) != null) parsedList.add(PotionEffectType.getByName(args[0]).createEffect(Integer.MAX_VALUE, args.length == 1 ? 0 : asInt(args[1])));
            else Messaging.printErr("Error! Tried to load invalid PotionEffect: " + potionEffect);
        } return parsedList;
    }

    public static List<String> parseDungeonMobs(List<DungeonMob> dungeonMobs) {
        List<String> parsedList = new ArrayList<String>();
        for(DungeonMob dungeonMob : dungeonMobs) parsedList.add(dungeonMob.getMobID());
        return parsedList;
    }

    public static List<DungeonMob> parseDungeonMobStrings(List<String> dungeonMobs) {
        List<DungeonMob> parsedList = new ArrayList<DungeonMob>();
        for(String mobID : dungeonMobs) {
            if(DungeonManager.getDungeonMob(mobID) != null) parsedList.add(DungeonManager.getDungeonMob(mobID));
            else if(EntityType.valueOf(mobID) != null) parsedList.add(new DungeonMob(mobID, EntityType.valueOf(mobID)));
            else Messaging.printErr("Error! Tried to load invalid DungeonMob: " + mobID);
        } return parsedList;
    }

    public static List<String> parseDungeons(List<Dungeon> dungeons) {
        List<String> parsedList = new ArrayList<String>();
        for(Dungeon dungeon : dungeons) parsedList.add(dungeon.getDungeonID());
        return parsedList;
    }

    public static List<Dungeon> parseDungeonStrings(List<String> dungeons) {
        List<Dungeon> parsedList = new ArrayList<Dungeon>();
        for(String dungeonID : dungeons) {
            if(DungeonManager.getDungeon(dungeonID) != null) parsedList.add(DungeonManager.getDungeon(dungeonID));
            else Messaging.printErr("Error! Tried to load invalid Dungeon: " + dungeonID);
        } return parsedList;
    }
}

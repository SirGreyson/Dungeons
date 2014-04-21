package net.shadowraze.dungeons.lobby;

import net.shadowraze.dungeons.utils.Configuration;
import net.shadowraze.dungeons.utils.Messaging;
import net.shadowraze.dungeons.utils.StringsUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;

public class LobbyManager {

    private static Map<String, Lobby> loadedLobbies = new TreeMap<String, Lobby>(String.CASE_INSENSITIVE_ORDER);
    private static Map<UUID, Lobby> lobbyInvites = new HashMap<UUID, Lobby>();

    public static void loadLobbies() {
        YamlConfiguration lobbyC = Configuration.getConfig("lobbies");
        for(String lobbyType : lobbyC.getKeys(false))
            for(String lobby : lobbyC.getConfigurationSection(lobbyType).getKeys(false)) {
                String path = lobbyType + "." + lobby;
                loadedLobbies.put(lobby, new Lobby(lobby, lobbyType.equalsIgnoreCase("vip"), lobbyC.getInt(path + ".maxPlayers"), StringsUtil.parseLocString(lobbyC.getString(path + ".spawnLocation")),
                        StringsUtil.parseDungeonStrings(lobbyC.getStringList(path + ".dungeons")), StringsUtil.parseLocStringList(lobbyC.getStringList(path + ".lobbySigns"))));
            }
    }

    public static void saveLobbies() {
        for(Lobby lobby : loadedLobbies.values()) lobby.save();
    }

    public static List<Lobby> getLobbies() {
        return new ArrayList<Lobby>(loadedLobbies.values());
    }

    public static boolean lobbyExists(String lobbyID) {
        return getLobby(lobbyID) != null;
    }

    public static Lobby getLobby(String lobbyID) {
        return loadedLobbies.get(lobbyID);
    }

    public static Lobby getLobby(UUID player) {
        for(Lobby lobby : loadedLobbies.values())
            if(lobby.hasPlayer(player)) return lobby;
        return null;
    }

    public static boolean isInLobby(UUID player) {
        return getLobby(player) != null;
    }

    public static Lobby getSignLobby(Location signLoc) {
        for(Lobby lobby : loadedLobbies.values())
            if(lobby.getLobbySigns().contains(signLoc)) return lobby;
        return null;
    }

    public static boolean createLobby(CommandSender sender, String lobbyID, boolean isVIP, int maxPlayers, Location spawnLoc) {
        if(lobbyExists(lobbyID)) Messaging.send(sender, "&cThere is already a lobby with that name!");
        else {
            loadedLobbies.put(lobbyID, new Lobby(lobbyID, isVIP, maxPlayers, spawnLoc));
            loadedLobbies.get(lobbyID).save();
            return true;
        }
        return false;
    }

    public static boolean removeLobby(CommandSender sender, String lobbyID) {
        if(!lobbyExists(lobbyID)) Messaging.send(sender, "&cThere is no lobby with that name!");
        else {
            loadedLobbies.remove(lobbyID);
            Configuration.getConfig("lobbies").set((getLobby(lobbyID).isVIP() ? "vip." : "default.") + lobbyID.toLowerCase(), null);
        }
        return !loadedLobbies.containsKey(lobbyID);
    }

    public static boolean hasInvite(UUID player, String lobbyID) {
        return lobbyInvites.containsKey(player) && lobbyInvites.get(player).getLobbyID().equalsIgnoreCase(lobbyID);
    }

    public static Map<UUID, Lobby> getLobbyInvites() {
        return lobbyInvites;
    }

    public static Lobby getInvitedLobby(UUID player) {
        return lobbyInvites.get(player);
    }

    public static boolean invitePlayer(CommandSender lobbyLeader, String playerName, Lobby lobby) {
        if (Bukkit.getPlayer(playerName) == null) {
            Messaging.send(lobbyLeader, "&cThere is no online player with that name!");
            return false;
        }

        Player player = Bukkit.getPlayer(playerName);
        if(isInLobby(player.getUniqueId())) Messaging.send(lobbyLeader, "&cSorry, that player is already in a lobby.");
        else if(lobbyInvites.containsKey(player.getUniqueId()) && getInvitedLobby(player.getUniqueId()).getLobbyID().equalsIgnoreCase(lobby.getLobbyID())) Messaging.send(lobbyLeader, "&cThat player already has a pending invite to this lobby!");
        else {
            lobbyInvites.put(player.getUniqueId(), lobby);
            Messaging.send(player, "&b" + lobbyLeader.getName() + "&a has invited you to join their lobby! Type &b/lobby join " + lobby.getLobbyID() + "&a to accept.");
            lobby.broadcast("&aLobby leader invited &b" + player.getName() + "&a to join the lobby!");
        }
        return lobbyInvites.containsKey(player.getUniqueId()) && getInvitedLobby(player.getUniqueId()).getLobbyID().equalsIgnoreCase(lobby.getLobbyID());
    }

    public static boolean acceptInvite(Player player) {
        if(!lobbyInvites.containsKey(player.getUniqueId())) Messaging.send(player, "&cYou do not have any pending invites!");
        else if(lobbyInvites.get(player.getUniqueId()).addPlayer(player)) lobbyInvites.remove(player.getUniqueId());
        return !lobbyInvites.containsKey(player.getUniqueId());
    }
}

/*
 * Copyright (c) 2014 ReasonDev
 * All rights reserved.
 */

package net.avonmc.dungeons.game;

import org.bukkit.ChatColor;

public enum GameStage {

    WAITING, STARTING, FORCE_STARTING, RUNNING, CONTINUING, RESETTING, DISABLING;

    public String toString() {
        if(this == WAITING) return ChatColor.GREEN + "WAITING";
        else if(this == STARTING || this == FORCE_STARTING) return ChatColor.GOLD + "STARTING";
        return ChatColor.RED + "IN PROGRESS";
    }

    public boolean isRunnable() {
        return this == STARTING || this == FORCE_STARTING || this == RUNNING || this == CONTINUING;
    }
}

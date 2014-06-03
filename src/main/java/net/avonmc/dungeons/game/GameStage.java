/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
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

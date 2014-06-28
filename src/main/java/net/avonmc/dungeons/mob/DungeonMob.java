/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package net.avonmc.dungeons.mob;

import me.confuser.barapi.BarAPI;
import net.avonmc.dungeons.Dungeons;
import net.avonmc.dungeons.lobby.Lobby;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class DungeonMob {

    private Dungeons plugin;

    private EntityType mobType;
    private String displayName;
    private double maxHealth;
    private double attackDamage;
    private ItemStack itemInHand;
    private ItemStack[] armorItems;
    private List<PotionEffect> potionEffects;
    private int abilityInterval;
    private List<Ability> mobAbilities;

    private Lobby lobby;
    private LivingEntity mob;
    private BukkitTask abilityTask;

    public DungeonMob(EntityType mobType, String displayName, double maxHealth, double attackDamage, ItemStack itemInHand, ItemStack[] armorItems, List<PotionEffect> potionEffects, int abilityInterval, List<Ability> mobAbilities) {
        this.plugin = (Dungeons) Bukkit.getServer().getPluginManager().getPlugin("Dungeons");
        this.mobType = mobType;
        this.displayName = StringUtil.colorize(displayName);
        this.maxHealth = maxHealth;
        this.attackDamage = attackDamage;
        this.itemInHand = itemInHand;
        this.armorItems = armorItems;
        this.potionEffects = potionEffects;
        this.abilityInterval = abilityInterval;
        this.mobAbilities = mobAbilities;
    }

    public boolean isDungeonMob(LivingEntity mob) {
        return mob.getType() == mobType && mob.getMaxHealth() == maxHealth && mob.getCustomName() != null && mob.getCustomName().equalsIgnoreCase(displayName);
    }

    public double getAttackDamage() {
        return attackDamage;
    }

    public LivingEntity spawnMob(Lobby lobby, Location loc) {
        this.lobby = lobby;
        mob = (LivingEntity) loc.getWorld().spawnEntity(loc, mobType);
        mob.setCustomName(displayName);
        mob.setCustomNameVisible(true);
        mob.setMaxHealth(maxHealth);
        mob.setHealth(maxHealth);
        mob.getEquipment().setItemInHand(itemInHand);
        mob.getEquipment().setArmorContents(armorItems);
        mob.addPotionEffects(potionEffects);
        mob.setRemoveWhenFarAway(false);
        runAbilityTask();
        for(OfflinePlayer player : lobby.getPlayers())
            if(player.isOnline()) BarAPI.setMessage(player.getPlayer(), displayName, 100f);
        return mob;
    }

    public void runAbilityTask() {
        abilityTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if(mob == null || mob.isDead()) cancelAbilityTask();
                else mobAbilities.get(Dungeons.random.nextInt(mobAbilities.size())).attack(mob);
            }
        }, 100, abilityInterval * 20);
    }

    private void cancelAbilityTask() {
        abilityTask.cancel();
        for(OfflinePlayer player : lobby.getPlayers())
            if(player.isOnline() && BarAPI.hasBar(player.getPlayer())) BarAPI.removeBar(player.getPlayer());
    }

    public void handleDamage() {
        for(OfflinePlayer player : lobby.getPlayers())
            if(player.isOnline()) BarAPI.setMessage(player.getPlayer(), displayName, (float) (mob.getHealth() / mob.getMaxHealth()));
    }

    public static DungeonMob deserialize(ConfigurationSection c) {
        return new DungeonMob(EntityType.valueOf(c.getString("mobType")), c.getString("displayName"), c.getDouble("maxHealth"), c.getDouble("attackDamage"),
                StringUtil.parseItemStack(c.getString("itemInHand")), StringUtil.parseItemStackList(c.getStringList("armorItems")),
                StringUtil.parsePotionEffectList(c.getStringList("potionEffects")), c.getInt("abilityInterval"), Ability.parseAbilities(c.getStringList("mobAbilities")));
    }
}

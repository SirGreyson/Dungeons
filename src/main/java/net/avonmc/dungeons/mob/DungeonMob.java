/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package net.avonmc.dungeons.mob;

import net.avonmc.dungeons.Dungeons;
import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    private ItemStack itemInHand;
    private ItemStack[] armorItems;
    private List<PotionEffect> potionEffects;
    private List<Ability> mobAbilities;

    private LivingEntity mob;
    private BukkitTask abilityTask;

    public DungeonMob(EntityType mobType, String displayName, double maxHealth, ItemStack itemInHand, ItemStack[] armorItems, List<PotionEffect> potionEffects, List<Ability> mobAbilities) {
        this.plugin = (Dungeons) Bukkit.getServer().getPluginManager().getPlugin("Dungeons");
        this.mobType = mobType;
        this.displayName = StringUtil.colorize(displayName);
        this.maxHealth = maxHealth;
        this.itemInHand = itemInHand;
        this.armorItems = armorItems;
        this.potionEffects = potionEffects;
        this.mobAbilities = mobAbilities;
    }

    public LivingEntity spawnMob(Location loc) {
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
        return mob;
    }

    public void runAbilityTask() {
        abilityTask = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                if(mob == null || mob.isDead()) cancelAbilityTask();
                else mobAbilities.get(Dungeons.random.nextInt(mobAbilities.size())).attack(mob);
            }
        }, 100, 300);
    }

    private void cancelAbilityTask() {
        abilityTask.cancel();
    }

    public static DungeonMob deserialize(ConfigurationSection c) {
        return new DungeonMob(EntityType.valueOf(c.getString("mobType")), c.getString("displayName"), c.getDouble("maxHealth"),
                StringUtil.parseItemStack(c.getString("itemInHand")), StringUtil.parseItemStackList(c.getStringList("armorItems")),
                StringUtil.parsePotionEffectList(c.getStringList("potionEffects")), Ability.parseAbilities(c.getStringList("mobAbilities")));
    }
}

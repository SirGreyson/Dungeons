/*
 * Copyright © ReasonDev 2014
 * All Rights Reserved
 * No part of this project or any of its contents may be reproduced, copied, modified or adapted, without the prior written consent of SirReason.
 */

package net.avonmc.dungeons.mob;

import net.avonmc.dungeons.util.StringUtil;
import org.bukkit.Effect;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.List;

public class Ability {

    public enum AbilityType {
        SHOOT, MINIONS;
    }

    private AbilityType abilityType;
    private EntityType entityType;
    private int amount;
    private int extra;

    public Ability(AbilityType abilityType, EntityType entityType, int amount, int extra) {
        this.abilityType = abilityType;
        this.entityType = entityType;
        this.amount = amount;
        this.extra = extra;
    }

    public void attack(LivingEntity dungeonMob) {
        if(abilityType == AbilityType.SHOOT) {
            Player target = null;
            for(Entity nearby : dungeonMob.getNearbyEntities(15, 15, 15))
                if(target != null) break;
                else if(nearby instanceof Player) target = (Player) nearby;
            if(target == null) return;
            for(int i = 0; i <= amount; i++)
                dungeonMob.launchProjectile(((Class<? extends Projectile>) entityType.getEntityClass()), target.getLocation().toVector().subtract(dungeonMob.getLocation().toVector()).normalize());
        } else if (abilityType == AbilityType.MINIONS) {
            for(int i =0 ; i <= amount; i++) {
                LivingEntity minion = (LivingEntity) dungeonMob.getWorld().spawnEntity(dungeonMob.getLocation(), entityType);
                if(minion instanceof Zombie) ((Zombie) minion).setBaby(true);
                else if(minion instanceof PigZombie) ((PigZombie) minion).setBaby(true);
                if(extra != 0) minion.setMaxHealth(extra);
                minion.setHealth(minion.getMaxHealth());
                minion.setCustomName(StringUtil.colorize("&cMinion"));
                dungeonMob.getWorld().playEffect(dungeonMob.getLocation(), Effect.MOBSPAWNER_FLAMES, 1);
            }
        }
    }

    public static List<Ability> parseAbilities(List<String> abilityList) {
        List<Ability> output = new ArrayList<Ability>();
        for(String ability : abilityList) output.add(parseAbility(ability.split(":")));
        return output;
    }

    public static Ability parseAbility(String[] args) {
        return new Ability(AbilityType.valueOf(args[0]), EntityType.valueOf(args[1]), StringUtil.asInt(args[2]), args.length == 4 ? StringUtil.asInt(args[3]) : 0);
    }
}

package net.shadowraze.dungeons.dungeon;

import net.shadowraze.dungeons.Dungeons;
import net.shadowraze.dungeons.utils.Messaging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DungeonMob {

    private String mobID;
    private EntityType mobType;
    private String displayName;
    private double health;
    private ItemStack inHand;
    private int abilityTargetRange;
    private int abilityInterval;
    private List<String> abilities;
    private List<PotionEffect> potionEffects;

    private Map<UUID, BukkitTask> abilityTask = new HashMap<UUID, BukkitTask>();
    private Map<UUID, Dungeon> dungeonMap = new HashMap<UUID, Dungeon>();

    public DungeonMob(String mobID, EntityType mobType) {
        this.mobID = mobID;
        this.mobType = mobType;
    }

    public DungeonMob(String mobID, EntityType mobType, String displayName, double health, ItemStack inHand, int abilityTargetRange, int abilityInterval, List<String> abilities, List<PotionEffect> potionEffects) {
        this.mobID = mobID;
        this.mobType = mobType;
        this.displayName = displayName;
        this.health = health;
        this.inHand = inHand;
        this.abilityTargetRange = abilityTargetRange;
        this.abilityInterval = abilityInterval;
        this.abilities = abilities;
        this.potionEffects = potionEffects;
    }

    public String getMobID() {
        return mobID;
    }

    public EntityType getMobType() {
        return mobType;
    }

    public String getDisplayName() {
        return ChatColor.translateAlternateColorCodes('&', displayName);
    }

    public double getHealth() {
        return health;
    }

    public ItemStack getInHand() {
        return inHand;
    }

    public int getAbilityTargetRange() {
        return abilityTargetRange;
    }

    public int getAbilityInterval() {
        return abilityInterval;
    }

    public List<String> getAbilities() {
        return abilities;
    }

    public List<PotionEffect> getPotionEffects() {
        return potionEffects;
    }

    public void spawnMob(Dungeon dungeon, Location location) {
        LivingEntity mob = (LivingEntity) location.getWorld().spawnEntity(location, mobType);
        if(displayName != null) mob.setCustomName(getDisplayName());
        mob.setCustomNameVisible(mob.getCustomName() != null);
        if(health != 0) mob.setMaxHealth(health);
        mob.setHealth(mob.getMaxHealth());
        if(inHand != null) mob.getEquipment().setItemInHand(inHand);
        if(potionEffects != null && !potionEffects.isEmpty())
            for(PotionEffect potionEffect : potionEffects) mob.addPotionEffect(potionEffect);
        if(abilities != null && !abilities.isEmpty()) runAbilityTask(mob);
        dungeonMap.put(mob.getUniqueId(), dungeon);
        dungeon.getSpawnedMobs().add(mob.getUniqueId());
    }

    public void despawnMob(LivingEntity mob) {
        if(abilityTask.containsKey(mob.getUniqueId())) {
            abilityTask.get(mob.getUniqueId()).cancel();
            abilityTask.remove(mob.getUniqueId());
        }
        dungeonMap.get(mob.getUniqueId()).getSpawnedMobs().remove(mob.getUniqueId());
        dungeonMap.remove(mob.getUniqueId());
        if(!mob.isDead()) mob.remove();
    }

    public void runAbilityTask(final LivingEntity mob) {
        abilityTask.put(mob.getUniqueId(), Bukkit.getScheduler().runTaskTimer(Dungeons.getPlugin(), new Runnable() {
            @Override
            public void run() {
                if(mob == null || mob.isDead()) despawnMob(mob);
                for (String ability : abilities) {
                    String[] args = ability.split(":");
                    if(args[0].equalsIgnoreCase("SHOOT") && args.length >= 2)
                        doShootAbility(mob, EntityType.valueOf(args[1]), args.length >= 3 ? Integer.parseInt(args[2]) : 1);
                    else if(args[0].equalsIgnoreCase("MINIONS") && args.length >= 2)
                        doSpawnAbility(mob, args[1], args.length >= 3 ? Integer.parseInt(args[2]) : 1);
                    else Messaging.printErr("Invalid Ability [" + args[0] + "] for DungeonMob " + mobID);
                }
            }
        }, 20L, abilityInterval * 20));
    }

    /*TODO: Make sure mobs don't target other mobs*/
    public void doShootAbility(LivingEntity mob, EntityType projectile, int amount) {
        if(mob == null || mob.isDead()) return;
        Player nearestP = null;
        for(Entity entity : mob.getNearbyEntities(abilityTargetRange, abilityTargetRange, abilityTargetRange)) {
            if(nearestP != null) break;
            else if(entity instanceof Player) nearestP = (Player) entity;
        }
        if(nearestP == null) return;
        for(int i = 0; i <= amount; i++) {
            Projectile toShoot = (Projectile) mob.getWorld().spawnEntity(mob.getLocation(), projectile);
            mob.launchProjectile(toShoot.getClass(), nearestP.getLocation().toVector().subtract(mob.getLocation().toVector()).normalize());
        }
    }

    public void doSpawnAbility(LivingEntity mob, String minionType, int amount) {
        if(mob == null || mob.isDead()) return;
        for(int i = 0; i <= amount; i++) {
            if(minionType.equalsIgnoreCase("ZOMBIE_BABY")) ((Zombie) mob.getWorld().spawnEntity(mob.getLocation(), EntityType.ZOMBIE)).setBaby(true);
            else mob.getWorld().spawnEntity(mob.getLocation(), EntityType.valueOf(minionType));
            mob.getWorld().playEffect(mob.getLocation(), Effect.MOBSPAWNER_FLAMES, 100);
        }
    }
}

package com.starshootercity.customenchanting.listeners;

import com.starshootercity.customenchanting.CustomEnchantment;
import com.starshootercity.customenchanting.CustomEnchantmentAPI;
import com.starshootercity.customenchanting.display.EnchantmentDisplay;
import com.starshootercity.customenchanting.wrappers.BukkitEnchantmentWrapper;
import com.starshootercity.customenchanting.wrappers.CustomEnchantmentWrapper;
import com.starshootercity.customenchanting.wrappers.EnchantmentWrapper;
import io.papermc.paper.enchantments.EnchantmentRarity;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnchantingTableListener implements Listener {
    private static final Map<Player, Integer> lastEnchantmentBonus = new HashMap<>();
    @EventHandler
    public void onPrepareItemEnchant(PrepareItemEnchantEvent event) {
        lastEnchantmentBonus.put(event.getEnchanter(), event.getEnchantmentBonus());
    }
    @EventHandler
    public void onEnchantItem(EnchantItemEvent event) {
        if (event.getItem().getType() == Material.BOOK) return;
        Random random = new Random();
        int enchantability = getEnchantability(event.getItem().getType());
        int randomValueOne = random.nextInt(0, Math.floorDiv(enchantability, 4));
        int randomValueTwo = random.nextInt(0, Math.floorDiv(enchantability, 4));
        int modifiedEnchantmentLevel = lastEnchantmentBonus.getOrDefault(event.getEnchanter(), 1) + randomValueOne + randomValueTwo + 1;
        event.getItem().addEnchantment(event.getEnchantmentHint(), event.getEnchantsToAdd().get(event.getEnchantmentHint()));
        while (random.nextDouble() < (modifiedEnchantmentLevel + 1f) / 50) {
            EnchantmentWrapper enchantment = getExtraEnchantment(event.getItem());
            enchantment.enchant(event.getItem(), random.nextInt(1, enchantment.getMaxLevel() + 1));
            modifiedEnchantmentLevel = Math.floorDiv(modifiedEnchantmentLevel, 2);
        }
        for (Enchantment enchantment : new HashMap<>(event.getEnchantsToAdd()).keySet()) {
            if (!enchantment.equals(event.getEnchantmentHint())) {
                event.getEnchantsToAdd().remove(enchantment);
            }
        }
        EnchantmentDisplay.updateEnchantmentDisplay(event.getItem());
    }

    public EnchantmentWrapper getExtraEnchantment(ItemStack item) {
        Random random = new Random();

        int totalWeight = 0;
        List<Enchantment> applicableEnchantments = new ArrayList<>();
        List<CustomEnchantment> applicableCustomEnchantments = new ArrayList<>();
        for (Enchantment enchantment : Enchantment.values()) {
            if (enchantment.isTreasure()) continue;
            if (CustomEnchantmentAPI.conflictsWith(item, enchantment)) continue;
            if (enchantment.canEnchantItem(item)) {
                applicableEnchantments.add(enchantment);
                totalWeight += getWeight(enchantment.getRarity());
            }
        }
        for (CustomEnchantment enchantment : CustomEnchantmentAPI.getRegisteredEnchantments().values()) {
            if (enchantment.isTreasure()) continue;
            if (CustomEnchantmentAPI.conflictsWith(item, enchantment)) continue;
            if (enchantment.canEnchantItem(item)) {
                applicableCustomEnchantments.add(enchantment);
                totalWeight += getWeight(enchantment.getRarity());
            }
        }
        int chosenWeight = random.nextInt(totalWeight);
        for (Enchantment enchantment : applicableEnchantments) {
            chosenWeight -= getWeight(enchantment.getRarity());
            if (chosenWeight < 0) {
                return new BukkitEnchantmentWrapper(enchantment);
            }
        }
        for (CustomEnchantment enchantment : applicableCustomEnchantments) {
            chosenWeight -= getWeight(enchantment.getRarity());
            if (chosenWeight < 0) {
                return new CustomEnchantmentWrapper(enchantment);
            }
        } return null;
    }

    private int getEnchantability(Material material) {
        return switch (material) {
            case WOODEN_AXE, WOODEN_HOE, WOODEN_PICKAXE, WOODEN_SHOVEL, WOODEN_SWORD, LEATHER_BOOTS, LEATHER_CHESTPLATE, LEATHER_LEGGINGS, LEATHER_HELMET, NETHERITE_AXE, NETHERITE_SHOVEL, NETHERITE_SWORD, NETHERITE_PICKAXE, NETHERITE_BOOTS, NETHERITE_LEGGINGS, NETHERITE_CHESTPLATE, NETHERITE_HELMET -> 15;
            case STONE_AXE, STONE_HOE, STONE_PICKAXE, STONE_SWORD -> 5;
            case CHAINMAIL_BOOTS, CHAINMAIL_LEGGINGS, CHAINMAIL_CHESTPLATE, CHAINMAIL_HELMET -> 12;
            case IRON_BOOTS, IRON_LEGGINGS, IRON_CHESTPLATE, IRON_HELMET, TURTLE_HELMET -> 9;
            case IRON_SWORD, IRON_AXE, IRON_HOE, IRON_PICKAXE, IRON_SHOVEL -> 14;
            case GOLDEN_BOOTS, GOLDEN_LEGGINGS, GOLDEN_CHESTPLATE, GOLDEN_HELMET -> 25;
            case GOLDEN_AXE, GOLDEN_PICKAXE, GOLDEN_SHOVEL, GOLDEN_HOE, GOLDEN_SWORD -> 22;
            case DIAMOND_AXE, DIAMOND_PICKAXE, DIAMOND_SHOVEL, DIAMOND_SWORD, DIAMOND_HOE, DIAMOND_BOOTS, DIAMOND_LEGGINGS, DIAMOND_CHESTPLATE, DIAMOND_HELMET -> 10;
            default -> 1;
        };
    }

    private int getWeight(EnchantmentRarity rarity) {
        return switch (rarity) {
            case COMMON -> 10;
            case UNCOMMON -> 5;
            case RARE -> 2;
            case VERY_RARE -> 1;
        };
    }
}

package com.starshootercity.customenchanting.listeners;

import com.starshootercity.customenchanting.CustomEnchantment;
import com.starshootercity.customenchanting.CustomEnchantmentAPI;
import com.starshootercity.customenchanting.display.EnchantmentDisplay;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

public class AnvilCombineListener implements Listener {
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getFirstItem();
        ItemStack secondItem = event.getInventory().getSecondItem();
        ItemStack resultItem = event.getInventory().getResult();
        if (firstItem == null || secondItem == null) return;
        if (resultItem == null) resultItem = firstItem.clone();
        if (secondItem.getType() == firstItem.getType() || secondItem.getType() == Material.ENCHANTED_BOOK) {
            for (CustomEnchantment enchantment : CustomEnchantmentAPI.getCustomEnchantments(secondItem).keySet()) {
                if (!enchantment.canEnchantItem(firstItem)) continue;
                if (CustomEnchantmentAPI.conflictsWith(firstItem, enchantment)) continue;
                int firstLevel = CustomEnchantmentAPI.getEnchantmentLevel(resultItem, enchantment);
                int secondLevel = CustomEnchantmentAPI.getEnchantmentLevel(secondItem, enchantment);
                if (firstLevel < secondLevel) CustomEnchantmentAPI.addCustomEnchantment(resultItem, enchantment, secondLevel);
                else if (firstLevel == secondLevel) CustomEnchantmentAPI.addCustomEnchantment(resultItem, enchantment, Math.min(enchantment.getMaxLevel(), firstLevel + 1));
            }
            for (Enchantment enchantment : CustomEnchantmentAPI.getEnchantments(secondItem).keySet()) {
                if (CustomEnchantmentAPI.conflictsWith(firstItem, enchantment)) {
                    resultItem.removeEnchantment(enchantment);
                }
            }
        }


        EnchantmentDisplay.updateEnchantmentDisplay(resultItem);
        EnchantmentDisplay.updateEnchantmentDisplay(firstItem);
        ItemMeta resultMeta = resultItem.getItemMeta();
        resultItem.setItemMeta(resultMeta);
        if (!resultItem.equals(firstItem)) {
            String renameText = event.getInventory().getRenameText();
            boolean isBeingRenamed = false;
            boolean isBeingFixed = false;
            if (renameText != null && renameText.length() > 1) {
                isBeingRenamed = true;
                resultMeta.displayName(Component.text(event.getInventory().getRenameText()));
            }
            if (resultMeta instanceof Repairable repairable) {
                int first = getWorkPenalty(firstItem);
                int second = getWorkPenalty(secondItem);
                repairable.setRepairCost(first + second + 1);
                resultItem.setItemMeta(repairable);
            }
            if (resultMeta instanceof Damageable resultDamageable) {
                if (firstItem.getItemMeta() instanceof Damageable firstDamageable) {
                    if (resultDamageable.getDamage() != firstDamageable.getDamage()) {
                        isBeingFixed = true;
                    }
                }
            }
            event.getInventory().setRepairCost(calculateExperienceCost(firstItem, secondItem, isBeingRenamed, isBeingFixed));
            event.setResult(resultItem);
            for (HumanEntity entity : event.getInventory().getViewers()) {
                if (entity instanceof Player player) {
                    player.updateInventory();
                }
            }
        }
    }

    private int calculateExperienceCost(ItemStack firstItem, ItemStack secondItem, boolean isBeingRenamed, boolean isBeingFixed) {
        int incompatibleCount = 0;
        for (Enchantment e : CustomEnchantmentAPI.getEnchantments(secondItem).keySet()) {
            if (CustomEnchantmentAPI.containsEnchantment(firstItem, e)) continue;
            if (CustomEnchantmentAPI.conflictsWith(firstItem, e)) incompatibleCount++;
        }
        for (CustomEnchantment e : CustomEnchantmentAPI.getCustomEnchantments(secondItem).keySet()) {
            if (CustomEnchantmentAPI.containsEnchantment(firstItem, e)) continue;
            if (CustomEnchantmentAPI.conflictsWith(firstItem, e)) incompatibleCount++;
        }
        int cost = getValueForSacrificing(secondItem, firstItem) + getWorkPenalty(firstItem) + getWorkPenalty(secondItem) + (isBeingRenamed ? 1 : 0) + (isBeingFixed ? 2 : 0) + incompatibleCount;

        int maxCost = CustomEnchantmentAPI.getInstance().getConfig().getInt("max-cost");


        if (maxCost < 0) maxCost = cost;
        return Math.min(maxCost, cost);
    }

    private int getWorkPenalty(ItemStack item) {
        if (item.getItemMeta() instanceof Repairable repairable) {
            return repairable.getRepairCost();
        } else return 0;
    }

    private int getValueForSacrificing(ItemStack sacrifice, ItemStack target) {
        int result = 0;
        for (Enchantment e : CustomEnchantmentAPI.getEnchantments(sacrifice).keySet()) {
            boolean contains = CustomEnchantmentAPI.containsEnchantment(target, e);
            if (CustomEnchantmentAPI.conflictsWith(target, e) && !contains) continue;
            int sacrificeLevel = CustomEnchantmentAPI.getEnchantmentLevel(sacrifice, e);
            result += (!contains ? sacrificeLevel : Math.max(
                    sacrificeLevel,
                    CustomEnchantmentAPI.getEnchantmentLevel(target, e)
            )) * getCostMultiplier(e.getRarity());
        }
        for (CustomEnchantment e : CustomEnchantmentAPI.getCustomEnchantments(sacrifice).keySet()) {
            boolean contains = CustomEnchantmentAPI.containsEnchantment(target, e);
            if (CustomEnchantmentAPI.conflictsWith(target, e) && !contains) continue;
            int sacrificeLevel = CustomEnchantmentAPI.getEnchantmentLevel(sacrifice, e);
            result += (!contains ? sacrificeLevel : Math.max(
                    sacrificeLevel,
                    CustomEnchantmentAPI.getEnchantmentLevel(target, e)
            )) * getCostMultiplier(e.getRarity());
        }
        return result;
    }

    private int getCostMultiplier(EnchantmentRarity rarity) {
        return switch (rarity) {
            case COMMON -> 1;
            case UNCOMMON -> 2;
            case RARE -> 4;
            case VERY_RARE -> 8;
        };
    }
}

package com.starshootercity.customenchanting;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class AnvilCombineListener implements Listener {
    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        ItemStack firstItem = event.getInventory().getFirstItem();
        ItemStack secondItem = event.getInventory().getSecondItem();
        ItemStack resultItem = event.getInventory().getResult();
        if (firstItem == null || secondItem == null) return;
        //Bukkit.broadcast(Component.text(firstItem.getItemMeta().toString()));
        for (Enchantment e : Enchantment.values()) {
            Bukkit.broadcast(Component.text(e.getRarity().toString()));
        }
        if (resultItem == null) resultItem = firstItem.clone();
        if (secondItem.getType() == firstItem.getType() || secondItem.getType() == Material.ENCHANTED_BOOK) {
            for (CustomEnchantment enchantment : CustomEnchantmentAPI.getCustomEnchantments(secondItem).keySet()) {
                int firstLevel = CustomEnchantmentAPI.getCustomEnchantmentLevel(resultItem, enchantment);
                int secondLevel = CustomEnchantmentAPI.getCustomEnchantmentLevel(secondItem, enchantment);
                if (firstLevel < secondLevel) CustomEnchantmentAPI.addCustomEnchantment(resultItem, enchantment, secondLevel);
                else if (firstLevel == secondLevel) CustomEnchantmentAPI.addCustomEnchantment(resultItem, enchantment, Math.min(enchantment.getMaxLevel(), firstLevel + 1));
            }
        }


        // Renaming does not work
        // XP cost needs to be calculated
        // Max XP cost from configuration needs to be added

        EnchantmentDisplay.updateEnchantmentDisplay(resultItem);
        event.getInventory().setRepairCost(calculateExperienceCost(firstItem, secondItem));
        if (!resultItem.equals(firstItem)) {
            ItemMeta resultMeta = resultItem.getItemMeta();
            String renameText = event.getInventory().getRenameText();
            if (renameText != null && renameText.length() > 1) {
                resultMeta.displayName(Component.text(event.getInventory().getRenameText()));
            }
            resultItem.setItemMeta(resultMeta);
            event.setResult(resultItem);
            for (HumanEntity entity : event.getInventory().getViewers()) {
                if (entity instanceof Player player) {
                    player.updateInventory();
                }
            }
        }
    }

    private int calculateExperienceCost(ItemStack firstItem, ItemStack secondItem) {
        //int cost = getValue(secondItem) + getWorkPenalty(firstItem) + getWorkPenalty(secondItem) + [Renaming Cost] + [Refilling Durability] + [Incompatible Enchantments (Java Edition)]

        int maxCost = CustomEnchantmentAPI.getInstance().getConfig().getInt("max-cost");
        //int cost = firstItem;
        int cost = 1;


        if (maxCost < 0) maxCost = cost;
        return Math.min(maxCost, cost);
    }
}

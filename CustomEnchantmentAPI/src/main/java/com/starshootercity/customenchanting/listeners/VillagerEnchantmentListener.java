package com.starshootercity.customenchanting.listeners;

import com.starshootercity.customenchanting.CustomEnchantmentAPI;
import org.bukkit.Material;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.VillagerAcquireTradeEvent;
import org.bukkit.inventory.MerchantRecipe;

public class VillagerEnchantmentListener implements Listener {
    @EventHandler
    public void onVillagerAcquireTrade(VillagerAcquireTradeEvent event) {
        if (event.getRecipe().getResult().getType() != Material.ENCHANTED_BOOK) return;
        if (event.getEntity() instanceof Villager villager) {
            MerchantRecipe newRecipe = CustomEnchantmentAPI.getVillagerEnchantmentChooser().pickEnchantment(
                    villager,
                    event.getRecipe()
            );
            if (newRecipe == null) event.setCancelled(true);
            else event.setRecipe(newRecipe);
        }
    }

}

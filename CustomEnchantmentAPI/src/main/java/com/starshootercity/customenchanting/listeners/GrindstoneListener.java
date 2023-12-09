package com.starshootercity.customenchanting.listeners;

import com.starshootercity.customenchanting.CustomEnchantment;
import com.starshootercity.customenchanting.CustomEnchantmentAPI;
import com.starshootercity.customenchanting.display.EnchantmentDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.inventory.ItemStack;

public class GrindstoneListener implements Listener {
    @EventHandler
    public void onPrepareGrindstone(PrepareGrindstoneEvent event) {
        ItemStack result = event.getResult();
        if (result == null) return;
        for (CustomEnchantment enchantment : CustomEnchantmentAPI.getCustomEnchantments(result).keySet()) {
            if (!enchantment.isCursed()) {
                CustomEnchantmentAPI.removeCustomEnchantment(result, enchantment);
            }
        }
        EnchantmentDisplay.updateEnchantmentDisplay(result);
        event.setResult(result);
    }
}

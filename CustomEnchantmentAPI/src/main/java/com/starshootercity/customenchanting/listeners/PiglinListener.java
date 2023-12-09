package com.starshootercity.customenchanting.listeners;

import com.starshootercity.customenchanting.CustomEnchantment;
import com.starshootercity.customenchanting.CustomEnchantmentAPI;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PiglinBarterEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PiglinListener implements Listener {
    private final Random random = new Random();
    @EventHandler
    public void onPiglinBarter(PiglinBarterEvent event) {
        List<CustomEnchantment> validEnchantments = new ArrayList<>();
        for (CustomEnchantment enchantment : CustomEnchantmentAPI.getRegisteredEnchantments().values()) {
            if (enchantment.isPiglin()) validEnchantments.add(enchantment);
        }
        int increase = validEnchantments.size() * 5;
        if (random.nextDouble() * 459 + increase > 459) {
            event.getOutcome().clear();
            ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
            CustomEnchantment enchantment = validEnchantments.get(random.nextInt(validEnchantments.size()));
            CustomEnchantmentAPI.addCustomEnchantment(book, enchantment, random.nextInt(1, enchantment.getMaxLevel() + 1));
            event.getOutcome().add(book);
        }
    }
}

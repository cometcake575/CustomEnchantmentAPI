package com.starshootercity.customenchanting.display;

import com.starshootercity.customenchanting.CustomEnchantment;
import com.starshootercity.customenchanting.CustomEnchantmentAPI;
import com.starshootercity.customenchanting.wrappers.BukkitEnchantmentWrapper;
import com.starshootercity.customenchanting.wrappers.CustomEnchantmentWrapper;
import com.starshootercity.customenchanting.wrappers.EnchantmentWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class EnchantmentDisplay {
    public static void updateEnchantmentDisplay(ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR) return;
        List<Component> lore = new ArrayList<>();
        Map<EnchantmentWrapper, Integer> enchantmentWrappers = new HashMap<>();
        for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
            if (enchantment.canEnchantItem(itemStack)) {
                enchantmentWrappers.put(new BukkitEnchantmentWrapper(enchantment), itemStack.getEnchantmentLevel(enchantment));
            }
        }
        Map<CustomEnchantment, Integer> customEnchantments = CustomEnchantmentAPI.getCustomEnchantments(itemStack);
        for (CustomEnchantment customEnchantment : customEnchantments.keySet()) {
            enchantmentWrappers.put(new CustomEnchantmentWrapper(customEnchantment), customEnchantments.get(customEnchantment));
        }
        List<EnchantmentWrapper> wrappers = new ArrayList<>(enchantmentWrappers.keySet());
        wrappers.sort(Comparator.comparing(EnchantmentWrapper::getSortKey));
        for (EnchantmentWrapper wrapper : wrappers) {
            lore.add(wrapper.getDisplayName(enchantmentWrappers.get(wrapper)));
        }
        List<Component> customLore = CustomEnchantmentAPI.getLore(itemStack);
        if (customLore != null) {
            lore.addAll(customLore);
        }
        itemStack.lore(lore.size() == 0 ? null : lore);
        if (CustomEnchantmentAPI.getCustomEnchantments(itemStack).size() > 0) {
            if (itemStack.getType() == Material.FISHING_ROD) {
                itemStack.addUnsafeEnchantment(Enchantment.WATER_WORKER, 1);
            } else if (itemStack.getType() != Material.ENCHANTED_BOOK) itemStack.addUnsafeEnchantment(Enchantment.LURE, 1);
        }
        if (itemStack.getEnchantments().size() > 0 || CustomEnchantmentAPI.getCustomEnchantments(itemStack).size() > 0) {
            itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
    }
}

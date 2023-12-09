package com.starshootercity.customenchanting.wrappers;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;

public class BukkitEnchantmentWrapper extends EnchantmentWrapper {
    private final String sortKey;
    private final Component displayName;
    private final Enchantment enchantment;
    public BukkitEnchantmentWrapper(Enchantment enchantment) {
        this.enchantment = enchantment;
        String[] fullTranslationKey = enchantment.translationKey().split("\\.");
        sortKey = fullTranslationKey[fullTranslationKey.length - 1];
        displayName = Component.translatable(enchantment.translationKey())
                .color(enchantment.isCursed() ? NamedTextColor.RED : NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);
    }

    @Override
    public String getSortKey() {
        return sortKey;
    }

    @Override
    public Component getDisplayName(int level) {
        if (getMaxLevel() > 1) {
            return displayName.append(Component.text(" %s".formatted(toRoman(level))));
        } else return displayName;
    }

    @Override
    public void enchant(ItemStack item, int level) {
        item.addEnchantment(enchantment, level);
    }

    @Override
    public void enchantForTrading(ItemStack item, int level) {
        if (item.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            meta.addStoredEnchant(enchantment, level, true);
            item.setItemMeta(meta);
        } else enchant(item, level);
    }

    @Override
    public int getMaxLevel() {
        return enchantment.getMaxLevel();
    }

    @Override
    public boolean isTreasure() {
        return enchantment.isTreasure();
    }

}

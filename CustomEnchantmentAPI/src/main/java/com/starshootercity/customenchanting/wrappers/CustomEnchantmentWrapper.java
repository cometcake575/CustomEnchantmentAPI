package com.starshootercity.customenchanting.wrappers;

import com.starshootercity.customenchanting.CustomEnchantment;
import com.starshootercity.customenchanting.CustomEnchantmentAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.inventory.ItemStack;

public class CustomEnchantmentWrapper extends EnchantmentWrapper {
    CustomEnchantment enchantment;
    private final String sortKey;
    private final Component displayName;
    public CustomEnchantmentWrapper(CustomEnchantment enchantment) {
        this.enchantment = enchantment;
        displayName = enchantment.getDisplayName();
        if (enchantment.getDisplayName() instanceof TranslatableComponent component) {
            String[] fullTranslationKey = component.key().split("\\.");
            sortKey = fullTranslationKey[fullTranslationKey.length - 1];
        } else sortKey = PlainTextComponentSerializer.plainText().serialize(enchantment.getDisplayName()).toLowerCase();
    }

    @Override
    public String getSortKey() {
        return sortKey;
    }

    @Override
    public Component getDisplayName(int level) {
        if (enchantment.getMaxLevel() > 1) {
            return displayName.append(Component.text(" %s".formatted(toRoman(level))));
        } else return displayName;
    }

    @Override
    public void enchant(ItemStack item, int level) {
        CustomEnchantmentAPI.addCustomEnchantment(item, enchantment, level);
    }

    @Override
    public void enchantForTrading(ItemStack item, int level) {
        enchant(item, level);
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

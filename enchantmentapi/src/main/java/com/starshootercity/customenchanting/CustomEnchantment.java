package com.starshootercity.customenchanting;

import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import static com.starshootercity.customenchanting.CustomEnchantmentAPI.enchantmentMap;

public class CustomEnchantment {
    private final Component displayName;
    private final int maxLevel;
    private final NamespacedKey key;
    private final EnchantmentRarity rarity;
    private final List<CustomEnchantment> conflictingCustomEnchantments = new ArrayList<>();
    private final List<Enchantment> conflictingEnchantments = new ArrayList<>();
    List<EnchantableMaterial> enchantableMaterials = new ArrayList<>() {{
        add(new EnchantableMaterial(Material.ENCHANTED_BOOK));
    }};

    public EnchantmentRarity getRarity() {
        return rarity;
    }

    public void addConflict(CustomEnchantment enchantment) {
        conflictingCustomEnchantments.add(enchantment);
        enchantment.conflictingCustomEnchantments.add(this);
    }

    public void addConflict(Enchantment enchantment) {
        conflictingEnchantments.add(enchantment);
    }

    public boolean canEnchantItem(ItemStack itemStack) {
        if (itemStack.getType() == Material.AIR) return false;
        if (CustomEnchantmentAPI.conflictsWith(itemStack, this)) return false;
        for (EnchantableMaterial material : enchantableMaterials) {
            if (material.matches(itemStack)) return true;
        }
        return false;
    }

    public void addCompatibleItem(Material material) {
        enchantableMaterials.add(new EnchantableMaterial(material));
    }

    public void addCompatibleItem(EnchantableMaterial material) {
        enchantableMaterials.add(material);
    }

    public boolean conflictsWith(CustomEnchantment enchantment) {
        return conflictingCustomEnchantments.contains(enchantment);
    }

    public boolean conflictsWith(Enchantment enchantment) {
        return conflictingEnchantments.contains(enchantment);
    }

    public Component getDisplayName() {
        return displayName;
    }

    public NamespacedKey getKey() {
        return key;
    }

    public CustomEnchantment(Component displayName,
                             NamespacedKey key,
                             int maxLevel,
                             EnchantmentRarity rarity
    ) {
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.key = key;
        this.rarity = rarity;
    }
    public CustomEnchantment(String displayName,
                             NamespacedKey key,
                             int maxLevel,
                             EnchantmentRarity rarity) {
        this(Component.text(displayName)
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false),
                key,
                maxLevel,
                rarity);
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}

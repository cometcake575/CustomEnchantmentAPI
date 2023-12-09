package com.starshootercity.customenchanting;

import com.starshootercity.customenchanting.villagers.VillagerEnchantmentChooser;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class CustomEnchantment {
    private final Component displayName;
    private final int maxLevel;
    private final NamespacedKey key;
    private final EnchantmentRarity rarity;
    private final List<CustomEnchantment> conflictingCustomEnchantments = new ArrayList<>();
    private final List<Enchantment> conflictingEnchantments = new ArrayList<>();
    private final VillagerEnchantmentChooser.ChoiceData choiceData;

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
        if (CustomEnchantmentAPI.containsEnchantment(itemStack, this)) return false;
        return target.includes(itemStack) || itemStack.getType() == Material.ENCHANTED_BOOK;
    }

    private final EnchantmentTarget target;

    public boolean conflictsWith(CustomEnchantment enchantment) {
        return conflictingCustomEnchantments.contains(enchantment);
    }

    private final boolean treasure;
    private final boolean cursed;
    private final boolean piglin;

    public boolean isCursed() {
        return cursed;
    }

    public boolean isTreasure() {
        return treasure;
    }

    public boolean isPiglin() {
        return piglin;
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

    public VillagerEnchantmentChooser.ChoiceData getChoiceData() {
        return choiceData;
    }

    public CustomEnchantment(Component displayName,
                             NamespacedKey key,
                             int maxLevel,
                             boolean treasure,
                             boolean piglin,
                             boolean cursed,
                             VillagerEnchantmentChooser.ChoiceData choiceData,
                             EnchantmentRarity rarity,
                             EnchantmentTarget target) {
        this.displayName = displayName;
        this.maxLevel = maxLevel;
        this.key = key;
        this.rarity = rarity;
        this.target = target;
        this.treasure = treasure;
        this.cursed = cursed;
        this.piglin = piglin;
        this.choiceData = choiceData;
    }
    public CustomEnchantment(String displayName,
                             NamespacedKey key,
                             int maxLevel,
                             boolean treasure,
                             boolean piglin,
                             boolean cursed,
                             VillagerEnchantmentChooser.ChoiceData choiceData,
                             EnchantmentRarity rarity,
                             EnchantmentTarget target) {
        this(Component.text(displayName)
                .color(cursed ? NamedTextColor.RED : NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false),
                key,
                maxLevel,
                treasure,
                piglin,
                cursed,
                choiceData,
                rarity,
                target);
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}

package com.starshootercity.customenchanting;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomEnchantmentAPI extends JavaPlugin {
    private static CustomEnchantmentAPI instance;

    public static CustomEnchantmentAPI getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        enchantmentContainerKey = new NamespacedKey(this, "custom-enchantments");;
        PluginCommand command = getCommand("test");
        if (command != null) command.setExecutor(new EnchantmentDisplay());
        new CustomEnchantment(Component.translatable("Hello"), new NamespacedKey(this, "hello"), 1);
    }

    public void registerEnchantment(CustomEnchantment enchantment, List<Material> appliesTo) {

    }

    private static NamespacedKey enchantmentContainerKey;

    public static ItemStack addCustomEnchantment(ItemStack itemStack, CustomEnchantment enchantment, int level) {
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer enchantmentContainer = meta.getPersistentDataContainer().get(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER);
        if (enchantmentContainer == null) enchantmentContainer = meta.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();

        enchantmentContainer.set(enchantment.getKey(), PersistentDataType.INTEGER, level);

        meta.getPersistentDataContainer().set(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER, enchantmentContainer);
        itemStack.setItemMeta(meta);
        return itemStack;
    }
    public static int getCustomEnchantmentLevel(ItemStack itemStack, CustomEnchantment enchantment) {
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer enchantmentContainer = meta.getPersistentDataContainer().get(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER);
        if (enchantmentContainer == null) return 0;

        Integer level = enchantmentContainer.get(enchantment.getKey(), PersistentDataType.INTEGER);

        return level == null ? 0 : level;
    }
    public static Map<CustomEnchantment, Integer> getCustomEnchantments(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer enchantmentContainer = meta.getPersistentDataContainer().get(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER);
        if (enchantmentContainer == null) return new HashMap<>();

        Map<CustomEnchantment, Integer> enchantments = new HashMap<>();

        for (NamespacedKey key : enchantmentContainer.getKeys()) {
            CustomEnchantment enchantment = CustomEnchantment.getEnchantmentByKey(key);
            enchantments.put(enchantment, getCustomEnchantmentLevel(itemStack, enchantment));
        }

        return enchantments;
    }

    public boolean conflictsWith(ItemStack itemStack, Enchantment enchantment) {
        for (CustomEnchantment otherEnchantment : getCustomEnchantments(itemStack).keySet()) {
            if (otherEnchantment.conflictsWith(enchantment)) return true;
        }
        for (Enchantment otherEnchantment : itemStack.getEnchantments().keySet()) {
            if (otherEnchantment.conflictsWith(enchantment)) return true;
        }
        return false;
    }

    public boolean conflictsWith(ItemStack itemStack, CustomEnchantment enchantment) {
        for (CustomEnchantment otherEnchantment : getCustomEnchantments(itemStack).keySet()) {
            if (enchantment.conflictsWith(otherEnchantment)) return true;
        }
        for (Enchantment otherEnchantment : itemStack.getEnchantments().keySet()) {
            if (enchantment.conflictsWith(otherEnchantment)) return true;
        }
        return false;
    }
}
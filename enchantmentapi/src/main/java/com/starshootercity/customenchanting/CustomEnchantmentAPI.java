package com.starshootercity.customenchanting;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Range;

import java.util.ArrayList;
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
        saveDefaultConfig();
        enchantmentContainerKey = new NamespacedKey(this, "custom-enchantments");
        PluginCommand updateCommand = getCommand("update");
        PluginCommand enchantCommand = getCommand("custom-enchant");
        PluginCommand forceEnchantCommand = getCommand("force-enchant");
        new EnchantmentCommands() {{
            if (updateCommand != null) updateCommand.setExecutor(this);
            if (enchantCommand != null) enchantCommand.setExecutor(this);
            if (forceEnchantCommand != null) forceEnchantCommand.setExecutor(this);
        }};
        Bukkit.getPluginManager().registerEvents(new VillagerEnchantmentListener(), this);
        Bukkit.getPluginManager().registerEvents(new PiglinBookListener(), this);
        Bukkit.getPluginManager().registerEvents(new EnchantingTableListener(), this);
        Bukkit.getPluginManager().registerEvents(new AnvilCombineListener(), this);
        Bukkit.getPluginManager().registerEvents(new GrindstoneListener(), this);
    }

    public static void registerEnchantment(
            CustomEnchantment enchantment,
            Map<Villager.Type, List<@Range(from = 1, to = 5) Integer>> villagerEnchants,
            @Range(from = 0, to = 1) double piglinChance,
            @Range(from = 0, to = 30) int enchantingTableReplacementLevelLower,
            @Range(from = 0, to = 30) int enchantingTableReplacementLevelUpper,
            @Range(from = 0, to = 1) double enchantingTableReplacementChance,
            boolean removableByGrindstones
            ) {
        enchantmentMap.put(enchantment.getKey(), enchantment);
        villagerEnchantments.put(enchantment.getKey(), villagerEnchants);
        piglinEnchantments.put(enchantment.getKey(), piglinChance);
        removableByGrindstoneEnchants.add(enchantment.getKey());
        tableEnchantmentLevelRange.put(enchantment.getKey(), new Integer[]{enchantingTableReplacementLevelLower, enchantingTableReplacementLevelUpper});
        tableEnchantmentReplacementChance.put(enchantment.getKey(), enchantingTableReplacementChance);
    }

    private static final Map<NamespacedKey, Map<Villager.Type, List<@Range(from = 1, to = 5) Integer>>> villagerEnchantments = new HashMap<>();
    private static final Map<NamespacedKey, Integer[]> tableEnchantmentLevelRange = new HashMap<>();
    private static final List<NamespacedKey> removableByGrindstoneEnchants = new ArrayList<>();
    private static final Map<NamespacedKey, Double> tableEnchantmentReplacementChance = new HashMap<>();
    private static final Map<NamespacedKey, Double> piglinEnchantments = new HashMap<>();

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
            CustomEnchantment enchantment = CustomEnchantmentAPI.getEnchantmentByKey(key);
            enchantments.put(enchantment, getCustomEnchantmentLevel(itemStack, enchantment));
        }

        return enchantments;
    }

    public static boolean conflictsWith(ItemStack itemStack, Enchantment enchantment) {
        for (CustomEnchantment otherEnchantment : getCustomEnchantments(itemStack).keySet()) {
            if (otherEnchantment.conflictsWith(enchantment)) return true;
        }
        for (Enchantment otherEnchantment : itemStack.getEnchantments().keySet()) {
            if (otherEnchantment.conflictsWith(enchantment)) return true;
        }
        return false;
    }

    public static boolean conflictsWith(ItemStack itemStack, CustomEnchantment enchantment) {
        for (CustomEnchantment otherEnchantment : getCustomEnchantments(itemStack).keySet()) {
            if (enchantment.conflictsWith(otherEnchantment)) return true;
        }
        for (Enchantment otherEnchantment : itemStack.getEnchantments().keySet()) {
            if (enchantment.conflictsWith(otherEnchantment)) return true;
        }
        return false;
    }

    protected static final Map<NamespacedKey, CustomEnchantment> enchantmentMap = new HashMap<>();
    public static CustomEnchantment getEnchantmentByKey(NamespacedKey key) {
        return enchantmentMap.get(key);
    }

}
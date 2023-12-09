package com.starshootercity.customenchanting;

import com.starshootercity.customenchanting.display.EnchantmentDisplay;
import com.starshootercity.customenchanting.listeners.*;
import com.starshootercity.customenchanting.villagers.VillagerEnchantmentChooser;
import com.starshootercity.customenchanting.wrappers.CustomEnchantmentWrapper;
import io.papermc.paper.datapack.Datapack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CustomEnchantmentAPI extends JavaPlugin {
    private static CustomEnchantmentAPI instance;

    public static CustomEnchantmentAPI getInstance() {
        return instance;
    }

    public static Map<NamespacedKey, CustomEnchantment> getRegisteredEnchantments() {
        return enchantmentMap;
    }

    private static VillagerEnchantmentChooser villagerEnchantmentChooser;

    public static VillagerEnchantmentChooser getVillagerEnchantmentChooser() {
        return villagerEnchantmentChooser;
    }

    public static void resetVillagerEnchantmentChooser(VillagerEnchantmentChooser.SetupMode mode) {
        villagerEnchantmentChooser = new VillagerEnchantmentChooser(mode);
    }

    @Override
    public void onEnable() {
        instance = this;
        Collection<Datapack> packs = Bukkit.getDatapackManager().getPacks();
        VillagerEnchantmentChooser.SetupMode mode = VillagerEnchantmentChooser.SetupMode.OLD;
        for (Datapack datapack : packs) {
            if (datapack.getName().equals("trade_rebalance")) {
                if (datapack.isEnabled()) mode = VillagerEnchantmentChooser.SetupMode.REBALANCED;
            }
        }
        villagerEnchantmentChooser = new VillagerEnchantmentChooser(mode);
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
        Bukkit.getPluginManager().registerEvents(new PiglinListener(), this);
        Bukkit.getPluginManager().registerEvents(new EnchantingTableListener(), this);
        Bukkit.getPluginManager().registerEvents(new AnvilCombineListener(), this);
        Bukkit.getPluginManager().registerEvents(new GrindstoneListener(), this);
    }

    public static void registerEnchantment(CustomEnchantment enchantment) {
        getVillagerEnchantmentChooser().addEnchantmentChoice(new CustomEnchantmentWrapper(enchantment), enchantment.getChoiceData());
        enchantmentMap.put(enchantment.getKey(), enchantment);
    }

    private static NamespacedKey enchantmentContainerKey;

    public static void addCustomEnchantment(ItemStack itemStack, CustomEnchantment enchantment, int level) {
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer enchantmentContainer = meta.getPersistentDataContainer().get(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER);
        if (enchantmentContainer == null) enchantmentContainer = meta.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();

        enchantmentContainer.set(enchantment.getKey(), PersistentDataType.INTEGER, level);

        meta.getPersistentDataContainer().set(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER, enchantmentContainer);
        itemStack.setItemMeta(meta);
        EnchantmentDisplay.updateEnchantmentDisplay(itemStack);
    }
    public static void removeCustomEnchantment(ItemStack itemStack, CustomEnchantment enchantment) {
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer enchantmentContainer = meta.getPersistentDataContainer().get(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER);
        if (enchantmentContainer == null) enchantmentContainer = meta.getPersistentDataContainer().getAdapterContext().newPersistentDataContainer();

        enchantmentContainer.remove(enchantment.getKey());

        meta.getPersistentDataContainer().set(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER, enchantmentContainer);
        itemStack.setItemMeta(meta);
        EnchantmentDisplay.updateEnchantmentDisplay(itemStack);
    }
    public static int getEnchantmentLevel(ItemStack itemStack, CustomEnchantment enchantment) {
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer enchantmentContainer = meta.getPersistentDataContainer().get(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER);
        if (enchantmentContainer == null) return 0;

        Integer level = enchantmentContainer.get(enchantment.getKey(), PersistentDataType.INTEGER);

        return level == null ? 0 : level;
    }
    public static int getEnchantmentLevel(ItemStack itemStack, Enchantment enchantment) {
        int level = itemStack.getEnchantmentLevel(enchantment);
        if (level == 0 && itemStack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            level = meta.getStoredEnchantLevel(enchantment);
        }
        return level;
    }
    public static boolean containsEnchantment(ItemStack itemStack, Enchantment enchantment) {
        boolean contains = itemStack.containsEnchantment(enchantment);
        if (!contains && itemStack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            contains = meta.hasStoredEnchant(enchantment);
        }
        return contains;
    }
    public static boolean containsEnchantment(ItemStack itemStack, CustomEnchantment enchantment) {
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer enchantmentContainer = meta.getPersistentDataContainer().get(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER);
        if (enchantmentContainer == null) return false;

        return enchantmentContainer.has(enchantment.getKey());
    }

    public static Map<Enchantment, Integer> getEnchantments(ItemStack itemStack) {
        Map<Enchantment, Integer> enchantments = new HashMap<>(itemStack.getEnchantments());
        if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta meta) {
            enchantments.putAll(meta.getStoredEnchants());
        }
        return enchantments;
    }

    public static Map<CustomEnchantment, Integer> getCustomEnchantments(ItemStack itemStack) {
        ItemMeta meta = itemStack.getItemMeta();

        PersistentDataContainer enchantmentContainer = meta.getPersistentDataContainer().get(enchantmentContainerKey, PersistentDataType.TAG_CONTAINER);
        if (enchantmentContainer == null) return new HashMap<>();

        Map<CustomEnchantment, Integer> enchantments = new HashMap<>();

        for (NamespacedKey key : enchantmentContainer.getKeys()) {
            CustomEnchantment enchantment = CustomEnchantmentAPI.getEnchantmentByKey(key);
            enchantments.put(enchantment, getEnchantmentLevel(itemStack, enchantment));
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

    private static final NamespacedKey loreKey = new NamespacedKey(getInstance(), "custom-lore");

    public static List<Component> getLore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            Component[] components = meta.getPersistentDataContainer().get(loreKey, new ComponentDataType());
            if (components == null) return null;
            return new ArrayList<>(List.of(components));
        } else return null;
    }

    public void setLore(ItemStack item, List<Component> lore) {
        Component[] components = new Component[lore.size()];
        for (int i = 0; i < lore.size(); i++) {
            components[i] = lore.get(i);
        }
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(loreKey, new ComponentDataType(), components);
            item.setItemMeta(meta);
        }
        EnchantmentDisplay.updateEnchantmentDisplay(item);
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

    public static class ComponentDataType implements PersistentDataType<String[], Component[]> {

        @Override
        public @NotNull Class<String[]> getPrimitiveType() {
            return String[].class;
        }

        @Override
        public @NotNull Class<Component[]> getComplexType() {
            return Component[].class;
        }

        @Override
        public String @NotNull [] toPrimitive(Component @NotNull [] complex, @NotNull PersistentDataAdapterContext context) {
            String[] result = new String[complex.length];
            for (int i = 0; i < complex.length; i++) {
                result[i] = JSONComponentSerializer.json().serialize(complex[i]);
            }
            return result;
        }

        @Override
        public Component @NotNull [] fromPrimitive(String @NotNull [] primitive, @NotNull PersistentDataAdapterContext context) {

            Component[] result = new Component[primitive.length];
            for (int i = 0; i < primitive.length; i++) {
                result[i] = JSONComponentSerializer.json().deserialize(primitive[i]);
            }
            return result;
        }
    }

}
package com.starshootercity.customenchanting.villagers;

import com.starshootercity.customenchanting.wrappers.BukkitEnchantmentWrapper;
import com.starshootercity.customenchanting.wrappers.EnchantmentWrapper;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import java.util.*;

@SuppressWarnings("unused")
public class VillagerEnchantmentChooser {
    public static class ChoiceDataPart {
        private final VillagerType type;
        private final List<VillagerLevel> villagerLevel;
        private final int forcedLevel;
        public ChoiceDataPart(VillagerType type, VillagerLevel villagerLevel, VillagerLevel... villagerLevels) {
            this(-1, type, villagerLevel, villagerLevels);
        }
        public ChoiceDataPart(int forcedLevel, VillagerType type, VillagerLevel villagerLevel, VillagerLevel... villagerLevels) {
            this.type = type;
            this.villagerLevel = new ArrayList<>(List.of(villagerLevel));
            this.villagerLevel.addAll(List.of(villagerLevels));
            this.forcedLevel = forcedLevel;
        }
        public boolean appliesTo(Villager villager) {
            VillagerLevel actualLevel = VillagerLevel.get(villager.getVillagerLevel());
            VillagerType actualType = VillagerType.get(villager.getVillagerType());
            if (actualLevel == null) return false;
            if (actualType == null) return false;
            return (type == VillagerType.ALL || actualType.matches(type)) && (villagerLevel.contains(VillagerLevel.ALL) || villagerLevel.contains(actualLevel));
        }
        public int getForcedLevel() {
            return forcedLevel;
        }
    }

    public static class ChoiceData {
        public ChoiceData() {
            choiceDataParts = new ArrayList<>();
        }
        public static ChoiceData merge(ChoiceData data1, ChoiceData data2) {
            ChoiceData result = new ChoiceData();
            result.choiceDataParts.addAll(data1.choiceDataParts);
            result.choiceDataParts.addAll(data2.choiceDataParts);
            return result;
        }
        public static final ChoiceData oldChoiceData = new ChoiceData(new ChoiceDataPart(VillagerType.ALL, VillagerLevel.ALL));
        private final List<ChoiceDataPart> choiceDataParts;
        public ChoiceData(ChoiceDataPart choiceDataPart, ChoiceDataPart... choiceDataParts) {
            this.choiceDataParts = new ArrayList<>(List.of(choiceDataPart));
            this.choiceDataParts.addAll(Arrays.stream(choiceDataParts).toList());
        }
        public ChoiceData(int forcedLevel, VillagerType type, VillagerLevel villagerLevel, VillagerLevel... villagerLevels) {
            this.choiceDataParts = new ArrayList<>(List.of(new ChoiceDataPart(forcedLevel, type, villagerLevel, villagerLevels)));
        }
        public ChoiceData(VillagerType type, VillagerLevel villagerLevel, VillagerLevel... villagerLevels) {
            this(-1, type, villagerLevel, villagerLevels);
        }
        public boolean appliesTo(Villager villager) {
            for (ChoiceDataPart data : choiceDataParts) {
                if (data.appliesTo(villager)) return true;
            }
            return false;
        }
    }

    private final Map<EnchantmentWrapper, ChoiceData> enchantmentData = new HashMap<>();
    private final Random random;

    public VillagerEnchantmentChooser(SetupMode mode) {
        random = new Random();
        if (mode == SetupMode.OLD) {
            List<EnchantmentWrapper> enchantments = new ArrayList<>();
            for (Enchantment enchantment : Enchantment.values()) {
                if (enchantment.isTradeable()) {
                    enchantmentData.put(new BukkitEnchantmentWrapper(enchantment), ChoiceData.oldChoiceData);
                }
            }
        } else if (mode == SetupMode.REBALANCED) {
            enchantmentData.put(new BukkitEnchantmentWrapper(Enchantment.ARROW_INFINITE), new ChoiceData(VillagerType.DESERT, VillagerLevel.APPRENTICE, VillagerLevel.JOURNEYMAN));
        }
    }
    public void addEnchantmentChoice(EnchantmentWrapper wrapper, ChoiceData data) {
        if (enchantmentData.containsKey(wrapper)) {
            ChoiceData mergedData = ChoiceData.merge(data, enchantmentData.get(wrapper));
        } else enchantmentData.put(wrapper, data);
    }
    public void clearEnchantmentChoices(EnchantmentWrapper wrapper) {
        enchantmentData.remove(wrapper);
    }

    public int getCostFor(EnchantmentWrapper enchantmentWrapper, int level) {
        int minPrice = 2 + 3 * level;
        int maxPrice = 6 + 13 * level;
        int cost = random.nextInt(minPrice, maxPrice + 1);
        if (enchantmentWrapper.isTreasure()) cost *= 2;
        if (cost > 64) cost = 64;
        return cost;
    }

    public MerchantRecipe pickEnchantment(Villager villager, MerchantRecipe recipe) {
        List<EnchantmentWrapper> choices = new ArrayList<>();
        for (EnchantmentWrapper wrapper : enchantmentData.keySet()) {
            if (enchantmentData.get(wrapper).appliesTo(villager)) {
                choices.add(wrapper);
            }
        }
        if (choices.size() == 0) return null;
        EnchantmentWrapper choice = choices.get(random.nextInt(choices.size()));
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        int level = random.nextInt(1, choice.getMaxLevel() + 1);
        choice.enchantForTrading(book, level);
        MerchantRecipe createdRecipe = new MerchantRecipe(
                book,
                recipe.getUses(),
                recipe.getMaxUses(),
                recipe.hasExperienceReward(),
                recipe.getVillagerExperience(),
                recipe.getPriceMultiplier(),
                recipe.getDemand(),
                recipe.getSpecialPrice(),
                recipe.shouldIgnoreDiscounts()
                );
        createdRecipe.addIngredient(new ItemStack(Material.EMERALD, getCostFor(choice, level)));
        createdRecipe.addIngredient(new ItemStack(Material.BOOK));
        return createdRecipe;
    }

    public enum SetupMode {
        OLD,
        REBALANCED,
        EMPTY
    }

    public enum VillagerLevel {
        NOVICE(1),
        APPRENTICE(2),
        JOURNEYMAN(3),
        EXPERT(4),
        MASTER(5),
        ALL(-1);

        final int value;

        VillagerLevel(int value) {
            this.value = value;
        }

        public boolean matches(VillagerLevel other) {
            return equals(ALL) || equals(other);
        }

        public static VillagerLevel get(int level) {
            for (VillagerLevel villagerType : VillagerLevel.values()) {
                if (villagerType.value == level) return villagerType;
            } return null;
        }
    }

    public enum VillagerType {
        DESERT(Villager.Type.DESERT),
        JUNGLE(Villager.Type.JUNGLE),
        PLAINS(Villager.Type.PLAINS),
        SAVANNA(Villager.Type.SAVANNA),
        SNOW(Villager.Type.SNOW),
        SWAMP(Villager.Type.SWAMP),
        TAIGA(Villager.Type.TAIGA),
        ALL(null);


        final Villager.Type type;
        VillagerType(Villager.Type type) {
            this.type = type;
        }

        public boolean matches(VillagerType other) {
            return equals(ALL) || equals(other);
        }

        public static VillagerType get(Villager.Type type) {
            for (VillagerType villagerType : VillagerType.values()) {
                if (villagerType.type == type) return villagerType;
            } return null;
        }
    }
}

package com.starshootercity.customenchanting.wrappers;

import net.kyori.adventure.text.Component;
import org.bukkit.inventory.ItemStack;

import java.util.TreeMap;

public abstract class EnchantmentWrapper {
    public abstract String getSortKey();


    public abstract Component getDisplayName(int level);

    private final TreeMap<Integer, String> map = new TreeMap<>() {{
        put(1000, "M");
        put(900, "CM");
        put(500, "D");
        put(400, "CD");
        put(100, "C");
        put(90, "XC");
        put(50, "L");
        put(40, "XL");
        put(10, "X");
        put(9, "IX");
        put(5, "V");
        put(4, "IV");
        put(1, "I");
    }};

    protected String toRoman(int number) {
        int l =  map.floorKey(number);
        if (number == l) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }

    public abstract void enchant(ItemStack item, int level);
    public abstract void enchantForTrading(ItemStack item, int level);
    public abstract int getMaxLevel();
    public abstract boolean isTreasure();
}

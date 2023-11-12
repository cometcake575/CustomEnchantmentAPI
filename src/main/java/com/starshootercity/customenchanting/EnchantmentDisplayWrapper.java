package com.starshootercity.customenchanting;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.enchantments.Enchantment;

import java.util.TreeMap;

public class EnchantmentDisplayWrapper {
    private Component displayName;
    private final String sortKey;

    public String getSortKey() {
        return sortKey;
    }

    // Creates an enchantment display wrapper to display a vanilla enchantment
    public EnchantmentDisplayWrapper(Enchantment enchantment, int level) {
        String[] fullTranslationKey = enchantment.translationKey().split("\\.");
        sortKey = fullTranslationKey[fullTranslationKey.length - 1];
        displayName = Component.translatable(enchantment.translationKey())
                .color(NamedTextColor.GRAY)
                .decoration(TextDecoration.ITALIC, false);
        if (enchantment.getMaxLevel() > 1) displayName = displayName.append(Component.text(" %s".formatted(toRoman(level))));

    }

    // Creates an enchantment display wrapper to display a custom enchantment
    public EnchantmentDisplayWrapper(CustomEnchantment enchantment, int level) {
        displayName = enchantment.getDisplayName();
        if (enchantment.getMaxLevel() > 1) displayName = displayName.append(Component.text(" %s".formatted(toRoman(level))));
        if (enchantment.getDisplayName() instanceof TranslatableComponent component) {
            String[] fullTranslationKey = component.key().split("\\.");
            sortKey = fullTranslationKey[fullTranslationKey.length - 1];
        } else sortKey = PlainTextComponentSerializer.plainText().serialize(enchantment.getDisplayName()).toLowerCase();
    }

    public Component getDisplayName() {
        return displayName;
    }

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

    private String toRoman(int number) {
        int l =  map.floorKey(number);
        if (number == l) {
            return map.get(number);
        }
        return map.get(l) + toRoman(number-l);
    }

}

package com.starshootercity.customenchanting;

import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

public class CustomEnchantmentExamples extends JavaPlugin {
    @Override
    public void onEnable() {
        CustomEnchantmentAPI.registerEnchantment(new CustomEnchantment(
                Component.text("Banana")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                new NamespacedKey(this, "banana-enchant"),
                3,
                EnchantmentRarity.COMMON
        ),
                new HashMap<>(),
                0,
                0,
                0,
                0,
                true);
    }
}
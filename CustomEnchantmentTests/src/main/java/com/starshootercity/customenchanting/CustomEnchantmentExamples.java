package com.starshootercity.customenchanting;

import com.starshootercity.customenchanting.villagers.VillagerEnchantmentChooser;
import io.papermc.paper.enchantments.EnchantmentRarity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomEnchantmentExamples extends JavaPlugin {
    @Override
    public void onEnable() {
        CustomEnchantmentAPI.resetVillagerEnchantmentChooser(VillagerEnchantmentChooser.SetupMode.EMPTY);
        CustomEnchantmentAPI.registerEnchantment(new CustomEnchantment(
                    Component.text("Banana")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                new NamespacedKey(this, "banana-enchant"),
                4,
                true,
                false,
                false,
                new VillagerEnchantmentChooser.ChoiceData(
                        new VillagerEnchantmentChooser.ChoiceDataPart(VillagerEnchantmentChooser.VillagerType.ALL,
                                VillagerEnchantmentChooser.VillagerLevel.ALL)
                ),
                EnchantmentRarity.COMMON,
                EnchantmentTarget.ARMOR)
        );
    }
}
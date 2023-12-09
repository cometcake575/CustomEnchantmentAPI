package com.starshootercity.customenchanting;

import com.starshootercity.customenchanting.display.EnchantmentDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class EnchantmentCommands implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item.getType() == Material.AIR) {
                return true;
            }
            if (command.getName().equals("update")) {
                EnchantmentDisplay.updateEnchantmentDisplay(item);
            } else {
                int level;
                if (args.length == 1) {
                    level = 1;
                } else if (args.length == 2) {
                    if (!args[1].matches("\\d+")) return false;
                    level = Integer.parseInt(args[1]);
                } else return false;
                String key = args[0];
                for (NamespacedKey enchantmentKey : CustomEnchantmentAPI.enchantmentMap.keySet()) {
                    if (enchantmentKey.toString().equals(key)) {
                        CustomEnchantment enchantment = CustomEnchantmentAPI.getEnchantmentByKey(enchantmentKey);
                        if (!enchantment.canEnchantItem(item) && command.getName().equals("custom-enchant")) {
                            player.sendMessage(
                                    item.displayName()
                                    .append(Component.text("cannot support that enchantment"))
                                    .color(NamedTextColor.RED)
                            );
                            return true;
                        }
                        if (enchantment.getMaxLevel() <= level && command.getName().equals("custom-enchant")) {
                            player.sendMessage(
                                    Component.text("%s is higher than the maximum level of %s supported by that enchantment"
                                                    .formatted(level, enchantment.getMaxLevel()))
                                    .color(NamedTextColor.RED)
                            );
                            return true;
                        }
                        CustomEnchantmentAPI.addCustomEnchantment(
                                item,
                                CustomEnchantmentAPI.getEnchantmentByKey(enchantmentKey),
                                level
                        );
                        return true;
                    }
                }
                return false;
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> enchantments = new ArrayList<>();
        if (command.getName().equals("custom-enchant") || command.getName().equals("force-enchant")) {
            if (sender instanceof Player player) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) return null;
                for (NamespacedKey enchantmentKey : CustomEnchantmentAPI.enchantmentMap.keySet()) {
                    if (CustomEnchantmentAPI.getEnchantmentByKey(enchantmentKey).canEnchantItem(item) || command.getName().equals("force-enchant")) {
                        enchantments.add(enchantmentKey.toString());
                    }
                }
            }
        }
        return enchantments;
    }
}

package com.starshootercity.customenchanting;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class EnchantmentDisplay implements CommandExecutor {
    public static void updateEnchantmentDisplay(ItemStack itemStack) {
        itemStack.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<Component> lore = new ArrayList<>();
        List<EnchantmentDisplayWrapper> enchantmentDisplayWrappers = new ArrayList<>();
        for (Enchantment enchantment : itemStack.getEnchantments().keySet()) {
            enchantmentDisplayWrappers.add(new EnchantmentDisplayWrapper(enchantment, itemStack.getEnchantmentLevel(enchantment)));
        }
        enchantmentDisplayWrappers.sort(Comparator.comparing(wrapper -> wrapper.getSortKey()));
        for (EnchantmentDisplayWrapper wrapper : enchantmentDisplayWrappers) {
            lore.add(wrapper.getDisplayName());
        }
        itemStack.lore(lore);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            updateEnchantmentDisplay(player.getInventory().getItemInMainHand());
        }
        return true;
    }
}

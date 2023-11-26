package com.starshootercity.customenchanting;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnchantableMaterial {
    private final Material material;
    private final Map<KeyDatatypePair, Object> requiredSameTags = new HashMap<>();
    private final Map<KeyDatatypePair, Object> requiredDifferentTags = new HashMap<>();
    private final List<NamespacedKey> requiredMissingTags = new ArrayList<>();
    private final List<NamespacedKey> requiredPresentTags = new ArrayList<>();

    public EnchantableMaterial(Material material) {
        this.material = material;
    }

    public Material getMaterial() {
        return material;
    }

    public boolean matches(ItemStack itemStack) {
        PersistentDataContainer container = itemStack.getItemMeta().getPersistentDataContainer();
        for (NamespacedKey key : requiredPresentTags) {
            if (!container.has(key)) return false;
        }
        for (NamespacedKey key : requiredMissingTags) {
            if (container.has(key)) return false;
        }
        for (KeyDatatypePair pair : requiredSameTags.keySet()) {
            Object value = container.get(pair.key(), pair.dataType());
            if (value == null) return false;
            if (!value.equals(requiredSameTags.get(pair))) return false;
        }
        for (KeyDatatypePair pair : requiredDifferentTags.keySet()) {
            Object value = container.get(pair.key(), pair.dataType());
            if (value == null) continue;
            if (value.equals(requiredDifferentTags.get(pair))) return false;
        }
        return true;
    }

    public <T, Z> void addRequiredSameTag(NamespacedKey key, PersistentDataType<T, Z> dataType, T value) {
        requiredSameTags.put(new KeyDatatypePair(key, dataType), value);
    }

    public <T, Z> void addRequiredDifferentTag(NamespacedKey key, PersistentDataType<T, Z> dataType, T value) {
        requiredDifferentTags.put(new KeyDatatypePair(key, dataType), value);
    }

    public void addRequiredPresentTag(NamespacedKey key) {
        requiredPresentTags.add(key);
    }

    public void addRequiredMissingTag(NamespacedKey key) {
        requiredMissingTags.add(key);
    }

    private record KeyDatatypePair(NamespacedKey key, PersistentDataType<?, ?> dataType) {}
}

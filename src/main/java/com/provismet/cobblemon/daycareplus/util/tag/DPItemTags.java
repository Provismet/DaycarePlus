package com.provismet.cobblemon.daycareplus.util.tag;

import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;

public abstract class DPItemTags {
    public static final TagKey<Item> BREEDING_ITEM = DPItemTags.of("breeding");
    public static final TagKey<Item> EGG_BAGS = DPItemTags.of("egg_bags");

    private static TagKey<Item> of (String path) {
        return TagKey.of(RegistryKeys.ITEM, DaycarePlusServer.identifier(path));
    }
}

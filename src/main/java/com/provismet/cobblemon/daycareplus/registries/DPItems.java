package com.provismet.cobblemon.daycareplus.registries;

import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.item.PokemonEggItem;
import com.provismet.cobblemon.daycareplus.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public abstract class DPItems {
    public static final PokemonEggItem POKEMON_EGG = register("pokemon_egg", new Item.Settings().maxCount(1), PokemonEggItem::new);

    private static <T extends PolymerItem> T register (String name, Item.Settings settings, ItemConstructor<T> constructor) {
        Identifier itemId = DaycarePlusServer.identifier(name);
        PolymerModelData model = PolymerResourcePackUtils.requestModel(Items.IRON_NUGGET, itemId.withPrefixedPath("item/"));
        return Registry.register(Registries.ITEM, itemId, constructor.apply(settings, Items.IRON_NUGGET, model));
    }

    public static void init () {}

    @FunctionalInterface
    private interface ItemConstructor<T extends PolymerItem> {
        T apply (Item.Settings settings, Item vanillaItem, PolymerModelData modelData);
    }
}

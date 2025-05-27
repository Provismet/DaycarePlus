package com.provismet.cobblemon.daycareplus.registries;

import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.item.EggBagItem;
import com.provismet.cobblemon.daycareplus.item.PokemonEggItem;
import com.provismet.cobblemon.daycareplus.item.PolymerItem;
import com.provismet.cobblemon.daycareplus.item.component.EggBagDataComponent;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public abstract class DPItems {
    public static final PokemonEggItem POKEMON_EGG = register("pokemon_egg", new Item.Settings().maxCount(1), PokemonEggItem::new);

    public static final EggBagItem LEATHER_EGG_BAG = registerBag("leather_egg_bag", 1, 8);

    private static <T extends PolymerItem> T register (String name, ItemConstructor<T> constructor) {
        Identifier itemId = DaycarePlusServer.identifier(name);
        PolymerModelData model = PolymerResourcePackUtils.requestModel(Items.IRON_NUGGET, itemId.withPrefixedPath("item/"));
        return Registry.register(Registries.ITEM, itemId, constructor.apply(new Item.Settings(), Items.IRON_NUGGET, model));
    }

    private static EggBagItem registerBag (String name, int eggsToTick, int carryingCapacity) {
        return register(name, (settings, vanillaItem, modelData) -> new EggBagItem(
            settings
                .maxCount(1)
                .component(DPItemDataComponents.ACTIVE_BAG, false)
                .component(DPItemDataComponents.HELD_EGGS, new EggBagDataComponent(carryingCapacity)),
            vanillaItem,
            modelData,
            eggsToTick
        ));
    }

    public static void init () {}

    @FunctionalInterface
    private interface ItemConstructor<T extends PolymerItem> {
        T apply (Item.Settings settings, Item vanillaItem, PolymerModelData modelData);
    }
}

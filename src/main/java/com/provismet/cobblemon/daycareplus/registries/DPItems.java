package com.provismet.cobblemon.daycareplus.registries;

import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.config.Options;
import com.provismet.cobblemon.daycareplus.item.EggBagItem;
import com.provismet.cobblemon.daycareplus.item.FertilityBoosterItem;
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
    public static final PokemonEggItem POKEMON_EGG = register("pokemon_egg",
        (settings, vanillaItem, modelData) ->
            new PokemonEggItem(settings.maxCount(1).maxDamage(100), vanillaItem, modelData, PolymerResourcePackUtils.requestModel(vanillaItem, DaycarePlusServer.identifier("pokemon_egg_shiny").withPrefixedPath("item/")))
    );
    public static final FertilityBoosterItem FERTILITY_CANDY = register("fertility_candy", FertilityBoosterItem::new);

    public static final EggBagItem LEATHER_EGG_BAG = registerBag("leather_egg_bag", "leather", Options.getLeather());
    public static final EggBagItem IRON_EGG_BAG = registerBag("iron_egg_bag", "iron", Options.getIron());
    public static final EggBagItem GOLD_EGG_BAG = registerBag("gold_egg_bag", "gold", Options.getGold());
    public static final EggBagItem DIAMOND_EGG_BAG = registerBag("diamond_egg_bag", "diamond", Options.getDiamond());
    public static final EggBagItem NETHERITE_EGG_BAG = registerBag("netherite_egg_bag", "netherite", Options.getNetherite());

    private static <T extends PolymerItem> T register (String name, ItemConstructor<T> constructor) {
        Identifier itemId = DaycarePlusServer.identifier(name);
        PolymerModelData model = PolymerResourcePackUtils.requestModel(Items.IRON_NUGGET, itemId.withPrefixedPath("item/"));
        return Registry.register(Registries.ITEM, itemId, constructor.apply(new Item.Settings(), Items.IRON_NUGGET, model));
    }

    private static EggBagItem registerBag (String name, String bagTier, Options.EggBagSettings eggBagSettings) {
        return register(name, (settings, vanillaItem, modelData) -> new EggBagItem(
            settings
                .maxCount(1)
                .component(DPItemDataComponents.HELD_EGGS, new EggBagDataComponent(eggBagSettings.capacity(), bagTier)),
            vanillaItem,
            modelData,
            eggBagSettings.eggsToTick()
        ));
    }

    public static void init () {
        DPIconItems.init();
    }

    @FunctionalInterface
    public interface ItemConstructor<T extends PolymerItem> {
        T apply (Item.Settings settings, Item vanillaItem, PolymerModelData modelData);
    }
}

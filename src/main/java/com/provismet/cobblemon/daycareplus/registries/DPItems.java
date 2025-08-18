package com.provismet.cobblemon.daycareplus.registries;

import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.item.DaycareBoosterItem;
import com.provismet.cobblemon.daycareplus.item.DaycareSparkItem;
import com.provismet.cobblemon.daycareplus.item.EggBagItem;
import com.provismet.cobblemon.daycareplus.item.IncubatorItem;
import com.provismet.cobblemon.daycareplus.item.FertilityBoosterItem;
import com.provismet.cobblemon.daycareplus.item.PokemonEggItem;
import com.provismet.cobblemon.daycareplus.item.PolymerItem;
import com.provismet.cobblemon.daycareplus.item.component.IncubatorType;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import java.util.function.Function;

public abstract class DPItems {
    public static final PokemonEggItem POKEMON_EGG = register("pokemon_egg",
        (settings, vanillaItem, modelData) ->
            new PokemonEggItem(settings.maxCount(1).maxDamage(100), vanillaItem, modelData, PolymerResourcePackUtils.requestModel(vanillaItem, DaycarePlusServer.identifier("pokemon_egg_shiny").withPrefixedPath("item/")))
    );
    public static final FertilityBoosterItem FERTILITY_CANDY = register("fertility_candy", FertilityBoosterItem::new);
    public static final DaycareSparkItem DAYCARE_SPARK = register("daycare_spark", DaycareSparkItem::new);
    public static final DaycareBoosterItem DAYCARE_BOOSTER = register("daycare_booster", (settings, vanillaItem, modelData) -> new DaycareBoosterItem(settings.component(DPItemDataComponents.BOOST_AMOUNT, 5), vanillaItem, modelData));

    public static final IncubatorItem COPPER_INCUBATOR = registerIncubator("copper_incubator", IncubatorType.ofMain("copper"));
    public static final IncubatorItem IRON_INCUBATOR = registerIncubator("iron_incubator", IncubatorType.ofMain("iron"));
    public static final IncubatorItem GOLD_INCUBATOR = registerIncubator("gold_incubator", new IncubatorType("gold", "gold"));
    public static final IncubatorItem DIAMOND_INCUBATOR = registerIncubator("diamond_incubator", IncubatorType.ofMain("diamond"));
    public static final IncubatorItem NETHERITE_INCUBATOR = registerIncubator("netherite_incubator", IncubatorType.ofMain("netherite"), Item.Settings::fireproof);

    @Deprecated
    public static final EggBagItem LEATHER_EGG_BAG = registerBag("leather_egg_bag", COPPER_INCUBATOR);
    @Deprecated
    public static final EggBagItem IRON_EGG_BAG = registerBag("iron_egg_bag", IRON_INCUBATOR);
    @Deprecated
    public static final EggBagItem GOLD_EGG_BAG = registerBag("gold_egg_bag", GOLD_INCUBATOR);
    @Deprecated
    public static final EggBagItem DIAMOND_EGG_BAG = registerBag("diamond_egg_bag", DIAMOND_INCUBATOR);
    @Deprecated
    public static final EggBagItem NETHERITE_EGG_BAG = registerBag("netherite_egg_bag", NETHERITE_INCUBATOR);

    private static <T extends PolymerItem> T register (String name, ItemConstructor<T> constructor) {
        Identifier itemId = DaycarePlusServer.identifier(name);
        PolymerModelData model = PolymerResourcePackUtils.requestModel(Items.IRON_NUGGET, itemId.withPrefixedPath("item/"));
        return Registry.register(Registries.ITEM, itemId, constructor.apply(new Item.Settings(), Items.IRON_NUGGET, model));
    }

    @Deprecated
    private static EggBagItem registerBag (String name, Item to) {
        return register(name, (settings, vanillaItem, modelData) -> new EggBagItem(settings, vanillaItem, modelData, to));
    }

    private static IncubatorItem registerIncubator (String name, IncubatorType incubatorType, Function<Item.Settings, Item.Settings> settingsModifier) {
        PolymerModelData eggModel = PolymerResourcePackUtils.requestModel(Items.IRON_NUGGET, DaycarePlusServer.identifier(name).withPrefixedPath("item/").withSuffixedPath("_full"));
        return register(name, (settings, vanillaItem, modelData) -> new IncubatorItem(
            settingsModifier.apply(settings)
                .maxCount(1)
                .component(DPItemDataComponents.INCUBATOR_TYPE, incubatorType),
            vanillaItem,
            modelData,
            eggModel
        ));
    }

    private static IncubatorItem registerIncubator (String name, IncubatorType incubatorType) {
        return registerIncubator(name, incubatorType, settings -> settings);
    }

    public static void init () {
        DPIconItems.init();
    }

    @FunctionalInterface
    public interface ItemConstructor<T extends PolymerItem> {
        T apply (Item.Settings settings, Item vanillaItem, PolymerModelData modelData);
    }
}

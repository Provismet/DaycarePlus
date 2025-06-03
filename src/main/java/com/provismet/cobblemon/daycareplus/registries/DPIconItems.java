package com.provismet.cobblemon.daycareplus.registries;

import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.item.PolymerItem;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

// Just to separate fake icon items from normal items.
public abstract class DPIconItems {
    public static final PolymerItem INFO = register("info", PolymerItem::new);
    public static final PolymerItem LEFT = register("left_arrow", PolymerItem::new);
    public static final PolymerItem RIGHT = register("right_arrow", PolymerItem::new);

    private static <T extends PolymerItem> T register (String name, DPItems.ItemConstructor<T> constructor) {
        Identifier itemId = DaycarePlusServer.identifier(name);
        PolymerModelData model = PolymerResourcePackUtils.requestModel(Items.FLINT, itemId.withPrefixedPath("item/"));
        return Registry.register(Registries.ITEM, itemId, constructor.apply(new Item.Settings(), Items.FLINT, model));
    }

    public static void init () {}
}

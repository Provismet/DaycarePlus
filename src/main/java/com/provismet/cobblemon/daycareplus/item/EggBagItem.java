package com.provismet.cobblemon.daycareplus.item;

import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;

import java.util.List;

@Deprecated
public class EggBagItem extends IncubatorItem {
    public EggBagItem (Settings settings, Item baseVanillaItem, PolymerModelData modelData, int eggsToTick) {
        super(settings, baseVanillaItem, modelData, modelData, eggsToTick);
    }

    @Override
    public void appendTooltip (ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.literal("Bags are being rebranded as incubators now!"));
        tooltip.add(Text.literal("Place this item in the crafting grid to update and reset it."));
        tooltip.add(Text.literal("This will reset its data, empty it or hatch your eggs first!"));
    }
}

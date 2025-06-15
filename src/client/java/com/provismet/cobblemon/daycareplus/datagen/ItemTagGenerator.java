package com.provismet.cobblemon.daycareplus.datagen;

import com.cobblemon.mod.common.CobblemonItems;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import com.provismet.cobblemon.daycareplus.util.tag.DPItemTags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends FabricTagProvider.ItemTagProvider {
    public ItemTagGenerator (FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture, null);
    }

    @Override
    protected void configure (RegistryWrapper.WrapperLookup wrapperLookup) {
        this.getOrCreateTagBuilder(DPItemTags.BREEDING_ITEM)
            .add(CobblemonItems.EVERSTONE)
            .add(CobblemonItems.DESTINY_KNOT)
            .add(CobblemonItems.POWER_ANKLET)
            .add(CobblemonItems.POWER_BAND)
            .add(CobblemonItems.POWER_BELT)
            .add(CobblemonItems.POWER_BRACER)
            .add(CobblemonItems.POWER_LENS)
            .add(CobblemonItems.POWER_WEIGHT)
            .add(CobblemonItems.MIRROR_HERB);

        this.getOrCreateTagBuilder(DPItemTags.EGG_BAGS)
            .add(DPItems.LEATHER_EGG_BAG)
            .add(DPItems.IRON_EGG_BAG)
            .add(DPItems.GOLD_EGG_BAG)
            .add(DPItems.DIAMOND_EGG_BAG)
            .add(DPItems.NETHERITE_EGG_BAG);
    }
}

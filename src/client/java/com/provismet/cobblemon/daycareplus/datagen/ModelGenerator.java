package com.provismet.cobblemon.daycareplus.datagen;

import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.registries.DPIconItems;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.data.client.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class ModelGenerator extends FabricModelProvider {
    public ModelGenerator (FabricDataOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels (BlockStateModelGenerator blockStateModelGenerator) {

    }

    @Override
    public void generateItemModels (ItemModelGenerator itemModelGenerator) {
        Consumer<Item> generated = item -> itemModelGenerator.register(item, Models.GENERATED);
        Consumer<Item> incubator = item -> {
            Identifier id = Registries.ITEM.getId(item).withPrefixedPath("item/").withSuffixedPath("_full");
            generated.accept(item);
            Models.GENERATED.upload(
                id,
                TextureMap.layer0(id),
                itemModelGenerator.writer
            );
        };

        generated.accept(DPItems.POKEMON_EGG);
        Models.GENERATED.upload(
            DaycarePlusServer.identifier("item/pokemon_egg_shiny"),
            TextureMap.layer0(DaycarePlusServer.identifier("item/pokemon_egg_shiny")),
            itemModelGenerator.writer
        );
        generated.accept(DPItems.FERTILITY_CANDY);

        incubator.accept(DPItems.COPPER_INCUBATOR);
        incubator.accept(DPItems.IRON_INCUBATOR);
        incubator.accept(DPItems.GOLD_INCUBATOR);
        incubator.accept(DPItems.DIAMOND_INCUBATOR);
        incubator.accept(DPItems.NETHERITE_INCUBATOR);

        generated.accept(DPItems.LEATHER_EGG_BAG);
        generated.accept(DPItems.IRON_EGG_BAG);
        generated.accept(DPItems.GOLD_EGG_BAG);
        generated.accept(DPItems.DIAMOND_EGG_BAG);
        generated.accept(DPItems.NETHERITE_EGG_BAG);

        generated.accept(DPIconItems.INFO);
        generated.accept(DPIconItems.LEFT);
        generated.accept(DPIconItems.RIGHT);
        generated.accept(DPIconItems.TAKE_ALL);
    }
}

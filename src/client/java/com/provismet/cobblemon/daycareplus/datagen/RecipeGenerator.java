package com.provismet.cobblemon.daycareplus.datagen;

import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.item.EggBagItem;
import com.provismet.cobblemon.daycareplus.item.IncubatorItem;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class RecipeGenerator extends FabricRecipeProvider {
    public RecipeGenerator (FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void generate (RecipeExporter recipeExporter) {
        ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, DPItems.COPPER_INCUBATOR)
            .pattern("iii")
            .pattern("grg")
            .pattern("iii")
            .input('g', Items.GLASS)
            .input('r', Items.BLAZE_POWDER)
            .input('i', Items.COPPER_BLOCK)
            .criterion(FabricRecipeProvider.hasItem(DPItems.POKEMON_EGG), FabricRecipeProvider.conditionsFromItem(DPItems.POKEMON_EGG))
            .offerTo(recipeExporter); // Unlock the recipe after collecting an egg.

        this.convert(DPItems.LEATHER_EGG_BAG, DPItems.COPPER_INCUBATOR).offerTo(recipeExporter, DaycarePlusServer.identifier("copper_convert"));
        this.convert(DPItems.IRON_EGG_BAG, DPItems.IRON_INCUBATOR).offerTo(recipeExporter, DaycarePlusServer.identifier("iron_convert"));
        this.convert(DPItems.GOLD_EGG_BAG, DPItems.GOLD_INCUBATOR).offerTo(recipeExporter, DaycarePlusServer.identifier("gold_convert"));
        this.convert(DPItems.DIAMOND_EGG_BAG, DPItems.DIAMOND_INCUBATOR).offerTo(recipeExporter, DaycarePlusServer.identifier("diamond_convert"));
        this.convert(DPItems.NETHERITE_EGG_BAG, DPItems.NETHERITE_INCUBATOR).offerTo(recipeExporter, DaycarePlusServer.identifier("netherite_convert"));

        this.incubator(DPItems.IRON_INCUBATOR, Items.IRON_BLOCK, DPItems.COPPER_INCUBATOR).offerTo(recipeExporter);
        this.incubator(DPItems.GOLD_INCUBATOR, Items.GOLD_BLOCK, DPItems.IRON_INCUBATOR).offerTo(recipeExporter); // Non-linear progression.
        this.incubator(DPItems.DIAMOND_INCUBATOR, Items.DIAMOND_BLOCK, DPItems.IRON_INCUBATOR).offerTo(recipeExporter);
        this.incubator(DPItems.NETHERITE_INCUBATOR, Items.NETHERITE_INGOT, DPItems.DIAMOND_INCUBATOR).offerTo(recipeExporter); // Do not use upgrades, they carry over components!
    }

    private ShapedRecipeJsonBuilder incubator (Item bag, Item inputMaterial, Item previousIncubator) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, bag)
            .pattern("iii")
            .pattern("grg")
            .pattern("iii")
            .input('g', Items.GLASS)
            .input('r', previousIncubator)
            .input('i', inputMaterial)
            .criterion(FabricRecipeProvider.hasItem(previousIncubator), FabricRecipeProvider.conditionsFromItem(previousIncubator));
    }

    private ShapelessRecipeJsonBuilder convert (EggBagItem from, IncubatorItem to) {
        return ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, to)
            .input(from)
            .criterion(FabricRecipeProvider.hasItem(from), FabricRecipeProvider.conditionsFromItem(from));
    }
}

package com.provismet.cobblemon.daycareplus.datagen;

import com.provismet.cobblemon.daycareplus.registries.DPItems;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeExporter;
import net.minecraft.data.server.recipe.RecipeProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
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
        this.eggBag(DPItems.LEATHER_EGG_BAG, Items.LEATHER).offerTo(recipeExporter);
        this.eggBag(DPItems.IRON_EGG_BAG, Items.IRON_BLOCK).offerTo(recipeExporter);
        this.eggBag(DPItems.GOLD_EGG_BAG, Items.GOLD_BLOCK).offerTo(recipeExporter);
        this.eggBag(DPItems.DIAMOND_EGG_BAG, Items.DIAMOND_BLOCK).offerTo(recipeExporter);
        RecipeProvider.offerNetheriteUpgradeRecipe(recipeExporter, DPItems.DIAMOND_EGG_BAG, RecipeCategory.MISC, DPItems.NETHERITE_EGG_BAG);
    }

    private ShapedRecipeJsonBuilder eggBag (Item bag, Item inputMaterial) {
        return ShapedRecipeJsonBuilder.create(RecipeCategory.MISC, bag)
            .pattern(" s ")
            .pattern("iri")
            .pattern("iii")
            .input('s', Items.STRING)
            .input('r', Items.RABBIT_HIDE)
            .input('i', inputMaterial)
            .criterion(FabricRecipeProvider.hasItem(inputMaterial), FabricRecipeProvider.conditionsFromItem(inputMaterial))
            .criterion(FabricRecipeProvider.hasItem(Items.RABBIT_HIDE), FabricRecipeProvider.conditionsFromItem(Items.RABBIT_HIDE));
    }
}

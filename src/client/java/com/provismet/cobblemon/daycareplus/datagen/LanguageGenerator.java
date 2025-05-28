package com.provismet.cobblemon.daycareplus.datagen;

import com.provismet.cobblemon.daycareplus.registries.DPItems;
import com.provismet.lilylib.datagen.provider.LilyLanguageProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class LanguageGenerator extends LilyLanguageProvider {
    protected LanguageGenerator (FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generateTranslations (RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("title.daycareplus.item_group", "Daycare+");

        translationBuilder.add(DPItems.POKEMON_EGG, "Pokemon Egg");
        translationBuilder.add(DPItems.LEATHER_EGG_BAG, "Leather Egg Bag");
        translationBuilder.add(DPItems.IRON_EGG_BAG, "Iron Egg Bag");
        translationBuilder.add(DPItems.GOLD_EGG_BAG, "Gold Egg Bag");
        translationBuilder.add(DPItems.DIAMOND_EGG_BAG, "Diamond Egg Bag");
        translationBuilder.add(DPItems.NETHERITE_EGG_BAG, "Netherite Egg Bag");

        // Egg Bag
        translationBuilder.add("message.overlay.daycareplus.egg_bag.collection.singular", "Collected %1$s egg.");
        translationBuilder.add("message.overlay.daycareplus.egg_bag.collection.plural", "Collected %1$s eggs.");
        translationBuilder.add("tooltip.daycareplus.egg_bag.eggs_held", "Eggs Held: %1$s/%2$s");

        // Egg Item
        translationBuilder.add("message.overlay.daycareplus.egg.hatch", "Your egg hatched.");
        translationBuilder.add("tooltip.daycareplus.egg.no_data", "No data found.");

        // Daycare
        translationBuilder.add("message.chat.daycareplus.egg_produced", "Your daycare has produced an egg.");
        translationBuilder.add("gui.button.daycareplus.open_pasture", "Open Pasture");
        translationBuilder.add("gui.button.daycareplus.info", "Info");

        // Properties
        translationBuilder.add("property.daycareplus.species", "Species: %1$s");
        translationBuilder.add("property.daycareplus.form", "Form: %1$s");
        translationBuilder.add("property.daycareplus.ability", "Ability: %1$s");
        translationBuilder.add("property.daycareplus.nature", "Nature: %1$s");
        translationBuilder.add("property.daycareplus.hp", "HP: %1$s");
        translationBuilder.add("property.daycareplus.attack", "Attack: %1$s");
        translationBuilder.add("property.daycareplus.defence", "Defence: %1$s");
        translationBuilder.add("property.daycareplus.special_attack", "Sp.Attack: %1$s");
        translationBuilder.add("property.daycareplus.special_defence", "Sp.Defence: %1$s");
        translationBuilder.add("property.daycareplus.speed", "Speed: %1$s");
    }
}

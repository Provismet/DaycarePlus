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
        translationBuilder.add("tooltip.daycareplus.egg.ticks", "Time: %1$s");

        // Daycare
        translationBuilder.add("message.chat.daycareplus.egg_produced", "Your daycare has produced an egg.");
        translationBuilder.add("message.chat.daycareplus.multiple_egg_produced", "Your daycare produced %1$s eggs while you were away.");
        translationBuilder.add("message.overlay.daycareplus.not_owner", "This is not your daycare.");
        translationBuilder.add("gui.button.daycareplus.open_pasture", "Open Pasture");
        translationBuilder.add("gui.button.daycareplus.info", "Info");
        translationBuilder.add("gui.button.daycareplus.info.tooltip.1", "The daycare attempt to produce an egg periodically.");
        translationBuilder.add("gui.button.daycareplus.info.tooltip.2", "Eggs will still be produced when the pasture is unloaded or the owner is offline.");
        translationBuilder.add("gui.button.daycareplus.offspring", "Offspring");
        translationBuilder.add("gui.button.daycareplus.eggs_held", "%1$s/%2$s eggs held");
        translationBuilder.add("gui.button.daycareplus.no_parent", "No parent selected.");
        translationBuilder.add("gui.button.daycareplus.no_parent.tooltip", "Add a pokemon to the pasture.");
        translationBuilder.add("gui.button.daycareplus.offspring.empty", "No preview available.");
        translationBuilder.add("gui.button.daycareplus.offspring.empty.tooltip", "Select two compatible Pokemon to view the preview.");

        // Intro GUI
        translationBuilder.add("gui.button.daycareplus.intro.daycare", "Daycare");
        translationBuilder.add("gui.button.daycareplus.intro.daycare.tooltip", "Use this pasture to breed Pokemon.");
        translationBuilder.add("gui.button.daycareplus.intro.pasture", "Pasture");
        translationBuilder.add("gui.button.daycareplus.intro.pasture.tooltip", "Use this pasture cosmetically without breeding.");

        // Properties
        translationBuilder.add("property.daycareplus.species", "Species: ");
        translationBuilder.add("property.daycareplus.form", "Form: ");
        translationBuilder.add("property.daycareplus.ability", "Ability: ");
        translationBuilder.add("property.daycareplus.nature", "Nature: ");
        translationBuilder.add("property.daycareplus.ivs", "IVs ");
        translationBuilder.add("property.daycareplus.hp", "HP: ");
        translationBuilder.add("property.daycareplus.attack", "Attack: ");
        translationBuilder.add("property.daycareplus.defence", "Defence: ");
        translationBuilder.add("property.daycareplus.special_attack", "Sp.Attack: ");
        translationBuilder.add("property.daycareplus.special_defence", "Sp.Defence: ");
        translationBuilder.add("property.daycareplus.speed", "Speed: ");
        translationBuilder.add("property.daycareplus.shiny", "Shiny Chance: ");
    }
}

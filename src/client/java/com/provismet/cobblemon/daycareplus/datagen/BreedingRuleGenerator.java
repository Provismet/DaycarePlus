package com.provismet.cobblemon.daycareplus.datagen;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.pokemon.Gender;
import com.provismet.cobblemon.daycareplus.api.BreedingRulesProvider;
import com.provismet.cobblemon.daycareplus.api.codec.BreedingRules;
import com.provismet.cobblemon.lilycobble.pokemon.FeatureApplicator;
import com.provismet.cobblemon.lilycobble.pokemon.PokemonPredicate;
import com.provismet.cobblemon.lilycobble.pokemon.PokemonSupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class BreedingRuleGenerator extends BreedingRulesProvider {
    public BreedingRuleGenerator (FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void generate (RegistryWrapper.WrapperLookup wrapperLookup, BreedingRulesConsumer consumer) {
        // Generic
        consumer.simple("manaphy", "phione");

        consumer.add("nidoranf", BreedingRules.of(BreedingRules.Rule.builder()
            .addOffspring(PokemonSupplier.builder().species("nidoranf"))
            .addOffspring(PokemonSupplier.builder().species("nidoranm"))));
        consumer.add("nidoranm", BreedingRules.of(BreedingRules.Rule.builder()
            .addOffspring(PokemonSupplier.builder().species("nidoranf"))
            .addOffspring(PokemonSupplier.builder().species("nidoranm"))));

        consumer.add("volbeat", BreedingRules.of(BreedingRules.Rule.builder()
            .addOffspring(PokemonSupplier.builder().species("volbeat"))
            .addOffspring(PokemonSupplier.builder().species("illumise"))));
        consumer.add("illumise", BreedingRules.of(BreedingRules.Rule.builder()
            .addOffspring(PokemonSupplier.builder().species("volbeat"))
            .addOffspring(PokemonSupplier.builder().species("illumise"))));

        consumer.add("indeedee", BreedingRules.of(BreedingRules.Rule.builder()
            .addOffspring(PokemonSupplier.builder()
                .species("indeedee")
                .gender(Gender.MALE))
            .addOffspring(PokemonSupplier.builder()
                .species("indeedee")
                .gender(Gender.FEMALE))));

        consumer.add("pikachu", BreedingRules.builder()
            .add(BreedingRules.Rule.builder()
                .primaryParent(PokemonPredicate.builder()
                    .species("pikachu")
                    .heldItem(CobblemonItems.LIGHT_BALL))
                .addOffspring(PokemonSupplier.builder()
                    .species("pichu")
                    .addMove("volttackle")))
            .add(BreedingRules.Rule.builder()
                .secondaryParent(PokemonPredicate.builder()
                    .species("pikachu")
                    .heldItem(CobblemonItems.LIGHT_BALL))
                .addOffspring(PokemonSupplier.builder()
                    .species("pichu")
                    .addMove("volttackle"))));

        // Alola Forms
        consumer.simple("exeggutor", "alola", "exeggcute", FeatureApplicator.single("region_bias", "alola"));
        consumer.simple("marowak", "alola", "cubone", FeatureApplicator.single("region_bias", "alola"));
        consumer.simple("raichu", "alola", "pichu", FeatureApplicator.single("region_bias", "alola"));

        // Galar Forms
        consumer.simple("cursola", "corsola", FeatureApplicator.single("galarian", true));
        consumer.simple("mrmime", "galar", "mimejr", FeatureApplicator.single("region_bias", "galar"));
        consumer.simple("obstagoon", "zigzagoon", FeatureApplicator.single("galarian", true));
        consumer.simple("perrserker", "meowth", FeatureApplicator.single("galarian", true));
        consumer.simple("runerigus", "yamask", FeatureApplicator.single("galarian", true));
        consumer.simple("sirfetchd", "farfetchd", FeatureApplicator.single("galarian", true));
        consumer.simple("weezing", "galar", "koffing", FeatureApplicator.single("region_bias", "galar"));

        // Hisui Forms
        consumer.simple("avalugg", "hisui", "bergmite", FeatureApplicator.single("region_bias", "hisui"));
        consumer.simple("basculegion", "basculin", FeatureApplicator.single("fish_stripes", "white"));
        consumer.simple("braviary", "hisui", "rufflet", FeatureApplicator.single("region_bias", "hisui"));
        consumer.simple("decidueye", "hisui", "rowlet", FeatureApplicator.single("region_bias", "hisui"));
        consumer.simple("lilligant", "hisui", "petilil", FeatureApplicator.single("region_bias", "hisui"));
        consumer.simple("overqwil", "qwilfish", FeatureApplicator.single("hisuian", true));
        consumer.simple("sneasler", "sneasel", FeatureApplicator.single("hisuian", true));
        consumer.simple("samurott", "hisui", "oshawott", FeatureApplicator.single("region_bias", "hisui"));
        consumer.simple("goodra", "hisui", "goomy", FeatureApplicator.single("region_bias", "hisui"));
        consumer.simple("sliggoo", "hisui", "goomy", FeatureApplicator.single("region_bias", "hisui"));
        consumer.simple("typhlosion", "hisui", "cyndaquil", FeatureApplicator.single("region_bias", "hisui"));

        // Paldea Forms
        consumer.simple("clodsire", "wooper", FeatureApplicator.single("paldean", true));
    }
}

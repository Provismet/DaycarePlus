package com.provismet.cobblemon.daycareplus.api;

import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.MiscUtilsKt;
import com.provismet.cobblemon.daycareplus.api.codec.BreedingRules;
import com.provismet.cobblemon.daycareplus.registries.DPDynamicRegistryKeys;
import com.provismet.cobblemon.lilycobble.pokemon.FeatureApplicator;
import com.provismet.cobblemon.lilycobble.pokemon.PokemonPredicate;
import com.provismet.cobblemon.lilycobble.pokemon.PokemonSupplier;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public abstract class BreedingRulesProvider extends FabricDynamicRegistryProvider {
    public BreedingRulesProvider (FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void configure (RegistryWrapper.WrapperLookup wrapperLookup, Entries entries) {
        BreedingRulesConsumer consumer = new BreedingRulesConsumer(entries);
        this.generate(wrapperLookup, consumer);
    }

    @Override
    public String getName() {
        return "breeding rules";
    }

    protected abstract void generate (RegistryWrapper.WrapperLookup wrapperLookup, BreedingRulesConsumer consumer);

    protected static class BreedingRulesConsumer {
        private final Entries entries;

        private BreedingRulesConsumer (Entries entries) {
            this.entries = entries;
        }

        public void add (Identifier identifier, BreedingRules rules) {
            this.entries.add(RegistryKey.of(DPDynamicRegistryKeys.BREEDING_RULES, identifier), rules);
        }

        public void add (Identifier identifier, Supplier<BreedingRules> rules) {
            this.add(identifier, rules.get());
        }

        public void add (Species species, BreedingRules rules) {
            this.add(species.resourceIdentifier, rules);
        }

        public void add (Species species, Supplier<BreedingRules> rules) {
            this.add(species.resourceIdentifier, rules);
        }

        public void add (String species, BreedingRules rules) {
            this.add(MiscUtilsKt.cobblemonResource(species), rules);
        }

        public void add (String species, Supplier<BreedingRules> rules) {
            this.add(MiscUtilsKt.cobblemonResource(species), rules);
        }

        public void simple (String parentSpecies, String childSpecies) {
            this.add(parentSpecies,
                BreedingRules.of(BreedingRules.Rule.builder()
                    .addOffspring(BreedingRules.PotentialOffspring.builder()
                        .pokemon(PokemonSupplier.builder()
                            .species(childSpecies))))
            );
        }

        public void simple (String parentSpecies, String childSpecies, FeatureApplicator features) {
            this.add(parentSpecies,
                BreedingRules.of(BreedingRules.Rule.builder()
                    .addOffspring(BreedingRules.PotentialOffspring.builder()
                        .pokemon(PokemonSupplier.builder()
                            .species(childSpecies)
                            .features(features))))
            );
        }

        public void simple (String parentSpecies, String parentForm, String childSpecies, FeatureApplicator childFeatures) {
            this.add(parentSpecies,
                BreedingRules.of(BreedingRules.Rule.builder()
                    .primaryParent(PokemonPredicate.builder()
                        .form(parentForm))
                    .addOffspring(BreedingRules.PotentialOffspring.builder()
                        .pokemon(PokemonSupplier.builder()
                            .species(childSpecies)
                            .features(childFeatures))))
            );
        }
    }
}

package com.provismet.cobblemon.daycareplus.api.codec;

import com.cobblemon.mod.common.pokemon.Pokemon;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.provismet.cobblemon.daycareplus.breeding.BreedingUtils;
import com.provismet.cobblemon.lilycobble.pokemon.PokemonPredicate;
import com.provismet.cobblemon.lilycobble.pokemon.PokemonSupplier;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

public record BreedingRules (List<Rule> rules) {
    public static final Codec<BreedingRules> CODEC = Rule.CODEC.listOf().xmap(BreedingRules::new, BreedingRules::rules);
    private static final Random random = new Random();

    public static Builder builder () {
        return new Builder();
    }

    public static BreedingRules of (Rule rule) {
        return builder().add(rule).get();
    }

    public static BreedingRules of (Supplier<Rule> rule) {
        return builder().add(rule).get();
    }

    @Nullable
    public Rule getRule (Pokemon primary, Pokemon secondary) {
        boolean compatibleEggGroup = BreedingUtils.isCompatibleEggGroup(primary, secondary);
        boolean compatibleGender = BreedingUtils.isCompatibleGender(primary, secondary);

        for (Rule rule : this.rules) {
            if ((compatibleEggGroup || rule.bypassEggGroups()) && (compatibleGender || rule.bypassGender()) && rule.test(primary, secondary)) {
                return rule;
            }
        }

        return null;
    }

    @Nullable
    public PokemonSupplier getOffspring (Pokemon primary, Pokemon secondary) {
        Rule rule = this.getRule(primary, secondary);
        if (rule == null) return null;

        return rule.chooseOffspring();
    }

    public static class Builder implements Supplier<BreedingRules> {
        private final List<Rule> rules = new ArrayList<>();

        public Builder add (Rule rule) {
            this.rules.add(rule);
            return this;
        }

        public Builder add (Supplier<Rule> rule) {
            this.rules.add(rule.get());
            return this;
        }

        @Override
        public BreedingRules get () {
            if (this.rules.isEmpty()) {
                throw new IllegalStateException("Tried to build an empty BreedingRules object.");
            }
            return new BreedingRules(this.rules);
        }
    }

    public record Rule (PokemonPredicate primaryPredicate, PokemonPredicate secondaryPredicate, boolean bypassEggGroups, boolean bypassGender, List<PotentialOffspring> offspring) implements BiPredicate<Pokemon, Pokemon> {
        public static final Codec<Rule> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PokemonPredicate.CODEC.optionalFieldOf("primary_parent", PokemonPredicate.TRUE).forGetter(Rule::primaryPredicate),
            PokemonPredicate.CODEC.optionalFieldOf("secondary_parent", PokemonPredicate.TRUE).forGetter(Rule::secondaryPredicate),
            Codec.BOOL.optionalFieldOf("bypass_egg_group", false).forGetter(Rule::bypassEggGroups),
            Codec.BOOL.optionalFieldOf("bypass_gender", false).forGetter(Rule::bypassGender),
            PotentialOffspring.ALT_CODEC.listOf().fieldOf("offspring").forGetter(Rule::offspring)
        ).apply(instance, Rule::new));

        public Rule (PokemonPredicate primaryPredicate, PokemonPredicate secondaryPredicate, PotentialOffspring offspring) {
            this(primaryPredicate, secondaryPredicate, false, false, List.of(offspring));
        }

        public Rule (PokemonPredicate primaryPredicate, List<PotentialOffspring> offspring) {
            this(primaryPredicate, PokemonPredicate.TRUE, false, false, offspring);
        }

        public Rule (PokemonPredicate primaryPredicate, PotentialOffspring offspring) {
            this(primaryPredicate, List.of(offspring));
        }

        public static Builder builder () {
            return new Builder();
        }

        @Override
        public boolean test (Pokemon primary, Pokemon secondary) {
            return this.primaryPredicate.test(primary) && this.secondaryPredicate.test(secondary);
        }

        @Nullable
        public PokemonSupplier chooseOffspring () {
            if (this.offspring.isEmpty()) return null;

            int totalWeight = 0;
            for (PotentialOffspring potential : this.offspring) {
                totalWeight += potential.weight();
            }

            float roll = random.nextFloat(totalWeight);
            for (PotentialOffspring potential : this.offspring) {
                if (roll < potential.weight()) return potential.pokemon();

                roll -= potential.weight();
            }

            return this.offspring.getLast().pokemon();
        }

        public static class Builder implements Supplier<Rule> {
            private PokemonPredicate primaryPredicate = PokemonPredicate.TRUE;
            private PokemonPredicate secondaryPredicate = PokemonPredicate.TRUE;
            private boolean bypassEggGroups = false;
            private boolean bypassGender = false;
            private List<PotentialOffspring> offspring;

            public Builder primaryParent (PokemonPredicate primary) {
                this.primaryPredicate = primary;
                return this;
            }

            public Builder primaryParent (Supplier<PokemonPredicate> primary) {
                this.primaryPredicate = primary.get();
                return this;
            }

            public Builder secondaryParent (PokemonPredicate secondary) {
                this.secondaryPredicate = secondary;
                return this;
            }

            public Builder secondaryParent (Supplier<PokemonPredicate> secondary) {
                this.secondaryPredicate = secondary.get();
                return this;
            }

            public Builder bypassEggGroups (boolean bypass) {
                this.bypassEggGroups = bypass;
                return this;
            }

            public Builder bypassGender (boolean bypass) {
                this.bypassGender = bypass;
                return this;
            }

            public Builder addOffspring (PotentialOffspring offspring) {
                if (this.offspring == null) this.offspring = new ArrayList<>();

                this.offspring.add(offspring);
                return this;
            }

            public Builder addOffspring (Supplier<PotentialOffspring> offspring) {
                if (this.offspring == null) this.offspring = new ArrayList<>();

                this.offspring.add(offspring.get());
                return this;
            }

            @Override
            public Rule get () {
                return new Rule(
                    Objects.requireNonNull(this.primaryPredicate),
                    Objects.requireNonNull(this.secondaryPredicate),
                    this.bypassEggGroups,
                    this.bypassGender,
                    Objects.requireNonNull(this.offspring)
                );
            }
        }
    }

    public record PotentialOffspring (int weight, PokemonSupplier pokemon) {
        public static final Codec<PotentialOffspring> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(PotentialOffspring::weight),
            PokemonSupplier.CODEC.fieldOf("pokemon").forGetter(PotentialOffspring::pokemon)
        ).apply(instance, PotentialOffspring::new));

        public static final Codec<PotentialOffspring> ALT_CODEC = Codec.either(PotentialOffspring.CODEC, PokemonSupplier.CODEC)
            .xmap(either -> either.map(l -> l, PotentialOffspring::new), Either::left);

        public PotentialOffspring (PokemonSupplier supplier) {
            this(1, supplier);
        }

        public static PotentialOffspring of (PokemonSupplier supplier) {
            return new PotentialOffspring(supplier);
        }

        public static PotentialOffspring of (Supplier<PokemonSupplier> supplier) {
            return new PotentialOffspring(supplier.get());
        }

        public static Builder builder () {
            return new Builder();
        }

        public static class Builder implements Supplier<PotentialOffspring> {
            int weight = 1;
            PokemonSupplier supplier;

            public Builder weight (int weight) {
                this.weight = weight;
                return this;
            }

            public Builder pokemon (PokemonSupplier pokemon) {
                this.supplier = pokemon;
                return this;
            }

            public Builder pokemon (Supplier<PokemonSupplier> pokemon) {
                this.supplier = pokemon.get();
                return this;
            }

            @Override
            public PotentialOffspring get () {
                return new PotentialOffspring(
                    this.weight,
                    Objects.requireNonNull(this.supplier)
                );
            }
        }
    }
}

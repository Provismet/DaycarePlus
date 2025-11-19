package com.provismet.cobblemon.daycareplus.feature;

import com.cobblemon.mod.common.api.pokemon.feature.GlobalSpeciesFeatures;
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.IntSpeciesFeatureProvider;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureProvider;
import com.cobblemon.mod.common.net.messages.client.pokemon.update.SpeciesFeatureUpdatePacket;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.provismet.cobblemon.daycareplus.config.DaycarePlusOptions;

/**
 * Fertility is a global feature, but its conditional nature means it requires special handling.
 */
public class FertilityProperty {
    public static final String KEY = "fertility";

    public static int get (Pokemon pokemon) {
        if (!DaycarePlusOptions.doCompetitiveBreeding()) return getMax();

        IntSpeciesFeature fertilityFeature = pokemon.getFeature(KEY);
        if (fertilityFeature == null) {
            return getMax();
        }

        return fertilityFeature.getValue();
    }

    public static int getMax () {
        SpeciesFeatureProvider<? extends SpeciesFeature> fertilityProvider = GlobalSpeciesFeatures.getFeature(KEY);
        if (fertilityProvider instanceof IntSpeciesFeatureProvider intProvider) {
            return intProvider.getMax();
        }

        // Implies the global feature doesn't exist.
        return 0;
    }

    public static void increment (Pokemon pokemon) {
        IntSpeciesFeature fertility = new IntSpeciesFeature(KEY, get(pokemon) + 1);
        fertility.apply(pokemon);
        update(pokemon, fertility);
    }

    public static void decrement (Pokemon pokemon) {
        IntSpeciesFeature fertility = new IntSpeciesFeature(KEY, get(pokemon) - 1);
        fertility.apply(pokemon);
        update(pokemon, fertility);
    }

    public static void update (Pokemon pokemon, SpeciesFeature feature) {
        if (DaycarePlusOptions.doCompetitiveBreeding()) {
            pokemon.markFeatureDirty(feature);
            pokemon.notify(new SpeciesFeatureUpdatePacket(
                () -> pokemon,
                pokemon.getSpecies().resourceIdentifier,
                new IntSpeciesFeature(KEY, get(pokemon))
            ));
        }
    }
}

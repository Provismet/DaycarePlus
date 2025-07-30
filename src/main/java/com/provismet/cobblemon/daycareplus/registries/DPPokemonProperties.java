package com.provismet.cobblemon.daycareplus.registries;

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.provismet.cobblemon.daycareplus.feature.BreedableProperty;

public class DPPokemonProperties {
    public static void register () {
        CustomPokemonProperty.Companion.register(new BreedableProperty());
    }
}

package com.provismet.cobblemon.daycareplus.registries;

import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.provismet.cobblemon.daycareplus.breeding.FertilityProperty;
import com.provismet.cobblemon.daycareplus.config.Options;

import java.util.Set;

public class DPPokemonProperties {
    public static void register () {
        CustomPokemonProperty.Companion.register(
            "fertility",
            true,
            stringValue -> {
                try {
                    return new FertilityProperty(Integer.parseInt(stringValue));
                }
                catch (NumberFormatException e) {
                    return null;
                }
            },
            () -> Set.of("0", String.valueOf(Options.getMaxFertility()))
        );
    }
}

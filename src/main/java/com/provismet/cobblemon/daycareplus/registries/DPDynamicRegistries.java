package com.provismet.cobblemon.daycareplus.registries;

import com.provismet.cobblemon.daycareplus.api.codec.BreedingRules;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;

public class DPDynamicRegistries {
    private static Registry<BreedingRules> breedingRules;

    public static Registry<BreedingRules> getBreedingRules () {
        return breedingRules;
    }

    public static void loadRegistries (DynamicRegistryManager registryAccess) {
        breedingRules = registryAccess.get(DPDynamicRegistryKeys.BREEDING_RULES);
    }

    public static void unloadRegistries () {
        breedingRules = null;
    }
}

package com.provismet.cobblemon.daycareplus.registries;

import com.provismet.cobblemon.daycareplus.DaycarePlusMain;
import com.provismet.cobblemon.daycareplus.api.codec.BreedingRules;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

public interface DPDynamicRegistryKeys {
    RegistryKey<Registry<BreedingRules>> BREEDING_RULES = RegistryKey.ofRegistry(DaycarePlusMain.identifier("breeding_rules"));

    static void register () {
        DynamicRegistries.register(BREEDING_RULES, BreedingRules.CODEC);
    }
}

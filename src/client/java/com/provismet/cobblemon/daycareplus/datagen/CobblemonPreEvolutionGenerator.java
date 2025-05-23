package com.provismet.cobblemon.daycareplus.datagen;

import com.provismet.cobblemon.daycareplus.api.PreEvolutionFormOverrideProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class CobblemonPreEvolutionGenerator extends PreEvolutionFormOverrideProvider {
    public CobblemonPreEvolutionGenerator (FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(output, registryLookup);
    }

    @Override
    protected void generate (PreEvolutionGenerator generator) {
        generator.add("overqwil", "qwilfish", "hisui");
    }
}

package com.provismet.cobblemon.daycareplus.mixin;

import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeature;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatureProvider;
import com.cobblemon.mod.common.api.pokemon.feature.SpeciesFeatures;
import com.provismet.cobblemon.daycareplus.config.DaycarePlusOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SpeciesFeatures.class, remap = false)
public abstract class SpeciesFeaturesMixin {
    @Inject(method = "getFeature", at = @At("HEAD"), cancellable = true)
    private void getFertility (String name, CallbackInfoReturnable<SpeciesFeatureProvider<? extends SpeciesFeature>> cir) {
        if (name.equalsIgnoreCase("fertility") && !DaycarePlusOptions.doCompetitiveBreeding()) {
            cir.setReturnValue(null);
        }
    }
}

package com.provismet.cobblemon.daycareplus.handler;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.CollectEggEvent;
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent;
import com.provismet.cobblemon.daycareplus.registries.DPPokemonProperties;
import com.provismet.cobblemon.daycareplus.registries.DPStats;
import kotlin.Unit;

public abstract class CobblemonEventHandler {
    public static void register () {
        CobblemonEvents.HATCH_EGG_POST.subscribe(Priority.NORMAL, CobblemonEventHandler::postHatch);
        CobblemonEvents.COLLECT_EGG.subscribe(Priority.NORMAL, CobblemonEventHandler::postCollect);
    }

    public static void registerEarly () {
        CobblemonEvents.POKEMON_PROPERTY_INITIALISED.subscribe(Priority.NORMAL, CobblemonEventHandler::initialiseProperties);
    }

    private static Unit postHatch (HatchEggEvent.Post event) {
        event.getPlayer().incrementStat(DPStats.EGGS_HATCHED);
        return Unit.INSTANCE;
    }

    private static Unit postCollect (CollectEggEvent event) {
        event.getPlayer().incrementStat(DPStats.EGGS_COLLECTED);
        return Unit.INSTANCE;
    }

    private static void initialiseProperties (Unit unit) {
        CustomPokemonProperty.Companion.register(new BreedableProperty());
    }
}

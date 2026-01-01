package com.provismet.cobblemon.daycareplus.handler;

import com.cobblemon.mod.common.api.Priority;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.CollectEggEvent;
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent;
import com.cobblemon.mod.common.api.mark.Mark;
import com.cobblemon.mod.common.api.mark.Marks;
import com.cobblemon.mod.common.api.properties.CustomPokemonProperty;
import com.cobblemon.mod.common.util.MiscUtilsKt;
import com.provismet.cobblemon.daycareplus.config.DaycarePlusOptions;
import com.provismet.cobblemon.daycareplus.feature.BreedableProperty;
import com.provismet.cobblemon.daycareplus.feature.FertilityFeature;
import com.provismet.cobblemon.daycareplus.registries.DPStats;
import kotlin.Unit;

import java.util.function.Consumer;

public abstract class CobblemonEventHandler {
    public static void register () {
        CobblemonEvents.HATCH_EGG_POST.subscribe(Priority.NORMAL, CobblemonEventHandler::incrementHatchStat);
        CobblemonEvents.COLLECT_EGG.subscribe(Priority.NORMAL, CobblemonEventHandler::postCollect);

        // How wow look at that, we suggest the marks and apply one in DIFFERENT event triggers at different times instead of dumping into a single molang of all things!
        if (DaycarePlusOptions.shouldApplyMarks()) {
            CobblemonEvents.HATCH_EGG_POST.subscribe(Priority.HIGHEST, CobblemonEventHandler::suggestPotentialMarks);
            CobblemonEvents.HATCH_EGG_POST.subscribe(Priority.LOWEST, CobblemonEventHandler::applyPotentialMark);
        }
    }

    public static void registerEarly () {
        CobblemonEvents.POKEMON_PROPERTY_INITIALISED.subscribe(Priority.NORMAL, CobblemonEventHandler::initialiseProperties);
    }

    private static void incrementHatchStat (HatchEggEvent.Post event) {
        event.getPlayer().incrementStat(DPStats.EGGS_HATCHED);
    }

    private static void suggestPotentialMarks(HatchEggEvent.Post event) {
        Consumer<String> marks = path -> {
            Mark mark = Marks.getByIdentifier(MiscUtilsKt.cobblemonResource(path));
            if (mark != null) event.getPokemon().addPotentialMark(mark);
        };

        marks.accept("mark_rare");
        marks.accept("mark_uncommon");
        marks.accept("mark_personality_absent-minded");
        marks.accept("mark_personality_angry");
        marks.accept("mark_personality_calmness");
        marks.accept("mark_personality_charismatic");
        marks.accept("mark_personality_crafty");
        marks.accept("mark_personality_excited");
        marks.accept("mark_personality_ferocious");
        marks.accept("mark_personality_flustered");
        marks.accept("mark_personality_jittery");
        marks.accept("mark_personality_joyful");
        marks.accept("mark_personality_intellectual");
        marks.accept("mark_personality_intense");
        marks.accept("mark_personality_kindly");
        marks.accept("mark_personality_scowling");
        marks.accept("mark_personality_rowdy");
        marks.accept("mark_personality_peeved");
        marks.accept("mark_personality_pumped-up");
        marks.accept("mark_personality_smiley");
        marks.accept("mark_personality_teary");
        marks.accept("mark_personality_upbeat");
        marks.accept("mark_personality_zoned-out");
        marks.accept("mark_personality_humble");
        marks.accept("mark_personality_prideful");
        marks.accept("mark_personality_slump");
        marks.accept("mark_personality_thorny");
        marks.accept("mark_personality_unsure");
        marks.accept("mark_personality_vigor");
        marks.accept("mark_personality_zero_energy");
    }

    private static void applyPotentialMark (HatchEggEvent.Post event) {
        event.getPokemon().applyPotentialMarks();
    }

    private static void postCollect (CollectEggEvent event) {
        event.getPlayer().incrementStat(DPStats.EGGS_COLLECTED);
    }

    private static void initialiseProperties (Unit unit) {
        CustomPokemonProperty.Companion.register(new BreedableProperty());
        FertilityFeature.register();
    }
}

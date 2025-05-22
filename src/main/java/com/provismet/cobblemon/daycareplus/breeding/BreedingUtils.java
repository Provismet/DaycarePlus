package com.provismet.cobblemon.daycareplus.breeding;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.Set;

public abstract class BreedingUtils {
    private static final String DITTO = "Ditto";

    public static boolean canBreed (Pokemon parent1, Pokemon parent2) {
        Set<EggGroup> eggGroups1 = parent1.getSpecies().getEggGroups();
        Set<EggGroup> eggGroups2 = parent2.getSpecies().getEggGroups();

        if (eggGroups1.contains(EggGroup.UNDISCOVERED) || eggGroups2.contains(EggGroup.UNDISCOVERED)) return false;
        if (eggGroups1.contains(EggGroup.DITTO) ^ eggGroups2.contains(EggGroup.DITTO)) return true;
        if (parent1.getGender() == Gender.GENDERLESS || parent2.getGender() == Gender.GENDERLESS) return false;
        if (parent1.getGender() == parent2.getGender()) return false;

        return eggGroups1.stream().anyMatch(eggGroups2::contains);
    }

    public static Pokemon getMotherOrNonDitto (Pokemon parent1, Pokemon parent2) {
        if (parent1.getGender() == Gender.FEMALE || parent2.getSpecies().getName().equalsIgnoreCase(DITTO)) return parent1;
        if (parent2.getGender() == Gender.FEMALE || parent1.getSpecies().getName().equalsIgnoreCase(DITTO)) return parent2;

        // Fallback value, this should not be reached.
        DaycarePlusServer.LOGGER.info("Fallback parent value reached for [{}] and [{}]", parent1.getDisplayName().getString(), parent2.getDisplayName().getString());
        return parent1;
    }

    public static Optional<PotentialPokemonProperties> getOffspring (@Nullable Pokemon parent1, @Nullable Pokemon parent2) {
        if (parent1 == null || parent2 == null || !canBreed(parent1, parent2)) return Optional.empty();

        Pokemon primary = getMotherOrNonDitto(parent1, parent2);
        Pokemon secondary = primary == parent1 ? parent2 : parent1;
        return Optional.of(new PotentialPokemonProperties(primary, secondary));
    }

    // TODO: Check if this actually works.
    public static FormData getBabyForm (Pokemon parent) {
        PreEvolution preevo = parent.getPreEvolution();
        if (preevo == null) return parent.getForm();

        while (preevo.getForm().getPreEvolution() != null) {
            preevo = preevo.getForm().getPreEvolution();
        }
        return preevo.getForm();
    }
}

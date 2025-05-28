package com.provismet.cobblemon.daycareplus.breeding;

import com.cobblemon.mod.common.api.pokemon.egg.EggGroup;
import com.cobblemon.mod.common.api.pokemon.evolution.PreEvolution;
import com.cobblemon.mod.common.pokemon.FormData;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class BreedingUtils implements SimpleSynchronousResourceReloadListener {
    private static final String DITTO = "Ditto";
    private static final Map<Identifier, PreEvoFormOverride> PRE_EVO_OVERRIDES = new HashMap<>();

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

    // TODO: Works for normal species, but breaks for forms.
    public static FormData getBabyForm (Pokemon parent) {
        DaycarePlusServer.LOGGER.info("Getting baby form for parent: {}, {}", parent.getSpecies().getName(), parent.getForm().getName());
        PreEvolution preevo = PreEvolution.Companion.of(parent.getSpecies(), parent.getForm());
        PreEvolution temp;
        while ((temp = getPreEvolution(preevo)) != null) {
            preevo = temp;
            DaycarePlusServer.LOGGER.info("PreEvo iterated to: {}, {}", preevo.getSpecies().getName(), preevo.getForm().getName());
        }
        return preevo.getForm();
    }

    @Nullable
    private static PreEvolution getPreEvolution (PreEvolution pokemon) {
        if (pokemon == null) return null;

        // Try to get the pre-evolution from the overrides.
        Identifier speciesId = pokemon.getSpecies().getResourceIdentifier();
        if (PRE_EVO_OVERRIDES.containsKey(speciesId)) {
            PreEvoFormOverride override = PRE_EVO_OVERRIDES.get(speciesId);
            if (override.hasForm(pokemon.getForm().formOnlyShowdownId())) {
                return override.getPreEvolution(pokemon.getForm());
            }
        }

        PreEvolution preEvolution = pokemon.getForm().getPreEvolution();
        if (preEvolution == null) return null; // There is no pre-evolution, return null.

        // Pre-evolution exists, try to match the forms.
        return PreEvolution.Companion.of(preEvolution.getSpecies(), preEvolution.getSpecies().getFormByShowdownId(pokemon.getForm().formOnlyShowdownId()));
    }

    @Override
    public Identifier getFabricId () {
        return DaycarePlusServer.identifier("reload_listener");
    }

    @Override
    public void reload (ResourceManager manager) {
        PRE_EVO_OVERRIDES.clear();

        Map<Identifier, Resource> overrides = manager.findResources("overrides/preevolutions", identifier -> Objects.equals(identifier.getNamespace(), DaycarePlusServer.MODID) && identifier.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : overrides.entrySet()) {
            try (InputStream stream = entry.getValue().getInputStream()) {
                String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                DataResult<Pair<PreEvoFormOverride, JsonElement>> dataResult = PreEvoFormOverride.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(text));
                PreEvoFormOverride resolved = dataResult.getOrThrow().getFirst();

                PRE_EVO_OVERRIDES.put(resolved.species(), resolved);
                DaycarePlusServer.LOGGER.info("Registered evolution override: {}", resolved);
            }
            catch (Throwable e) {
                DaycarePlusServer.LOGGER.error("DaycarePlus encountered an error whilst parsing override file {}: ", entry.getKey(), e);
            }
        }
    }
}

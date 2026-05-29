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
import com.provismet.cobblemon.daycareplus.DaycarePlusMain;
import com.provismet.cobblemon.daycareplus.api.codec.BreedingRules;
import com.provismet.cobblemon.daycareplus.config.DaycarePlusOptions;
import com.provismet.cobblemon.daycareplus.feature.BreedableProperty;
import com.provismet.cobblemon.daycareplus.feature.FertilityFeature;
import com.provismet.cobblemon.daycareplus.registries.DPDynamicRegistries;
import com.provismet.cobblemon.lilycobble.pokemon.PokemonSupplier;
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
    private static final Map<String, FormPropertiesOverride> FORM_PROPERTY_OVERRIDES = new HashMap<>();

    public static boolean isAllowedToBreed (Pokemon pokemon) {
        return BreedableProperty.get(pokemon)
            && (!DaycarePlusOptions.doCompetitiveBreeding() || DaycarePlusOptions.shouldAllowBreedingWithoutFertility() || FertilityFeature.get(pokemon) > 0);
    }

    public static boolean isCompatible (Pokemon parent1, Pokemon parent2) {
        return isCompatibleGender(parent1, parent2) && isCompatibleEggGroup(parent1, parent2);
    }

    public static boolean isCompatibleEggGroup (Pokemon parent1, Pokemon parent2) {
        Set<EggGroup> eggGroups1 = parent1.getForm().getEggGroups();
        Set<EggGroup> eggGroups2 = parent2.getForm().getEggGroups();

        if (eggGroups1.contains(EggGroup.UNDISCOVERED) || eggGroups2.contains(EggGroup.UNDISCOVERED)) return false;
        if (eggGroups1.contains(EggGroup.DITTO) ^ eggGroups2.contains(EggGroup.DITTO)) return true;
        return eggGroups1.stream().anyMatch(eggGroups2::contains);
    }

    public static boolean isCompatibleGender (Pokemon parent1, Pokemon parent2) {
        if (parent1.getForm().getEggGroups().contains(EggGroup.DITTO) || parent2.getForm().getEggGroups().contains(EggGroup.DITTO)) return true;
        if (parent1.getGender() == Gender.GENDERLESS || parent2.getGender() == Gender.GENDERLESS) return false;
        return parent1.getGender() != parent2.getGender();
    }

    public static Pokemon getMotherOrNonDitto (Pokemon parent1, Pokemon parent2) {
        if (parent1.getGender() == Gender.FEMALE || parent2.getSpecies().getName().equalsIgnoreCase(DITTO)) return parent1;
        if (parent2.getGender() == Gender.FEMALE || parent1.getSpecies().getName().equalsIgnoreCase(DITTO)) return parent2;

        // Fallback value, this should not be reached.
        return parent1;
    }

    public static Optional<PotentialPokemonProperties> getOffspring (@Nullable Pokemon parent1, @Nullable Pokemon parent2) {
        if (parent1 == null || parent2 == null) return Optional.empty();
        if (!isAllowedToBreed(parent1) || !isAllowedToBreed(parent2)) return Optional.empty();

        Pokemon primary = getMotherOrNonDitto(parent1, parent2);
        Pokemon secondary = primary == parent1 ? parent2 : parent1;

        if (applyRules(primary, secondary) != null || isCompatible(primary, secondary)) {
            return Optional.of(new PotentialPokemonProperties(primary, secondary));
        }
        return Optional.empty();
    }

    public static boolean parentsHaveFertility (Pokemon parent1, Pokemon parent2) {
        return FertilityFeature.get(parent1) > 0 && FertilityFeature.get(parent2) > 0;
    }

    public static FormData getBabyForm (Pokemon parent) {
        PreEvolution preevo = PreEvolution.Companion.of(parent.getSpecies(), parent.getForm());
        PreEvolution temp;
        while ((temp = getPreEvolution(preevo)) != null) {
            preevo = temp;
        }
        return preevo.getForm();
    }

    @Nullable
    public static PokemonSupplier applyRules (Pokemon primary, Pokemon secondary) {
        if (DPDynamicRegistries.getBreedingRules() != null) {
            BreedingRules rules = DPDynamicRegistries.getBreedingRules().get(primary.getSpecies().resourceIdentifier);
            if (rules != null) {
                return rules.getOffspring(primary, secondary);
            }
        }
        return null;
    }

    // Necessary because for some reason the "region-bias-{{choice}}" aspects don't parse directly.
    public static String getFormProperties (FormData form) {
        if (FORM_PROPERTY_OVERRIDES.containsKey(form.formOnlyShowdownId())) {
            return FORM_PROPERTY_OVERRIDES.get(form.formOnlyShowdownId()).toString();
        }
        return String.join(" ", form.getAspects());
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
        return DaycarePlusMain.identifier("reload_listener");
    }

    @Override
    public void reload (ResourceManager manager) {
        // Dynamic Registries probably make more sense in practice, but that would require passing Dynamic Registry managers all over this class.

        PRE_EVO_OVERRIDES.clear();
        FORM_PROPERTY_OVERRIDES.clear();

        Map<Identifier, Resource> overrides = manager.findResources("overrides/preevolutions", identifier -> Objects.equals(identifier.getNamespace(), DaycarePlusMain.MODID) && identifier.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : overrides.entrySet()) {
            try (InputStream stream = entry.getValue().getInputStream()) {
                String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                DataResult<Pair<PreEvoFormOverride, JsonElement>> dataResult = PreEvoFormOverride.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(text));
                PreEvoFormOverride resolved = dataResult.getOrThrow().getFirst();

                PRE_EVO_OVERRIDES.put(resolved.species(), resolved);
                DaycarePlusMain.LOGGER.info("Registered evolution override: {}", resolved);
            }
            catch (Throwable e) {
                DaycarePlusMain.LOGGER.error("DaycarePlus encountered an error whilst parsing override file {}: ", entry.getKey(), e);
            }
        }

        Map<Identifier, Resource> forms = manager.findResources("overrides/forms", identifier -> Objects.equals(identifier.getNamespace(), DaycarePlusMain.MODID) && identifier.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : forms.entrySet()) {
            try (InputStream stream = entry.getValue().getInputStream()) {
                String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                DataResult<Pair<FormPropertiesOverride, JsonElement>> dataResult = FormPropertiesOverride.CODEC.decode(JsonOps.INSTANCE, JsonParser.parseString(text));
                FormPropertiesOverride resolved = dataResult.getOrThrow().getFirst();

                String formId = entry.getKey().getPath().replace("overrides/forms", "").replace("/", "").replace(".json", "");
                FORM_PROPERTY_OVERRIDES.put(formId, resolved);
                DaycarePlusMain.LOGGER.info("Registered form property override: {} -> {}", formId, resolved);
            }
            catch (Throwable e) {
                DaycarePlusMain.LOGGER.error("DaycarePlus encountered an error whilst parsing form property file file {}: ", entry.getKey(), e);
            }
        }
    }
}

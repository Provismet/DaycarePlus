package com.provismet.cobblemon.daycareplus.breeding;

import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.properties.IntProperty;
import com.provismet.cobblemon.daycareplus.config.Options;
import kotlin.Unit;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.math.MathHelper;

public class FertilityProperty extends IntProperty {
    public FertilityProperty (int value) {
        super(
            "fertility",
            value,
            FertilityProperty::applyToPokemon,
            FertilityProperty::applyToPokemonEntity,
            FertilityProperty::matchPokemon,
            FertilityProperty::matchPokemonEntity
        );
    }

    public static int get (Pokemon pokemon) {
        if (!Options.doCompetitiveBreeding()) return Options.getMaxFertility();
        if (pokemon == null) return 0;

        if (pokemon.getPersistentData().contains("fertility", NbtElement.INT_TYPE)) {
            return pokemon.getPersistentData().getInt("fertility");
        }
        return Options.getMaxFertility();
    }

    public static void decrement (Pokemon pokemon) {
        new FertilityProperty(get(pokemon) - 1).apply(pokemon);
    }

    private static Unit applyToPokemon (Pokemon pokemon, int value) {
        value = MathHelper.clamp(value, 0, Options.getMaxFertility());
        pokemon.getPersistentData().putInt("fertility", value);
        return Unit.INSTANCE;
    }

    private static Unit applyToPokemonEntity (PokemonEntity pokemon, int value) {
        return applyToPokemon(pokemon.getPokemon(), value);
    }

    private static boolean matchPokemon (Pokemon pokemon, int value) {
        if (pokemon.getPersistentData().contains("fertility", NbtElement.INT_TYPE)) {
            return pokemon.getPersistentData().getInt("fertility") == value;
        }
        return false;
    }

    private static boolean matchPokemonEntity (PokemonEntity pokemon, int value) {
        return matchPokemon(pokemon.getPokemon(), value);
    }
}

package com.provismet.cobblemon.daycareplus.api;

import com.provismet.cobblemon.daycareplus.registries.DPItems;
import net.minecraft.item.ItemStack;

import java.util.Optional;

@SuppressWarnings("unused")
public class EggHelper {
    /**
     * @param stack A potential egg item.
     * @return Whether or not this item represents a Pokémon Egg.
     */
    public static boolean isEgg (ItemStack stack) {
        return tryGetEgg(stack).isPresent();
    }

    /**
     * A conversion method that can be used to convert eggs from other mods into Daycare+ eggs.
     * <p>
     * Use mixins to inject into this method to add compatibility.
     *
     * @param stack A stack that may or may not contain a pokemon egg.
     * @return A possible pokemon egg. If present, the stack will always be daycareplus:pokemon_egg.
     */
    public static Optional<ItemStack> tryGetEgg (ItemStack stack) {
        if (stack.isOf(DPItems.POKEMON_EGG)) return Optional.of(stack);
        return Optional.empty();
    }
}

package com.provismet.cobblemon.daycareplus.item;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.cobblemon.mod.common.util.ResourceLocationExtensionsKt;
import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.config.Options;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.List;

public class PokemonEggItem extends PolymerItem {
    public PokemonEggItem (Settings settings, Item baseVanillaItem, PolymerModelData modelData) {
        super(settings, baseVanillaItem, modelData);
    }

    public ItemStack createEggItem (PokemonProperties properties) {
        ItemStack stack = DPItems.POKEMON_EGG.getDefaultStack();
        stack.set(DPItemDataComponents.POKEMON_PROPERTIES, properties.asString(" "));

        if (properties.getSpecies() != null) {
            Identifier speciesId = ResourceLocationExtensionsKt.asIdentifierDefaultingNamespace(properties.getSpecies(), Cobblemon.MODID);
            Species species = PokemonSpecies.INSTANCE.getByIdentifier(speciesId);
            if (species != null) {
                stack.set(DPItemDataComponents.EGG_STEPS, Options.getEggPoints(species.getEggCycles()));
            }
            else {
                stack.set(DPItemDataComponents.EGG_STEPS, 7200);
            }
        }

        return stack;
    }

    @Override
    public void appendTooltip (ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);

        String properties = stack.get(DPItemDataComponents.POKEMON_PROPERTIES);
        if (properties == null) {
            tooltip.add(Text.literal("No data found."));
        }
        else {
            PokemonProperties pokemonProperties = PokemonProperties.Companion.parse(properties);
            if (pokemonProperties.getSpecies() != null) tooltip.add(Text.literal("Species: ").append(pokemonProperties.getSpecies()));
            if (pokemonProperties.getForm() != null) tooltip.add(Text.literal("Form: ").append(pokemonProperties.getForm()));
            if (pokemonProperties.getNature() != null) tooltip.add(Text.literal("Nature: ").append(pokemonProperties.getNature()));

            Integer steps = stack.get(DPItemDataComponents.EGG_STEPS);
            if (steps != null) {
                tooltip.add(Text.literal("Steps: " + steps));
            }

            IVs iv = pokemonProperties.getIvs();
            if (iv != null) {
                tooltip.add(Text.empty());
                tooltip.add(Text.literal("HP  : " + iv.getOrDefault(Stats.HP)));
                tooltip.add(Text.literal("Atk : " + iv.getOrDefault(Stats.ATTACK)));
                tooltip.add(Text.literal("Def : " + iv.getOrDefault(Stats.DEFENCE)));
                tooltip.add(Text.literal("Sp.A: " + iv.getOrDefault(Stats.SPECIAL_ATTACK)));
                tooltip.add(Text.literal("Sp.D: " + iv.getOrDefault(Stats.SPECIAL_DEFENCE)));
                tooltip.add(Text.literal("Spe : " + iv.getOrDefault(Stats.SPEED)));
            }
        }
    }

    // TODO: Temporary until incubators implemented.
    @Override
    public void inventoryTick (ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (entity instanceof ServerPlayerEntity player) {
            this.decrementEggSteps(stack, 1, player);
        }
    }

    public void decrementEggSteps (ItemStack stack, int amount, ServerPlayerEntity player) {
        Integer steps = stack.get(DPItemDataComponents.EGG_STEPS);
        if (steps == null) steps = 7200;

        steps = Math.max(0, steps - amount);

        if (steps <= 0) {
            DaycarePlusServer.LOGGER.info("Attempt to hatch egg.");
            boolean playerPartyBusy = PlayerExtensionsKt.isPartyBusy(player) || PlayerExtensionsKt.isInBattle(player);
            boolean partyHasSpace = PlayerExtensionsKt.party(player).getFirstAvailablePosition() != null || PlayerExtensionsKt.pc(player).getFirstAvailablePosition() != null;

            // Don't waste eggs, only hatch if there is space!
            if (!playerPartyBusy && partyHasSpace) this.hatch(stack, player);
        }
        else stack.set(DPItemDataComponents.EGG_STEPS, steps);
    }

    public void hatch (ItemStack stack, ServerPlayerEntity player) {
        DaycarePlusServer.LOGGER.info("Attempting to hatch egg.");
        String propertiesString = stack.get(DPItemDataComponents.POKEMON_PROPERTIES);

        if (propertiesString != null) {
            DaycarePlusServer.LOGGER.info("Pokemon data found.");
            PokemonProperties properties = PokemonProperties.Companion.parse(propertiesString);

            CobblemonEvents.HATCH_EGG_PRE.emit(new HatchEggEvent.Pre(properties, player));
            Pokemon pokemon = properties.create(player);
            player.sendMessage(Text.literal("Your egg hatched."), true);
            PlayerExtensionsKt.party(player).add(pokemon);
            CobblemonEvents.HATCH_EGG_POST.emit(new HatchEggEvent.Post(properties, player));
        }
        stack.decrement(1);
    }
}

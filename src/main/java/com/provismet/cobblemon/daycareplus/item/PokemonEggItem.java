package com.provismet.cobblemon.daycareplus.item;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.cobblemon.mod.common.util.ResourceLocationExtensionsKt;
import com.provismet.cobblemon.daycareplus.config.Options;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import com.provismet.cobblemon.daycareplus.util.StringFormatting;
import com.provismet.cobblemon.daycareplus.util.Styles;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class PokemonEggItem extends PolymerItem {
    private static final int TICKS_PER_MINUTE = 60 * 20;
    private static final int DEFAULT_STEPS = 7200;

    private final PolymerModelData shiny;

    public PokemonEggItem (Settings settings, Item baseVanillaItem, PolymerModelData modelData, PolymerModelData shinyModel) {
        super(settings, baseVanillaItem, modelData);
        this.shiny = shinyModel;
    }

    public ItemStack createEggItem (PokemonProperties properties) {
        ItemStack stack = DPItems.POKEMON_EGG.getDefaultStack();
        stack.set(DPItemDataComponents.POKEMON_PROPERTIES, properties.asString(" "));

        if (properties.getSpecies() != null) {
            Identifier speciesId = ResourceLocationExtensionsKt.asIdentifierDefaultingNamespace(properties.getSpecies(), Cobblemon.MODID);
            Species species = PokemonSpecies.INSTANCE.getByIdentifier(speciesId);
            if (species != null) {
                stack.set(DPItemDataComponents.EGG_STEPS, Options.getEggPoints(species.getEggCycles()));
                stack.set(DPItemDataComponents.MAX_EGG_STEPS, Options.getEggPoints(species.getEggCycles()));
            }
            else {
                stack.set(DPItemDataComponents.EGG_STEPS, DEFAULT_STEPS);
                stack.set(DPItemDataComponents.MAX_EGG_STEPS, DEFAULT_STEPS);
            }
        }

        return stack;
    }

    @Override
    public void appendTooltip (ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        if (stack.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)) return;

        String properties = stack.get(DPItemDataComponents.POKEMON_PROPERTIES);
        if (properties == null) {
            tooltip.add(Text.translatable("tooltip.daycareplus.egg.no_data"));
        }
        else {
            PokemonProperties pokemonProperties = PokemonProperties.Companion.parse(properties);
            if (pokemonProperties.getSpecies() != null) {
                MutableText species = Text.translatable("property.daycareplus.species").formatted(Formatting.YELLOW)
                    .append(Text.literal(StringFormatting.titleCase(pokemonProperties.getSpecies())).styled(Styles.WHITE_NO_ITALICS));

                if (Objects.requireNonNullElse(pokemonProperties.getShiny(), false)) species = species.append(Text.literal(" ★").formatted(Formatting.GOLD));
                tooltip.add(species);
            }
            if (pokemonProperties.getForm() != null) tooltip.add(Text.translatable("property.daycareplus.form").formatted(Formatting.YELLOW).append(Text.literal(StringFormatting.titleCase(pokemonProperties.getForm())).styled(Styles.WHITE_NO_ITALICS)));
            if (pokemonProperties.getNature() != null) tooltip.add(Text.translatable("property.daycareplus.nature").formatted(Formatting.YELLOW).append(Text.literal(StringFormatting.titleCase(Identifier.of(pokemonProperties.getNature()).getPath())).styled(Styles.WHITE_NO_ITALICS)));
            if (pokemonProperties.getGender() != null && pokemonProperties.getGender() != Gender.GENDERLESS) {
                Text gender = switch (pokemonProperties.getGender()) {
                    case MALE -> Text.literal("M").formatted(Formatting.BLUE);
                    case FEMALE -> Text.literal("F").formatted(Formatting.RED);
                    default -> Text.literal("");
                };

                tooltip.add(Text.translatable("property.daycareplus.gender").formatted(Formatting.YELLOW).append(gender));
            }

            Integer steps = stack.get(DPItemDataComponents.EGG_STEPS);
            if (steps != null) {
                String minutes = "" + (steps / TICKS_PER_MINUTE);
                String seconds = "" + ((steps % TICKS_PER_MINUTE) / 20);

                if (minutes.length() < 2) minutes = "0" + minutes;
                if (seconds.length() < 2) seconds = "0" + seconds;

                tooltip.add(Text.translatable("tooltip.daycareplus.egg.ticks", minutes + ":" + seconds));
            }

            IVs iv = pokemonProperties.getIvs();
            if (iv != null) {
                tooltip.add(Text.empty());
                tooltip.add(Text.translatable("property.daycareplus.hp").styled(Styles.colouredNoItalics(Styles.HP))
                    .append(Text.literal(String.valueOf(iv.getOrDefault(Stats.HP))).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.attack").styled(Styles.colouredNoItalics(Styles.ATTACK))
                    .append(Text.literal(String.valueOf(iv.getOrDefault(Stats.ATTACK))).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.defence").styled(Styles.colouredNoItalics(Styles.DEFENCE))
                    .append(Text.literal(String.valueOf(iv.getOrDefault(Stats.DEFENCE))).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.special_attack").styled(Styles.colouredNoItalics(Styles.SPECIAL_ATTACK))
                    .append(Text.literal(String.valueOf(iv.getOrDefault(Stats.SPECIAL_ATTACK))).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.special_defence").styled(Styles.colouredNoItalics(Styles.SPECIAL_DEFENCE))
                    .append(Text.literal(String.valueOf(iv.getOrDefault(Stats.SPECIAL_DEFENCE))).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.speed").styled(Styles.colouredNoItalics(Styles.SPEED))
                    .append(Text.literal(String.valueOf(iv.getOrDefault(Stats.SPEED))).styled(Styles.WHITE_NO_ITALICS)));
            }
        }
    }

    public int getRemainingSteps (ItemStack stack) {
        return stack.getOrDefault(DPItemDataComponents.EGG_STEPS, DEFAULT_STEPS);
    }

    public int getMaxSteps (ItemStack stack) {
        return stack.getOrDefault(DPItemDataComponents.MAX_EGG_STEPS, DEFAULT_STEPS);
    }

    public void decrementEggSteps (ItemStack stack, int amount, ServerPlayerEntity player) {
        int steps = this.getRemainingSteps(stack);
        steps = Math.max(0, steps - amount);
        stack.setDamage(MathHelper.lerp(1f - ((float)steps / this.getMaxSteps(stack)), 1, 100));

        if (steps == 0) {
            boolean playerPartyBusy = PlayerExtensionsKt.isPartyBusy(player) || PlayerExtensionsKt.isInBattle(player);
            boolean partyHasSpace = PlayerExtensionsKt.party(player).getFirstAvailablePosition() != null || PlayerExtensionsKt.pc(player).getFirstAvailablePosition() != null;

            // Don't waste eggs, only hatch if there is space!
            if (!playerPartyBusy && partyHasSpace) this.hatch(stack, player);
        }
        else stack.set(DPItemDataComponents.EGG_STEPS, steps);
    }

    public void hatch (ItemStack stack, ServerPlayerEntity player) {
        String propertiesString = stack.get(DPItemDataComponents.POKEMON_PROPERTIES);

        if (propertiesString != null) {
            PokemonProperties properties = PokemonProperties.Companion.parse(propertiesString);

            CobblemonEvents.HATCH_EGG_PRE.emit(new HatchEggEvent.Pre(properties, player));
            Pokemon pokemon = properties.create(player);
            player.sendMessage(Text.translatable("message.overlay.daycareplus.egg.hatch"), true);
            PlayerExtensionsKt.party(player).add(pokemon);
            CobblemonEvents.HATCH_EGG_POST.emit(new HatchEggEvent.Post(properties, player));
        }
        stack.decrement(1);
    }

    @Override
    public int getPolymerCustomModelData (ItemStack stack, @Nullable ServerPlayerEntity player) {
        if (stack.get(DataComponentTypes.CUSTOM_MODEL_DATA) != null) return -1;
        if (stack.getOrDefault(DPItemDataComponents.POKEMON_PROPERTIES, "").contains("shiny=true")) return this.shiny.value();
        return super.getPolymerCustomModelData(stack, player);
    }
}

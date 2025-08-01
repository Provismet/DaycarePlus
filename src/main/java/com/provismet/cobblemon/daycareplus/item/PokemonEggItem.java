package com.provismet.cobblemon.daycareplus.item;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.HatchEggEvent;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.PokemonSpecies;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.pokemon.Gender;
import com.cobblemon.mod.common.pokemon.IVs;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.pokemon.Species;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.cobblemon.mod.common.util.ResourceLocationExtensionsKt;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.provismet.cobblemon.daycareplus.config.DaycarePlusOptions;
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
import java.util.Optional;

public class PokemonEggItem extends PolymerItem {
    public static final Codec<ItemStack> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.optionalFieldOf("pokemon").forGetter(stack -> Optional.ofNullable(stack.get(DPItemDataComponents.POKEMON_PROPERTIES))),
        Codec.INT.optionalFieldOf("steps").forGetter(stack -> Optional.ofNullable(stack.get(DPItemDataComponents.EGG_STEPS))),
        Codec.INT.optionalFieldOf("max_steps").forGetter(stack -> Optional.ofNullable(stack.get(DPItemDataComponents.MAX_EGG_STEPS)))
    ).apply(instance, (pokemonProperties, steps, maxSteps) -> {
        if (pokemonProperties.isEmpty()) return ItemStack.EMPTY;
        if (steps.isEmpty() || maxSteps.isEmpty()) return DPItems.POKEMON_EGG.createEggItem(PokemonProperties.Companion.parse(pokemonProperties.get()));

        ItemStack stack = DPItems.POKEMON_EGG.getDefaultStack();
        stack.set(DPItemDataComponents.POKEMON_PROPERTIES, pokemonProperties.get());
        stack.set(DPItemDataComponents.EGG_STEPS, steps.get());
        stack.set(DPItemDataComponents.MAX_EGG_STEPS, maxSteps.get());
        return stack;
    })));

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
                stack.set(DPItemDataComponents.EGG_STEPS, DaycarePlusOptions.getEggPoints(species.getEggCycles()));
                stack.set(DPItemDataComponents.MAX_EGG_STEPS, DaycarePlusOptions.getEggPoints(species.getEggCycles()));
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

        Integer steps = stack.get(DPItemDataComponents.EGG_STEPS);
        if (steps != null) {
            String minutes = "" + (steps / TICKS_PER_MINUTE);
            String seconds = "" + ((steps % TICKS_PER_MINUTE) / 20);

            if (minutes.length() < 2) minutes = "0" + minutes;
            if (seconds.length() < 2) seconds = "0" + seconds;

            tooltip.add(Text.translatable("tooltip.daycareplus.egg.ticks", minutes + ":" + seconds));
        }
        if (!DaycarePlusOptions.shouldShowEggTooltip()) return;

        tooltip.add(Text.empty());
        String properties = stack.get(DPItemDataComponents.POKEMON_PROPERTIES);
        if (properties == null) {
            tooltip.add(Text.translatable("tooltip.daycareplus.egg.no_data"));
        }
        else {
            PokemonProperties pokemonProperties = PokemonProperties.Companion.parse(properties);
            if (pokemonProperties.getSpecies() != null) {
                MutableText species = Text.translatable("property.daycareplus.species").formatted(Formatting.YELLOW)
                    .append(this.getTooltipSpeciesName(pokemonProperties));
                tooltip.add(species);
            }
            if (pokemonProperties.getForm() != null) tooltip.add(Text.translatable("property.daycareplus.form").formatted(Formatting.YELLOW).append(this.getTooltipFormName(pokemonProperties)));
            if (pokemonProperties.getNature() != null) tooltip.add(Text.translatable("property.daycareplus.nature").formatted(Formatting.YELLOW).append(this.getTooltipNatureName(pokemonProperties)));
            if (pokemonProperties.getAbility() != null) tooltip.add(Text.translatable("property.daycareplus.ability").formatted(Formatting.YELLOW).append(this.getTooltipAbilityName(pokemonProperties)));
            if (pokemonProperties.getGender() != null && pokemonProperties.getGender() != Gender.GENDERLESS) {
                Text gender = switch (pokemonProperties.getGender()) {
                    case MALE -> Text.literal("M").formatted(Formatting.BLUE);
                    case FEMALE -> Text.literal("F").formatted(Formatting.RED);
                    default -> Text.literal("");
                };

                tooltip.add(Text.translatable("property.daycareplus.gender").formatted(Formatting.YELLOW).append(gender));
            }

            IVs iv = pokemonProperties.getIvs();
            if (iv != null) {
                tooltip.add(Text.empty());
                tooltip.add(Text.translatable("property.daycareplus.hp").styled(Styles.colouredNoItalics(Styles.HP))
                    .append(Text.literal(this.formatIV(iv, Stats.HP)).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.attack").styled(Styles.colouredNoItalics(Styles.ATTACK))
                    .append(Text.literal(this.formatIV(iv, Stats.ATTACK)).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.defence").styled(Styles.colouredNoItalics(Styles.DEFENCE))
                    .append(Text.literal(this.formatIV(iv, Stats.DEFENCE)).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.special_attack").styled(Styles.colouredNoItalics(Styles.SPECIAL_ATTACK))
                    .append(Text.literal(this.formatIV(iv, Stats.SPECIAL_ATTACK)).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.special_defence").styled(Styles.colouredNoItalics(Styles.SPECIAL_DEFENCE))
                    .append(Text.literal(this.formatIV(iv, Stats.SPECIAL_DEFENCE)).styled(Styles.WHITE_NO_ITALICS)));
                tooltip.add(Text.translatable("property.daycareplus.speed").styled(Styles.colouredNoItalics(Styles.SPEED))
                    .append(Text.literal(this.formatIV(iv, Stats.SPEED)).styled(Styles.WHITE_NO_ITALICS)));
            }
        }
    }

    // Exists for mixin convenience.
    private MutableText getTooltipSpeciesName (PokemonProperties properties) {
        MutableText text = Text.literal(StringFormatting.titleCase(properties.getSpecies())).styled(Styles.WHITE_NO_ITALICS);
        if (Objects.requireNonNullElse(properties.getShiny(), false)) text.append(Text.literal(" ★").formatted(Formatting.GOLD));
        return text;
    }

    // Exists for mixin convenience.
    private MutableText getTooltipFormName (PokemonProperties properties) {
        return Text.literal(StringFormatting.titleCase(properties.getForm())).styled(Styles.WHITE_NO_ITALICS);
    }

    // Exists for mixin convenience.
    private MutableText getTooltipNatureName (PokemonProperties properties) {
        assert properties.getNature() != null;
        return Text.literal(StringFormatting.titleCase(Identifier.of(properties.getNature()).getPath())).styled(Styles.WHITE_NO_ITALICS);
    }

    // Exists for mixin convenience.
    private MutableText getTooltipAbilityName (PokemonProperties properties) {
        return Text.literal(StringFormatting.titleCase(properties.getAbility())).styled(Styles.WHITE_NO_ITALICS);
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

    private String formatIV (IVs ivs, Stat stat) {
        Integer iv = ivs.get(stat);
        if (iv == null) return "?";
        return String.valueOf(iv);
    }
}

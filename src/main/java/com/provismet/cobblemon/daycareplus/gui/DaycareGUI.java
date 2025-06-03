package com.provismet.cobblemon.daycareplus.gui;

import ca.landonjw.gooeylibs2.api.button.ButtonBase;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.PageBase;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.abilities.AbilityTemplate;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.api.pokemon.stats.Stat;
import com.cobblemon.mod.common.api.pokemon.stats.Stats;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.item.PokemonItem;
import com.cobblemon.mod.common.pokemon.Nature;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.provismet.cobblemon.daycareplus.breeding.BreedingUtils;
import com.provismet.cobblemon.daycareplus.breeding.PotentialPokemonProperties;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.registries.DPIconItems;
import com.provismet.cobblemon.daycareplus.util.StringFormatting;
import com.provismet.cobblemon.daycareplus.util.Styles;
import com.provismet.cobblemon.daycareplus.util.tag.DPItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;
import net.minecraft.util.hit.BlockHitResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DaycareGUI {
    static PageBase create (PokemonPastureBlockEntity pastureBlockEntity, IMixinPastureBlockEntity mixinPasture, ServerPlayerEntity player, BlockState state, BlockHitResult hit) {
        GooeyButton filler = GooeyButton.builder()
            .display(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack())
            .with(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE)
            .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();

        GooeyButton fillerHeldItem = GooeyButton.builder()
            .display(Items.WHITE_STAINED_GLASS_PANE.getDefaultStack())
            .with(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE)
            .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();

        GooeyButton infoButton = GooeyButton.builder()
            .display(DPIconItems.INFO.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.info").styled(Styles.WHITE_NO_ITALICS))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.translatable("gui.button.daycareplus.info.tooltip.1").styled(Styles.GRAY_NO_ITALICS),
                Text.translatable("gui.button.daycareplus.info.tooltip.2").styled(Styles.GRAY_NO_ITALICS)
            )))
            .build();

        GooeyButton openPasture = GooeyButton.builder()
            .display(CobblemonItems.PASTURE.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.open_pasture").styled(Styles.WHITE_NO_ITALICS))
            .onClick(action -> {
                mixinPasture.setShouldSkipDaycareGUI(true);
                state.onUse(player.getWorld(), player, hit);
            })
            .build();

        ButtonBase eggCounter = mixinPasture.getEggCounterButton();

        Pokemon parent1 = null;
        Pokemon parent2 = null;
        if (!pastureBlockEntity.getTetheredPokemon().isEmpty()) parent1 = pastureBlockEntity.getTetheredPokemon().getFirst().getPokemon();
        if (pastureBlockEntity.getTetheredPokemon().size() == 2) parent2 = pastureBlockEntity.getTetheredPokemon().getLast().getPokemon();

        GooeyButton missingParent = GooeyButton.builder()
            .display(Items.BARRIER.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.no_parent").styled(Styles.WHITE_NO_ITALICS))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("gui.button.daycareplus.no_parent.tooltip").styled(Styles.GRAY_NO_ITALICS))))
            .build();

        ButtonBase parent1Info;
        GooeyButton parent1Item = fillerHeldItem;
        if (parent1 != null) {
            parent1Info = createButtonForPokemon(parent1);

            if (parent1.heldItem().isIn(DPItemTags.BREEDING_ITEM)) {
                parent1Item = GooeyButton.builder()
                    .display(parent1.heldItem())
                    .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                    .with(DataComponentTypes.LORE, new LoreComponent(List.of(
                        Text.translatable(parent1.heldItem().getTranslationKey() + ".breeding").styled(Styles.GRAY_NO_ITALICS)
                    )))
                    .build();
            }
        }
        else {
            parent1Info = missingParent;
        }

        ButtonBase parent2Info;
        GooeyButton parent2Item = fillerHeldItem;
        if (parent2 != null) {
            parent2Info = createButtonForPokemon(parent2);

            if (parent2.heldItem().isIn(DPItemTags.BREEDING_ITEM)) {
                parent2Item = GooeyButton.builder()
                    .display(parent2.heldItem())
                    .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
                    .with(DataComponentTypes.LORE, new LoreComponent(List.of(
                        Text.translatable(parent2.heldItem().getTranslationKey() + ".breeding").styled(Styles.GRAY_NO_ITALICS)
                    )))
                    .build();
            }
        }
        else {
            parent2Info = missingParent;
        }

        GooeyButton offspringInfo;
        Optional<PotentialPokemonProperties> offspring = BreedingUtils.getOffspring(parent1, parent2);
        if (offspring.isPresent()) {
            Map<Stat, PotentialPokemonProperties.PotentialIV> ivs = offspring.get().getPossibleIVs();
            PokemonProperties props = offspring.get().createPokemonProperties();
            props.setShiny(false);
            Pokemon tile = props.create();

            offspringInfo = GooeyButton.builder()
                .display(PokemonItem.from(props))
                .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.offspring").styled(Styles.WHITE_NO_ITALICS))
                .with(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.translatable("property.daycareplus.species").styled(Styles.formattedNoItalics(Formatting.YELLOW)).append(tile.getSpecies().getTranslatedName().styled(Styles.WHITE_NO_ITALICS)),
                    Text.translatable("property.daycareplus.form").styled(Styles.formattedNoItalics(Formatting.YELLOW)).append(Text.literal(tile.getForm().getName()).styled(Styles.WHITE_NO_ITALICS)),
                    Text.translatable("property.daycareplus.ability").styled(Styles.formattedNoItalics(Formatting.YELLOW)).append(Text.literal(String.join(", ", offspring.get().getPossibleAbilities().stream().map(AbilityTemplate::getName).map(StringFormatting::titleCase).toList())).styled(Styles.WHITE_NO_ITALICS)),
                    Text.translatable("property.daycareplus.nature").styled(Styles.formattedNoItalics(Formatting.YELLOW)).append((offspring.get().getPossibleNatures().isEmpty() ? Text.literal("?") : offspring.get().getPossibleNatures().stream().map(Nature::getDisplayName).map(Text::translatable).reduce(Text.literal(""), (nature1, nature2) -> nature1.getString().isEmpty() ? nature2 : nature1.append(", ").append(nature2))).styled(Styles.WHITE_NO_ITALICS)),
                    Text.empty(),
                    Text.translatable("property.daycareplus.ivs").styled(Styles.WHITE_NO_ITALICS),
                    Text.translatable("property.daycareplus.hp").styled(Styles.colouredNoItalics(Styles.HP)).append(Text.literal(ivs.get(Stats.HP).toString()).styled(Styles.WHITE_NO_ITALICS)),
                    Text.translatable("property.daycareplus.attack").styled(Styles.colouredNoItalics(Styles.ATTACK)).append(Text.literal(ivs.get(Stats.ATTACK).toString()).styled(Styles.WHITE_NO_ITALICS)),
                    Text.translatable("property.daycareplus.defence").styled(Styles.colouredNoItalics(Styles.DEFENCE)).append(Text.literal(ivs.get(Stats.DEFENCE).toString()).styled(Styles.WHITE_NO_ITALICS)),
                    Text.translatable("property.daycareplus.special_attack").styled(Styles.colouredNoItalics(Styles.SPECIAL_ATTACK)).append(Text.literal(ivs.get(Stats.SPECIAL_ATTACK).toString()).styled(Styles.WHITE_NO_ITALICS)),
                    Text.translatable("property.daycareplus.special_defence").styled(Styles.colouredNoItalics(Styles.SPECIAL_DEFENCE)).append(Text.literal(ivs.get(Stats.SPECIAL_DEFENCE).toString()).styled(Styles.WHITE_NO_ITALICS)),
                    Text.translatable("property.daycareplus.speed").styled(Styles.colouredNoItalics(Styles.SPEED)).append(Text.literal(ivs.get(Stats.SPEED).toString()).styled(Styles.WHITE_NO_ITALICS)),
                    Text.empty(),
                    Text.translatable("property.daycareplus.shiny").styled(Styles.formattedNoItalics(Formatting.GOLD)).append(Text.literal("1/" + (int)(1 / offspring.get().getShinyRate())).styled(Styles.WHITE_NO_ITALICS)),
                    Text.literal("(Debug) Aspects: " + String.join(", ", tile.getAspects())) // TODO: Temporary for bug testing.
                )))
                .build();
        }
        else {
            offspringInfo = GooeyButton.builder()
                .display(Items.BARRIER.getDefaultStack())
                .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.offspring.empty").styled(style -> style.withItalic(false).withColor(Formatting.WHITE)))
                .with(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("gui.button.daycareplus.offspring.empty.tooltip").styled(style -> style.withItalic(false).withColor(Formatting.GRAY)))))
                .build();
        }

        ChestTemplate template = ChestTemplate.builder(5)
            .fill(filler)
            .set(0, 8, infoButton)
            .set(0, 7, openPasture)
            .set(0, 6, eggCounter)
            .set(2, 2, parent1Info)
            .set(3, 2, parent1Item)
            .set(2, 4, offspringInfo)
            .set(2, 6, parent2Info)
            .set(3, 6, parent2Item)
            .build();

        return GooeyPage.builder()
            .title("Daycare")
            .template(template)
            .build();
    }

    static ButtonBase createEggButton (IMixinPastureBlockEntity mixinPasture) {
        return GooeyButton.builder()
            .display(Items.EGG.getDefaultStack())
            .with(
                DataComponentTypes.CUSTOM_NAME,
                Text.translatable("gui.button.daycareplus.eggs_held", mixinPasture.count(), mixinPasture.size())
                    .styled(Styles.WHITE_NO_ITALICS))
            .build();
    }

    static ButtonBase createButtonForPokemon (Pokemon pokemon) {
        return GooeyButton.builder()
            .display(PokemonItem.from(pokemon))
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.parent").styled(Styles.WHITE_NO_ITALICS))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.translatable("property.daycareplus.species").styled(Styles.formattedNoItalics(Formatting.YELLOW)).append(pokemon.getSpecies().getTranslatedName().styled(Styles.WHITE_NO_ITALICS)),
                Text.translatable("property.daycareplus.form").styled(Styles.formattedNoItalics(Formatting.YELLOW)).append(Text.literal(pokemon.getForm().getName()).styled(Styles.WHITE_NO_ITALICS)),
                Text.translatable("property.daycareplus.ability").styled(Styles.formattedNoItalics(Formatting.YELLOW)).append(Text.translatable(pokemon.getAbility().getDisplayName()).styled(Styles.WHITE_NO_ITALICS)),
                Text.translatable("property.daycareplus.nature").styled(Styles.formattedNoItalics(Formatting.YELLOW)).append(Text.translatable(pokemon.getNature().getDisplayName()).styled(Styles.WHITE_NO_ITALICS)),
                Text.empty(),
                Text.translatable("property.daycareplus.ivs").styled(Styles.WHITE_NO_ITALICS),
                Text.translatable("property.daycareplus.hp").styled(Styles.colouredNoItalics(Styles.HP)).append(Text.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.HP))).styled(Styles.WHITE_NO_ITALICS)),
                Text.translatable("property.daycareplus.attack").styled(Styles.colouredNoItalics(Styles.ATTACK)).append(Text.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.ATTACK))).styled(Styles.WHITE_NO_ITALICS)),
                Text.translatable("property.daycareplus.defence").styled(Styles.colouredNoItalics(Styles.DEFENCE)).append(Text.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.DEFENCE))).styled(Styles.WHITE_NO_ITALICS)),
                Text.translatable("property.daycareplus.special_attack").styled(Styles.colouredNoItalics(Styles.SPECIAL_ATTACK)).append(Text.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.SPECIAL_ATTACK))).styled(Styles.WHITE_NO_ITALICS)),
                Text.translatable("property.daycareplus.special_defence").styled(Styles.colouredNoItalics(Styles.SPECIAL_DEFENCE)).append(Text.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.SPECIAL_DEFENCE))).styled(Styles.WHITE_NO_ITALICS)),
                Text.translatable("property.daycareplus.speed").styled(Styles.colouredNoItalics(Styles.SPEED)).append(Text.literal(String.valueOf(pokemon.getIvs().getOrDefault(Stats.SPEED))).styled(Styles.WHITE_NO_ITALICS))
            )))
            .build();
    }
}

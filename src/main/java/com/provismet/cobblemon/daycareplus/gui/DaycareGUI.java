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
import com.provismet.cobblemon.daycareplus.util.tag.DPItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DaycareGUI {
    static PageBase create (PokemonPastureBlockEntity pastureBlockEntity, IMixinPastureBlockEntity mixinPasture, ServerPlayerEntity player, BlockState state, BlockHitResult hit) {
        GooeyButton infoButton = GooeyButton.builder()
            .display(Items.FEATHER.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.literal("Info"))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.literal("The daycare attempt to produce an egg periodically."),
                Text.literal("Eggs will still be produced when the pasture is unloaded or the owner is offline.")
            )))
            .build();

        GooeyButton openPasture = GooeyButton.builder()
            .display(CobblemonItems.PASTURE.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.literal("Open Pasture"))
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
            .with(DataComponentTypes.CUSTOM_NAME, Text.literal("No parent selected."))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal("Add a Pokemon to the pasture."))))
            .build();

        GooeyButton parent1Info;
        GooeyButton parent1Item = null;
        if (parent1 != null) {
            parent1Info = GooeyButton.builder()
                .display(PokemonItem.from(parent1))
                .build();

            if (parent1.heldItem().isIn(DPItemTags.BREEDING_ITEM)) {
                parent1Item = GooeyButton.builder()
                    .display(parent1.heldItem())
                    .build();
            }
        }
        else {
            parent1Info = missingParent;
        }

        GooeyButton parent2Info;
        GooeyButton parent2Item = null;
        if (parent2 != null) {
            parent2Info = GooeyButton.builder()
                .display(PokemonItem.from(parent2))
                .build();

            if (parent2.heldItem().isIn(DPItemTags.BREEDING_ITEM)) {
                parent2Item = GooeyButton.builder()
                    .display(parent2.heldItem())
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
            Pokemon tile = props.create(); // TODO: Test if it's still necessary to create the full pokemon.

            offspringInfo = GooeyButton.builder()
                .display(PokemonItem.from(props))
                .with(DataComponentTypes.CUSTOM_NAME, Text.literal("Offspring"))
                .with(DataComponentTypes.LORE, new LoreComponent(List.of(
                    Text.literal("Species: " + props.getSpecies()),
                    Text.literal("Form: " + props.getForm()),
                    Text.literal("Abilities: " + String.join(", ", offspring.get().getPossibleAbilities().stream().map(AbilityTemplate::getName).toList())),
                    Text.literal("Nature: " + (offspring.get().getPossibleNatures().isEmpty() ? "?" : String.join(", ", offspring.get().getPossibleNatures().stream().map(Nature::getDisplayName).toList()))),
                    Text.empty(),
                    Text.literal("IVs"),
                    Text.literal("HP: " + ivs.get(Stats.HP).toString()),
                    Text.literal("Attack: " + ivs.get(Stats.ATTACK).toString()),
                    Text.literal("Defence: " + ivs.get(Stats.DEFENCE).toString()),
                    Text.literal("Sp.Atk: " + ivs.get(Stats.SPECIAL_ATTACK).toString()),
                    Text.literal("Sp.Def: " + ivs.get(Stats.SPECIAL_DEFENCE).toString()),
                    Text.literal("Speed: " + ivs.get(Stats.SPEED).toString()),
                    Text.empty(),
                    Text.literal("Aspects: " + String.join(", ", tile.getAspects())),
                    Text.literal("Shiny Rate: 1/" + (int)(1 / offspring.get().getShinyRate()))
                )))
                .build();
        }
        else {
            offspringInfo = GooeyButton.builder()
                .display(Items.BARRIER.getDefaultStack())
                .with(DataComponentTypes.CUSTOM_NAME, Text.literal("No preview available."))
                .with(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal("Select two compatible Pokemon to view the preview."))))
                .build();
        }

        ChestTemplate template = ChestTemplate.builder(6)
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
            .with(DataComponentTypes.CUSTOM_NAME, Text.literal("Egg Collection"))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal(mixinPasture.count() + "/" + mixinPasture.size() + " eggs held"))))
            .build();
    }
}

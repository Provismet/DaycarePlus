package com.provismet.cobblemon.daycareplus.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.cobblemon.mod.common.CobblemonSounds;
import com.provismet.cobblemon.daycareplus.breeding.BreedingLink;
import com.provismet.cobblemon.daycareplus.config.Options;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import com.provismet.cobblemon.daycareplus.util.Styles;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Unit;

import java.util.List;
import java.util.UUID;

public interface IntroGUI {
    static Page create (IMixinPastureBlockEntity pastureMixin, ServerPlayerEntity serverPlayer) {
        GooeyButton filler = GooeyButton.builder()
            .display(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack())
            .with(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE)
            .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();

        GooeyButton activateBreeding = GooeyButton.builder()
            .display(DPItems.POKEMON_EGG.getDefaultStack())
            .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.intro.daycare").styled(Styles.WHITE_NO_ITALICS))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(
                Text.translatable("gui.button.daycareplus.intro.daycare.tooltip.1").styled(Styles.GRAY_NO_ITALICS),
                Text.translatable("gui.button.daycareplus.intro.daycare.tooltip.2", BreedingLink.count(serverPlayer), Options.getMaxPasturesPerPlayer()).styled(Styles.GRAY_NO_ITALICS)
            )))
            .onClick(buttonAction -> {
                if (BreedingLink.isAtLimit(serverPlayer)) {
                    serverPlayer.playSoundToPlayer(SoundEvents.ENTITY_VILLAGER_NO, SoundCategory.PLAYERS, 1f, 1f);
                    serverPlayer.sendMessage(Text.translatable("message.overlay.daycareplus.limit_reached").formatted(Formatting.RED));
                }
                else {
                    if (pastureMixin.getBreederUUID() == null) {
                        pastureMixin.setBreederUUID(UUID.randomUUID());
                    }
                    BreedingLink.add(serverPlayer, pastureMixin.getBreederUUID());
                    pastureMixin.setShouldBreed(true);
                    UIManager.closeUI(buttonAction.getPlayer());
                }
            })
            .build();

        GooeyButton noBreeding = GooeyButton.builder()
            .display(Items.SHORT_GRASS.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.intro.pasture").styled(Styles.WHITE_NO_ITALICS))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("gui.button.daycareplus.intro.pasture.tooltip").styled(Styles.GRAY_NO_ITALICS))))
            .onClick(buttonAction -> {
                pastureMixin.setShouldBreed(false);
                pastureMixin.setSkipIntroDialogue(true);
                UIManager.closeUI(buttonAction.getPlayer());
            })
            .build();

        ChestTemplate template = ChestTemplate.builder(3)
            .fill(filler)
            .set(1, 2, activateBreeding)
            .set(1, 6, noBreeding)
            .build();

        return GooeyPage.builder()
            .title("Daycare Setup")
            .template(template)
            .onOpen(pageAction -> pageAction.getPlayer().playSoundToPlayer(CobblemonSounds.PC_ON, SoundCategory.BLOCKS, 1f, 1f))
            .onClose(pageAction -> pageAction.getPlayer().playSoundToPlayer(CobblemonSounds.PC_OFF, SoundCategory.BLOCKS, 1f, 1f))
            .build();
    }
}

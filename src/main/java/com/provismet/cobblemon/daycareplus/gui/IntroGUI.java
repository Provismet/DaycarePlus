package com.provismet.cobblemon.daycareplus.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.util.Styles;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;

import java.util.List;

public interface IntroGUI {
    static Page create (IMixinPastureBlockEntity pastureMixin) {
        GooeyButton filler = GooeyButton.builder()
            .display(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack())
            .with(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE)
            .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();

        GooeyButton activateBreeding = GooeyButton.builder()
            .display(Items.EGG.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.intro.daycare").styled(Styles.WHITE_NO_ITALICS))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(Text.translatable("gui.button.daycareplus.intro.daycare.tooltip").styled(Styles.GRAY_NO_ITALICS))))
            .onClick(buttonAction -> {
                pastureMixin.setShouldBreed(true);
                UIManager.closeUI(buttonAction.getPlayer());
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
            .build();
    }
}

package com.provismet.cobblemon.daycareplus.gui;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.Page;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

public interface IntroGUI {
    static Page create (IMixinPastureBlockEntity pastureMixin) {
        GooeyButton activateBreeding = GooeyButton.builder()
            .display(Items.EGG.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.literal("Daycare"))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal("Use this pasture to breed Pokemon."))))
            .onClick(buttonAction -> {
                pastureMixin.setShouldBreed(true);
                UIManager.closeUI(buttonAction.getPlayer());
            })
            .build();

        GooeyButton noBreeding = GooeyButton.builder()
            .display(Items.SHORT_GRASS.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.literal("Pasture"))
            .with(DataComponentTypes.LORE, new LoreComponent(List.of(Text.literal("Use this pasture cosmetically without breeding."))))
            .onClick(buttonAction -> {
                pastureMixin.setShouldBreed(false);
                pastureMixin.setSkipIntroDialogue(true);
                UIManager.closeUI(buttonAction.getPlayer());
            })
            .build();

        ChestTemplate template = ChestTemplate.builder(3)
            .set(2, 2, activateBreeding)
            .set(2, 6, noBreeding)
            .build();

        return GooeyPage.builder()
            .title("Daycare Setup")
            .template(template)
            .build();
    }
}

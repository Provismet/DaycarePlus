package com.provismet.cobblemon.daycareplus.gui;

import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.ButtonBase;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.InventoryListenerButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.PageAction;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import ca.landonjw.gooeylibs2.api.template.types.InventoryTemplate;
import com.provismet.cobblemon.daycareplus.item.component.EggBagDataComponent;
import com.provismet.cobblemon.daycareplus.registries.DPIconItems;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import com.provismet.cobblemon.daycareplus.util.Styles;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;

public class EggBagGUI extends GooeyPage {
    private static final int ITEMS_PER_ROW = 9;
    private static final int ROWS_PER_PAGE = 5;
    private static final int ITEMS_PER_PAGE = ITEMS_PER_ROW * ROWS_PER_PAGE;

    private final ItemStack bag;
    private int minSlotDisplayed;

    public EggBagGUI (@NotNull Template template, @Nullable InventoryTemplate inventoryTemplate, @Nullable Text title, ItemStack bag) {
        this(template, inventoryTemplate, title, null, null, bag);
    }

    public EggBagGUI (@NotNull Template template, @Nullable InventoryTemplate inventoryTemplate, @Nullable Text title, @Nullable Consumer<PageAction> onOpen, @Nullable Consumer<PageAction> onClose, ItemStack bag) {
        super(template, inventoryTemplate, title, onOpen, onClose);
        this.bag = bag;
        this.minSlotDisplayed = 0;
    }

    public static EggBagGUI createFrom (ItemStack bag) {
        Template template = EggBagGUI.createTemplate();
        //InventoryTemplate inventoryTemplate = EggBagGUI.createFromPlayer(bag);
        EggBagGUI gui = new EggBagGUI(template, null, Text.translatable(bag.getTranslationKey()), bag);
        gui.reset();
        return gui;
    }

    public ItemStack getBag () {
        return this.bag;
    }

    public void reset () {
        EggBagDataComponent component = this.getComponent();
        Template template = EggBagGUI.createTemplate();

        for (int i = 0; i + 9 < template.getSize() && i + this.minSlotDisplayed < component.contents().size(); ++i) {
            int slotToTake = i + this.minSlotDisplayed;
            template.getSlot(i + 9).setButton(
                GooeyButton.builder()
                    .display(component.contents().get(slotToTake))
                    .onClick(buttonAction -> {
                        Optional<ItemStack> stack = component.get(slotToTake);
                        if (stack.isPresent() && buttonAction.getPlayer().giveItemStack(stack.get())) {
                            this.bag.set(DPItemDataComponents.HELD_EGGS, component.remove(slotToTake));
                            this.reset();
                        }
                    })
                    .build()
            );
        }

        this.setTemplate(template);
    }

    public void nextPage () {
        this.minSlotDisplayed += ITEMS_PER_PAGE;
        this.reset();
    }

    public void previousPage () {
        this.minSlotDisplayed = Math.max(0, this.minSlotDisplayed - ITEMS_PER_PAGE);
        this.reset();
    }

    private EggBagDataComponent getComponent () {
        return this.bag.getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT);
    }

    private static Template createTemplate () {
        ButtonBase filler = GooeyButton.builder()
            .display(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack())
            .with(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE)
            .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();

        ButtonBase borderFiller = GooeyButton.builder()
            .display(Items.BLACK_STAINED_GLASS_PANE.getDefaultStack())
            .with(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE)
            .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();

        Button previous = GooeyButton.builder()
            .display(DPIconItems.LEFT.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.prev").styled(Styles.WHITE_NO_ITALICS))
            .onClick(buttonAction -> {
                if (buttonAction.getPage() instanceof EggBagGUI eggBagGUI) {
                    eggBagGUI.previousPage();
                }
            })
            .build();

        Button next = GooeyButton.builder()
            .display(DPIconItems.RIGHT.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.next").styled(Styles.WHITE_NO_ITALICS))
            .onClick(buttonAction -> {
                if (buttonAction.getPage() instanceof EggBagGUI eggBagGUI) {
                    eggBagGUI.nextPage();
                }
            })
            .build();

        return ChestTemplate.builder(ROWS_PER_PAGE + 1)
            .fill(filler)
            .row(0, borderFiller)
            .set(0, previous)
            .set(8, next)
            .build();
    }

    private static InventoryTemplate createFromPlayer (ItemStack eggBag) {
        EggBagDataComponent component = eggBag.getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT);

        InventoryListenerButton button = new InventoryListenerButton(buttonAction -> {
            Optional<Integer> slot = buttonAction.getInventorySlot();
            if (slot.isPresent() && !component.isFull()) {
                ItemStack stack = buttonAction.getPlayer().getInventory().getStack(slot.get());
                if (stack.isOf(DPItems.POKEMON_EGG)) {
                    eggBag.set(DPItemDataComponents.HELD_EGGS, component.add(stack.copyAndEmpty()));
                }
                if (buttonAction.getPage() instanceof EggBagGUI gui) gui.reset();
            }
        });

        return InventoryTemplate.builder()
            .fill(button)
            .build();
    }
}

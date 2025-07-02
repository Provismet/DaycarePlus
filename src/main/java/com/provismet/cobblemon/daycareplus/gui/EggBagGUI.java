package com.provismet.cobblemon.daycareplus.gui;

import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.ButtonBase;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.PageAction;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import ca.landonjw.gooeylibs2.api.template.types.InventoryTemplate;
import com.provismet.cobblemon.daycareplus.api.EggHelper;
import com.provismet.cobblemon.daycareplus.item.component.EggBagDataComponent;
import com.provismet.cobblemon.daycareplus.registries.DPIconItems;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.util.Styles;
import com.provismet.cobblemon.daycareplus.util.tag.DPItemTags;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class EggBagGUI extends GooeyPage {
    private static final int ITEMS_PER_ROW = 9;
    private static final int ROWS_PER_PAGE = 5;
    private static final int ITEMS_PER_PAGE = ITEMS_PER_ROW * ROWS_PER_PAGE;

    private final ItemStack bag;
    private int minSlotDisplayed;
    private boolean isAtEnd;
    private ServerPlayerEntity player;

    public EggBagGUI (@NotNull Template template, @Nullable InventoryTemplate inventoryTemplate, @Nullable Text title, ItemStack bag) {
        this(template, inventoryTemplate, title, null, null, bag);
    }

    public EggBagGUI (@NotNull Template template, @Nullable InventoryTemplate inventoryTemplate, @Nullable Text title, @Nullable Consumer<PageAction> onOpen, @Nullable Consumer<PageAction> onClose, ItemStack bag) {
        super(template, inventoryTemplate, title, onOpen, onClose);
        this.bag = bag;
        this.minSlotDisplayed = 0;
        this.isAtEnd = false;
    }

    public static EggBagGUI createFrom (ItemStack bag, ServerPlayerEntity player) {
        Template template = EggBagGUI.createBorder();
        EggBagGUI.fillSpace(template);
        InventoryTemplate inventoryTemplate = EggBagGUI.createFromPlayer(bag, player);
        EggBagGUI gui = new EggBagGUI(template, inventoryTemplate, Text.translatable(bag.getTranslationKey()), bag);
        gui.player = player;
        gui.reset();
        return gui;
    }

    public ItemStack getBag () {
        return this.bag;
    }

    public void reset () {
        EggBagDataComponent component = this.getComponent();
        EggBagGUI.fillSpace(this.getTemplate());

        InventoryTemplate inventoryTemplate = EggBagGUI.createFromPlayer(this.bag, this.player);
        this.setPlayerInventoryTemplate(inventoryTemplate);

        this.isAtEnd = false;
        for (int i = 0; i + 9 < this.getTemplate().getSize() && i + this.minSlotDisplayed < component.contents().size(); ++i) {
            int slotToTake = i + this.minSlotDisplayed;
            this.getTemplate().getSlot(i + 9).setButton(
                GooeyButton.builder()
                    .display(component.contents().get(slotToTake))
                    .onClick(buttonAction -> {
                        Optional<ItemStack> stack = component.get(slotToTake);
                        if (stack.isPresent() && buttonAction.getPlayer().giveItemStack(stack.get())) {
                            this.bag.set(DPItemDataComponents.HELD_EGGS, component.remove(slotToTake));
                            buttonAction.getPlayer().playSoundToPlayer(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS, 1f, 1f);
                            this.reset();
                        }
                    })
                    .build()
            );

            if (i + this.minSlotDisplayed == component.contents().size() - 1) this.isAtEnd = true;
        }
        this.update();
    }

    public void nextPage () {
        if (!this.isAtEnd) {
            this.minSlotDisplayed += ITEMS_PER_PAGE;
            this.reset();
        }
    }

    public void previousPage () {
        this.minSlotDisplayed = Math.max(0, this.minSlotDisplayed - ITEMS_PER_PAGE);
        this.reset();
    }

    private EggBagDataComponent getComponent () {
        return this.bag.getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT);
    }

    private static Template createBorder () {
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

        Button takeAll = GooeyButton.builder()
            .display(DPIconItems.TAKE_ALL.getDefaultStack())
            .with(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.take").styled(Styles.WHITE_NO_ITALICS))
            .onClick(buttonAction -> {
                if (buttonAction.getPage() instanceof EggBagGUI eggBagGUI) {
                    EggBagDataComponent component = eggBagGUI.getBag().getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT);
                    component = component.addAllCopiesAndEmpty(buttonAction.getPlayer().getInventory().main);
                    eggBagGUI.getBag().set(DPItemDataComponents.HELD_EGGS, component);
                    buttonAction.getPlayer().playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1f, 1f);
                    eggBagGUI.reset();
                }
            })
            .build();

        return ChestTemplate.builder(ROWS_PER_PAGE + 1)
            .row(0, borderFiller)
            .set(0, previous)
            .set(7, takeAll)
            .set(8, next)
            .build();
    }

    private static void fillSpace (Template input) {
        ButtonBase filler = GooeyButton.builder()
            .display(Items.GRAY_STAINED_GLASS_PANE.getDefaultStack())
            .with(DataComponentTypes.HIDE_TOOLTIP, Unit.INSTANCE)
            .with(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE)
            .build();

        input.getSlots().forEach(delegate -> {
            if (delegate.getButton().isEmpty()
                || !(delegate.getButton().get().getDisplay().isIn(DPItemTags.GUI) || delegate.getButton().get().getDisplay().isOf(Items.BLACK_STAINED_GLASS_PANE))) {
                delegate.setButton(filler);
            }
        });
    }

    private static InventoryTemplate createFromPlayer (ItemStack eggBag, ServerPlayerEntity player) {
        EggBagDataComponent component = eggBag.getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT);

        Function<ItemStack, Button> makeButton = stack -> GooeyButton.builder()
            .display(stack)
            .onClick(buttonAction -> {
                if (EggHelper.isEgg(stack)) {
                    eggBag.set(DPItemDataComponents.HELD_EGGS, component.addCopyAndEmpty(stack));
                    buttonAction.getPlayer().playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1f, 1f);
                }
                if (buttonAction.getPage() instanceof EggBagGUI gui) gui.reset();
            })
            .build();

        List<Button> buttons = Stream.concat(
            player.getInventory().main.subList(9, player.getInventory().main.size())
                .stream()
                .map(makeButton),
            player.getInventory().main.subList(0, 9) // The hotbar appears first in the inventory, but we need it last in the template.
                .stream()
                .map(makeButton)
        ).toList();

        return InventoryTemplate.builder()
            .fillFromList(buttons)
            .build();
    }
}

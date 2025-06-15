package com.provismet.cobblemon.daycareplus.item;

import ca.landonjw.gooeylibs2.api.UIManager;
import com.provismet.cobblemon.daycareplus.gui.EggBagGUI;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.item.component.EggBagDataComponent;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import com.provismet.cobblemon.daycareplus.util.Styles;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.Optional;

public class EggBagItem extends PolymerItem {
    private final int eggsToTick;

    public EggBagItem (Settings settings, Item baseVanillaItem, PolymerModelData modelData, int eggsToTick) {
        super(settings, baseVanillaItem, modelData);
        this.eggsToTick = eggsToTick;
    }

    @Override
    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand) {
        if (user.isCreative()) {
            user.sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.creative").formatted(Formatting.RED), true);
            return TypedActionResult.fail(user.getStackInHand(hand));
        }

        if (user instanceof ServerPlayerEntity serverPlayer) {
            UIManager.openUIForcefully(serverPlayer, EggBagGUI.createFrom(user.getStackInHand(hand), serverPlayer));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock (ItemUsageContext context) {
        if (context.getWorld().getBlockEntity(context.getBlockPos()) instanceof IMixinPastureBlockEntity daycare) {
            EggBagDataComponent component = context.getStack().get(DPItemDataComponents.HELD_EGGS);
            if (component != null) {
                int remainingSlots = component.capacity() - component.contents().size();
                List<ItemStack> eggs = daycare.withdraw(remainingSlots);
                int size = eggs.size();
                context.getStack().set(DPItemDataComponents.HELD_EGGS, component.addAll(eggs));
                if (context.getPlayer() != null) {
                    this.playInsertSound(context.getPlayer());
                    if (size == 1) context.getPlayer().sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.collection.singular", size), true);
                    else context.getPlayer().sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.collection.plural", size), true);
                }
                return ActionResult.SUCCESS;
            }
        }
        return super.useOnBlock(context);
    }

    // Bag is on the cursor, hover over another item to collect it.
    @Override
    public boolean onStackClicked (ItemStack eggBag, Slot slot, ClickType clickType, PlayerEntity player) {
        if (clickType != ClickType.RIGHT || player.isCreative()) return false;

        EggBagDataComponent component = eggBag.get(DPItemDataComponents.HELD_EGGS);
        if (component == null) return false;

        ItemStack otherItem = slot.getStack();
        if (!otherItem.isEmpty() && !otherItem.isOf(DPItems.POKEMON_EGG)) return true;

        EggBagDataComponent.Builder builder = new EggBagDataComponent.Builder(component);
        if (otherItem.isEmpty()) { // Place egg in inventory.
            this.playRemoveOneSound(player);
            ItemStack stackFromBag = builder.remove();
            ItemStack itemStack3 = slot.insertStack(stackFromBag);
            builder.add(itemStack3);
        }
        else if (!component.isFull()) { // Put egg in bag.
            builder.add(otherItem.copyAndEmpty());
            this.playInsertSound(player);
        }

        eggBag.set(DPItemDataComponents.HELD_EGGS, builder.build());
        return true;
    }

    // Bag is in the inventory, withdraw an egg or put one in.
    @Override
    public boolean onClicked (ItemStack eggBag, ItemStack cursorStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && slot.canTakePartial(player) && !player.isCreative()) {
            EggBagDataComponent component = eggBag.get(DPItemDataComponents.HELD_EGGS);
            if (component == null) return false;

            EggBagDataComponent.Builder builder = new EggBagDataComponent.Builder(component);

            if (cursorStack.isEmpty() && !component.isEmpty()) {
                ItemStack itemStack = builder.remove();
                this.playRemoveOneSound(player);
                cursorStackReference.set(itemStack);
            }
            else if (cursorStack.isOf(DPItems.POKEMON_EGG) && !component.isFull()) {
                builder.add(cursorStack.copyAndEmpty());
            }

            eggBag.set(DPItemDataComponents.HELD_EGGS, builder.build());
            return true;
        }
        return false;
    }

    // TODO: Only exists clientside, future goal is to make this work on compatible clients. Don't know how to check that yet though.
    @Override
    public Optional<TooltipData> getTooltipData (ItemStack stack) {
        return !stack.contains(DataComponentTypes.HIDE_TOOLTIP) && !stack.contains(DataComponentTypes.HIDE_ADDITIONAL_TOOLTIP)
            ? Optional.ofNullable(stack.getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT)).map(EggBagDataComponent::asBundle).map(BundleTooltipData::new)
            : Optional.empty();
    }

    @Override
    public void appendTooltip (ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        EggBagDataComponent component = stack.getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT);
        tooltip.add(Text.translatable("tooltip.daycareplus.egg_bag.eggs_held", component.contents().size(), component.capacity()).styled(Styles.GRAY_NO_ITALICS));
    }

    public void tickEggs (ItemStack stack, ServerPlayerEntity player, int amount) {
        EggBagDataComponent component = stack.getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT);
        if (component.contents().isEmpty()) return;

        for (int i = 0; i < this.eggsToTick; ++i) {
            component.get(i).ifPresent(eggStack -> {
                if (eggStack.getItem() instanceof PokemonEggItem egg) {
                    egg.decrementEggSteps(eggStack, amount, player);
                }
            });
        }
        stack.set(DPItemDataComponents.HELD_EGGS, component.validate());
    }

    private void playRemoveOneSound (Entity entity) {
        entity.getWorld().playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS, 0.8f, 0.8f + entity.getWorld().getRandom().nextFloat() * 0.4f, true);
    }

    private void playInsertSound (Entity entity) {
        entity.getWorld().playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.8f, 0.8f, true);
    }
}

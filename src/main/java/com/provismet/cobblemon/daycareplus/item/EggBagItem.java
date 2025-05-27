package com.provismet.cobblemon.daycareplus.item;

import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.item.component.EggBagDataComponent;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import java.util.List;

public class EggBagItem extends PolymerItem {
    private final int eggsToTick;

    public EggBagItem (Settings settings, Item baseVanillaItem, PolymerModelData modelData, int eggsToTick) {
        super(settings, baseVanillaItem, modelData);
        this.eggsToTick = eggsToTick;
    }

    public boolean isActive (ItemStack stack) {
        return true;
        // TODO: Implement active states
        //return stack.getOrDefault(DPItemDataComponents.ACTIVE_BAG, false);
    }

    public void setActive (ItemStack stack, boolean activeState) {
        stack.set(DPItemDataComponents.ACTIVE_BAG, activeState);
    }

    @Override
    public ActionResult useOnBlock (ItemUsageContext context) {
        DaycarePlusServer.LOGGER.info("Used egg bag on block.");
        if (context.getWorld().getBlockEntity(context.getBlockPos()) instanceof IMixinPastureBlockEntity daycare) {
            DaycarePlusServer.LOGGER.info("Block is daycare");
            EggBagDataComponent component = context.getStack().get(DPItemDataComponents.HELD_EGGS);
            if (component != null) {
                DaycarePlusServer.LOGGER.info("Bag has component");
                int remainingSlots = component.capacity() - component.contents().size();
                List<ItemStack> eggs = daycare.withdraw(remainingSlots);
                context.getStack().set(DPItemDataComponents.HELD_EGGS, component.addAll(eggs));

                if (context.getPlayer() != null) this.playInsertSound(context.getPlayer());

                DaycarePlusServer.LOGGER.info("Should work");
                return ActionResult.SUCCESS;
            }
        }
        DaycarePlusServer.LOGGER.info("Did not work.");
        return super.useOnBlock(context);
    }

    @Override
    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand) {
        return super.use(world, user, hand); // TODO: Open an inventory window.
    }

    // Inserts into the bag
    @Override
    public boolean onStackClicked (ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        return super.onStackClicked(stack, slot, clickType, player);
    }

    @Override
    public void appendTooltip (ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        EggBagDataComponent component = stack.getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT);
        tooltip.add(Text.literal("Eggs held: " + component.contents().size() + "/" + component.capacity()));
    }

    @Override
    public void inventoryTick (ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if (!(entity instanceof ServerPlayerEntity player) || player.age % 20 != 0) return;

        if (this.isActive(stack)) {
            EggBagDataComponent component = stack.getOrDefault(DPItemDataComponents.HELD_EGGS, EggBagDataComponent.DEFAULT);
            if (component.contents().isEmpty()) return;

            for (int i = 0; i < this.eggsToTick; ++i) {
                component.get(i).ifPresent(eggStack -> {
                    if (eggStack.getItem() instanceof PokemonEggItem egg) {
                        egg.decrementEggSteps(eggStack, 20, player); // TODO: Make this number variable.
                    }
                });
            }
            stack.set(DPItemDataComponents.HELD_EGGS, component.validate());
        }
    }

    private void playRemoveOneSound (Entity entity) {
        entity.getWorld().playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BUNDLE_REMOVE_ONE, SoundCategory.PLAYERS, 0.8f, 0.8f + entity.getWorld().getRandom().nextFloat() * 0.4f, true);
    }

    private void playInsertSound (Entity entity) {
        entity.getWorld().playSound(entity.getX(), entity.getY(), entity.getZ(), SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 0.8f, 0.8f, true);
    }
}

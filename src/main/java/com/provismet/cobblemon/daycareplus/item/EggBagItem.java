package com.provismet.cobblemon.daycareplus.item;

import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.List;

@Deprecated
public class EggBagItem extends PolymerItem {
    private final Item convertTo;

    public EggBagItem (Settings settings, Item baseVanillaItem, PolymerModelData modelData, Item convertTo) {
        super(settings, baseVanillaItem, modelData);
        this.convertTo = convertTo;
    }

    @Override
    public void appendTooltip (ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        super.appendTooltip(stack, context, tooltip, type);
        tooltip.add(Text.literal("Bags are being rebranded as incubators now!"));
        tooltip.add(Text.literal("Place this item in the crafting grid to update and reset it."));
        tooltip.add(Text.literal("This will reset its data, empty it or hatch your eggs first!"));
    }

    @Override
    public void inventoryTick (ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);

        if (entity instanceof PlayerEntity player && !player.getOffHandStack().equals(stack)) {
            int count = stack.getCount();
            player.getInventory().removeStack(slot);
            ItemStack newStack = new ItemStack(this.convertTo, count);
            if (!player.getInventory().insertStack(slot, newStack)) {
                // For some reason the new item failed to be given to the player, try again.
                PlayerExtensionsKt.giveOrDropItemStack(player, newStack, false);
            }
        }
    }
}

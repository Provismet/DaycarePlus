package com.provismet.cobblemon.daycareplus.item;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.container.GooeyContainer;
import com.cobblemon.mod.common.block.PastureBlock;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.provismet.cobblemon.daycareplus.gui.EggBagGUI;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.item.component.EggBagDataComponent;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.util.Styles;
import com.provismet.cobblemon.daycareplus.util.tag.DPItemTags;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class EggBagItem extends PolymerItem {
    private static final Set<String> HATCH_ABILITIES = Set.of("flamebody", "steamengine", "magmaarmor");
    private final int eggsToTick;

    public EggBagItem (Settings settings, Item baseVanillaItem, PolymerModelData modelData, int eggsToTick) {
        super(settings, baseVanillaItem, modelData);
        this.eggsToTick = eggsToTick;
    }

    @Override
    public void inventoryTick (ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, world, entity, slot, selected);
        if (!(entity instanceof ServerPlayerEntity player) || player.age % 20 != 0) return;

        // Exit early if other egg bags are found.
        for (int i = slot + 1; i < player.getInventory().size(); ++i) {
            if (player.getInventory().getStack(i).isIn(DPItemTags.EGG_BAGS)) return;
        }

        int abilityMultiplier = 1;
        for (Pokemon pokemon : PlayerExtensionsKt.party(player)) {
            if (HATCH_ABILITIES.contains(pokemon.getAbility().getName().toLowerCase(Locale.ROOT))) {
                abilityMultiplier = 2;
                break;
            }
        }

        this.tickEggs(stack, player, 20 * abilityMultiplier);
        if (player.currentScreenHandler instanceof GooeyContainer gooeyContainer && gooeyContainer.getPage() instanceof EggBagGUI gui) {
            gui.reset();
        }
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
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();

        if (block instanceof PastureBlock pastureBlock) {
            BlockPos pasturePos = pastureBlock.getBasePosition(context.getWorld().getBlockState(context.getBlockPos()), context.getBlockPos());

            if (context.getWorld().getBlockEntity(pasturePos) instanceof IMixinPastureBlockEntity daycare) {
                EggBagDataComponent component = context.getStack().get(DPItemDataComponents.HELD_EGGS);
                if (component != null) {
                    int remainingSlots = component.capacity() - component.contents().size();
                    List<ItemStack> eggs = daycare.withdraw(remainingSlots);
                    int size = eggs.size();
                    context.getStack().set(DPItemDataComponents.HELD_EGGS, component.addAll(eggs));
                    if (context.getPlayer() instanceof ServerPlayerEntity player) {
                        this.playInsertSound(player);
                        if (size == 1)
                            player.sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.collection.singular", size), true);
                        else
                            player.sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.collection.plural", size), true);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }
        return super.useOnBlock(context);
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

    private void playInsertSound (ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1f, 1f);
    }
}

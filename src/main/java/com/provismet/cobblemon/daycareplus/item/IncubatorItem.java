package com.provismet.cobblemon.daycareplus.item;

import com.cobblemon.mod.common.block.PastureBlock;
import com.provismet.cobblemon.daycareplus.gui.EggStorageGUI;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.item.component.IncubatorOwner;
import com.provismet.cobblemon.daycareplus.item.component.IncubatorType;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.storage.EggStorage;
import com.provismet.cobblemon.daycareplus.storage.IncubatorCollection;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class IncubatorItem extends PolymerItem {
    public static final Set<String> HATCH_ABILITIES = Set.of("flamebody", "steamengine", "magmaarmor");
    private final PolymerModelData hasEggData;

    public IncubatorItem (Settings settings, Item baseVanillaItem, PolymerModelData normalData, PolymerModelData hasEggData) {
        super(settings, baseVanillaItem, normalData);
        this.hasEggData = hasEggData;
    }

    @Override
    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isCreative()) {
            user.sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.creative").formatted(Formatting.RED), true);
            return TypedActionResult.fail(stack);
        }

        if (user instanceof ServerPlayerEntity serverPlayer) {
            if (stack.get(DPItemDataComponents.INCUBATOR_OWNER) == null) {
                stack.set(DPItemDataComponents.INCUBATOR_OWNER, new IncubatorOwner(serverPlayer));
                return TypedActionResult.success(stack);
            }
            else if (this.isPlayerOwner(stack, serverPlayer)) {
                IncubatorType incubatorType = stack.get(DPItemDataComponents.INCUBATOR_TYPE);
                if (incubatorType == null) {
                    user.sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.typeless").formatted(Formatting.RED), true);
                    return TypedActionResult.fail(stack);
                }

                IncubatorCollection collection = IncubatorCollection.getOrCreate(serverPlayer);
                Optional<EggStorage> currentStorage = collection.get(incubatorType.type());
                if (currentStorage.isPresent()) {
                    this.getStorage(stack).ifPresent(storage -> {
                        if (currentStorage.get().getCapacity() < storage.getCapacity() && currentStorage.get().getEggsToTick() < storage.getEggsToTick()) {
                            currentStorage.get().setTier(storage.getTier());
                            currentStorage.get().setCapacity(storage.getCapacity());
                            currentStorage.get().setEggsToTick(storage.getEggsToTick());
                        }
                    });
                }
                else {
                    EggStorage storage = EggStorage.fromSettings(incubatorType.tier());

                    if (storage != null) {
                        collection.put(incubatorType.type(), storage);
                    }
                }
                EggStorageGUI gui = EggStorageGUI.create(serverPlayer, incubatorType.type());
                if (gui != null) gui.open();
            }
            else {
                user.sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.stolen").formatted(Formatting.RED), true);
                return TypedActionResult.fail(user.getStackInHand(hand));
            }
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock (ItemUsageContext context) {
        Block block = context.getWorld().getBlockState(context.getBlockPos()).getBlock();

        if (block instanceof PastureBlock pastureBlock) {
            BlockPos pasturePos = pastureBlock.getBasePosition(context.getWorld().getBlockState(context.getBlockPos()), context.getBlockPos());

            if (context.getWorld().getBlockEntity(pasturePos) instanceof IMixinPastureBlockEntity daycare) {
                Optional<EggStorage> storage = this.getStorage(context.getStack());
                if (storage.isPresent()) {
                    int remainingSlots = storage.get().size() - storage.get().getCapacity();
                    List<ItemStack> eggs = daycare.withdraw(remainingSlots);
                    eggs.forEach(storage.get()::addCopyAndEmpty);

                    if (context.getPlayer() instanceof ServerPlayerEntity player) {
                        this.playInsertSound(player);
                        if (eggs.size() == 1)
                            player.sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.collection.singular", eggs.size()), true);
                        else
                            player.sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.collection.plural", eggs.size()), true);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip (ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        IncubatorOwner owner = stack.get(DPItemDataComponents.INCUBATOR_OWNER);
        if (owner != null) tooltip.add(Text.literal(owner.playerName()));
    }

    public void tickEggs (ItemStack stack, ServerPlayerEntity player, int amount) {
        this.getStorage(stack).ifPresent(storage -> storage.tick(amount, player));
    }

    private boolean isPlayerOwner (ItemStack stack, ServerPlayerEntity player) {
        return stack.getOrDefault(DPItemDataComponents.INCUBATOR_OWNER, IncubatorOwner.DEFAULT).matches(player);
    }

    private Optional<EggStorage> getStorage (ItemStack stack) {
        IncubatorOwner owner = stack.get(DPItemDataComponents.INCUBATOR_OWNER);
        IncubatorType type = stack.get(DPItemDataComponents.INCUBATOR_TYPE);
        if (owner != null && type != null) {
            return IncubatorCollection.getCollection(owner.uuid()).get(type.type());
        }
        return Optional.empty();
    }

    @Override
    public int getPolymerCustomModelData (ItemStack itemStack, @Nullable ServerPlayerEntity player) {
        return super.getPolymerCustomModelData(itemStack, player);
    }

    private void playInsertSound (ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1f, 1f);
    }
}

package com.provismet.cobblemon.daycareplus.item;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.container.GooeyContainer;
import com.cobblemon.mod.common.block.PastureBlock;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.provismet.cobblemon.daycareplus.gui.EggBagGUI;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.item.component.HeldEggsDataComponent;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public class IncubatorItem extends PolymerItem {
    private static final Set<String> HATCH_ABILITIES = Set.of("flamebody", "steamengine", "magmaarmor");
    private final int eggsToTick;
    private final PolymerModelData hasEggData;

    public IncubatorItem (Settings settings, Item baseVanillaItem, PolymerModelData normalData, PolymerModelData hasEggData, int eggsToTick) {
        super(settings, baseVanillaItem, normalData);
        this.eggsToTick = eggsToTick;
        this.hasEggData = hasEggData;
    }

    @Override
    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand) {
        if (user.isCreative()) {
            user.sendMessage(Text.translatable("message.overlay.daycareplus.egg_bag.creative").formatted(Formatting.RED), true);
            return TypedActionResult.fail(user.getStackInHand(hand));
        }

        if (user instanceof ServerPlayerEntity serverPlayer) {
            //UIManager.openUIForcefully(serverPlayer, EggBagGUI.createFrom(user.getStackInHand(hand), serverPlayer));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @Override
    public ActionResult useOnBlock (ItemUsageContext context) {
        if (context.getPlayer() instanceof ServerPlayerEntity player) {
            player.sendMessage(Text.literal("Incubators are currently disabled."), true);
        }
        return super.useOnBlock(context);
    }

    @Override
    public void appendTooltip (ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Incubators are currently disabled.").styled(Styles.GRAY_NO_ITALICS));
    }

    public void tickEggs (ItemStack stack, ServerPlayerEntity player, int amount) {
        HeldEggsDataComponent component =HeldEggsDataComponent.DEFAULT;
        if (component.contents().isEmpty()) return;

        for (int i = 0; i < this.eggsToTick; ++i) {
            component.get(i).ifPresent(eggStack -> {
                if (eggStack.getItem() instanceof PokemonEggItem egg) {
                    egg.decrementEggSteps(eggStack, amount, player);
                }
            });
        }
        //stack.set(DPItemDataComponents.HELD_EGGS, component.validate());
    }

    private void playInsertSound (ServerPlayerEntity player) {
        player.playSoundToPlayer(SoundEvents.ITEM_BUNDLE_INSERT, SoundCategory.PLAYERS, 1f, 1f);
    }
}

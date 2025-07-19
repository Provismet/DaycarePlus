package com.provismet.cobblemon.daycareplus.item;

import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.battles.model.actor.BattleActor;
import com.cobblemon.mod.common.api.item.PokemonSelectingItem;
import com.cobblemon.mod.common.battles.pokemon.BattlePokemon;
import com.cobblemon.mod.common.item.battle.BagItem;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.provismet.cobblemon.daycareplus.feature.FertilityProperty;
import com.provismet.cobblemon.daycareplus.config.DaycarePlusOptions;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import com.provismet.cobblemon.daycareplus.util.Styles;
import eu.pb4.polymer.resourcepack.api.PolymerModelData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FertilityBoosterItem extends PolymerItem implements PokemonSelectingItem {
    public FertilityBoosterItem (Settings settings, Item baseVanillaItem, PolymerModelData modelData) {
        super(settings, baseVanillaItem, modelData);
    }

    @Override
    public void appendTooltip (ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.translatable(DPItems.FERTILITY_CANDY.getTranslationKey() + ".tooltip").styled(Styles.GRAY_NO_ITALICS));
    }

    @Nullable
    @Override
    public TypedActionResult<ItemStack> applyToPokemon (@NotNull ServerPlayerEntity player, @NotNull ItemStack itemStack, @NotNull Pokemon pokemon) {
        if (!this.canUseOnPokemon(pokemon)) return TypedActionResult.fail(itemStack);

        FertilityProperty.increment(pokemon);
        player.sendMessage(Text.translatable("message.overlay.daycareplus.fertility_boosted", pokemon.getDisplayName(), FertilityProperty.get(pokemon)), true);
        itemStack.decrementUnlessCreative(1, player);
        player.playSoundToPlayer(CobblemonSounds.MEDICINE_CANDY_USE, SoundCategory.PLAYERS, 1f, 1f);
        return TypedActionResult.success(itemStack);
    }

    @Nullable
    @Override
    public BagItem getBagItem () {
        return null;
    }

    @NotNull
    @Override
    public TypedActionResult<ItemStack> use (@NotNull ServerPlayerEntity player, @NotNull ItemStack itemStack) {
        return PokemonSelectingItem.DefaultImpls.use(this, player, itemStack);
    }

    @Override
    public TypedActionResult<ItemStack> use (World world, PlayerEntity user, Hand hand) {
        if (user instanceof ServerPlayerEntity serverPlayer) {
            return this.use(serverPlayer, serverPlayer.getStackInHand(hand));
        }
        return TypedActionResult.success(user.getStackInHand(hand));
    }

    @NotNull
    @Override
    public TypedActionResult<ItemStack> interactGeneral (@NotNull ServerPlayerEntity player, @NotNull ItemStack itemStack) {
        return PokemonSelectingItem.DefaultImpls.interactGeneral(this, player, itemStack);
    }

    @Override
    public boolean canUseOnPokemon (@NotNull Pokemon pokemon) {
        return DaycarePlusOptions.doCompetitiveBreeding() && FertilityProperty.get(pokemon) < FertilityProperty.getMax();
    }

    @Override
    public boolean canUseOnBattlePokemon (@NotNull BattlePokemon battlePokemon) {
        return false;
    }

    @NotNull
    @Override
    public TypedActionResult<ItemStack> interactWithSpecificBattle (@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattlePokemon battlePokemon) {
        return TypedActionResult.fail(itemStack);
    }

    @NotNull
    @Override
    public TypedActionResult<ItemStack> interactGeneralBattle (@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattleActor battleActor) {
        return TypedActionResult.fail(itemStack);
    }

    @Override
    public void applyToBattlePokemon (@NotNull ServerPlayerEntity serverPlayerEntity, @NotNull ItemStack itemStack, @NotNull BattlePokemon battlePokemon) {

    }
}

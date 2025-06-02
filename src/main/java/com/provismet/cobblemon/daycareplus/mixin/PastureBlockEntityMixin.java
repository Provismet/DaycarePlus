package com.provismet.cobblemon.daycareplus.mixin;

import ca.landonjw.gooeylibs2.api.button.ButtonBase;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.breeding.PastureExtension;
import com.provismet.cobblemon.daycareplus.config.Options;
import com.provismet.cobblemon.daycareplus.gui.DaycareGUI;
import com.provismet.cobblemon.daycareplus.imixin.IMixinPastureBlockEntity;
import com.provismet.cobblemon.daycareplus.util.Styles;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(PokemonPastureBlockEntity.class)
public abstract class PastureBlockEntityMixin extends BlockEntity implements IMixinPastureBlockEntity {
    public PastureBlockEntityMixin (BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Unique private boolean isBreeder = false;
    @Unique private boolean skipIntroDialogue = false;
    @Unique private boolean skipDaycareGUI = false;
    @Unique private PastureExtension extension;
    @Unique private DefaultedList<ItemStack> inventory = DefaultedList.ofSize(Options.getPastureInventorySize(), ItemStack.EMPTY);
    @Unique private ButtonBase eggCounter = DaycareGUI.createEggButton(this);

    @Override
    public PastureExtension getExtension () {
        return this.extension;
    }

    @Override
    public void setExtension (PastureExtension extension) {
        this.extension = extension;
    }

    @Override
    public void setShouldBreed (boolean shouldBreed) {
        this.isBreeder = shouldBreed;
    }

    @Override
    public boolean shouldBreed () {
        return this.isBreeder;
    }

    @Override
    public void setSkipIntroDialogue (boolean skipIntroDialogue) {
        this.skipIntroDialogue = skipIntroDialogue;
    }

    @Override
    public boolean shouldSkipIntro () {
        return this.skipIntroDialogue;
    }

    @Override
    public void setShouldSkipDaycareGUI (boolean skipGUI) {
        this.skipDaycareGUI = skipGUI;
    }

    @Override
    public boolean shouldSkipDaycareGUI () {
        return this.skipDaycareGUI;
    }

    @Override
    public ButtonBase getEggCounterButton () {
        return eggCounter;
    }

    @Override
    public void add (ItemStack stack) {
        for (int i = 0; i < this.inventory.size(); ++i) {
            if (this.inventory.get(i).isEmpty()) {
                this.inventory.set(i, stack.copyAndEmpty());
                this.markDirty();
                this.updateEggCounter();
                break;
            }
        }
    }

    @Override
    public List<ItemStack> withdraw (int amount) {
        if (this.isEmpty()) return List.of();

        List<ItemStack> withdrawn = new ArrayList<>();
        for (int i = 0; i < this.inventory.size() && withdrawn.size() < amount; ++i) {
            ItemStack egg = this.getStack(i);
            if (!egg.isEmpty()) {
                withdrawn.add(egg.copy());
                this.setStack(i, ItemStack.EMPTY);
            }
        }
        return withdrawn;
    }

    @Override
    public int count () {
        int heldEggs = 0;
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) ++heldEggs;
        }
        return heldEggs;
    }

    @Override
    public int size () {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty () {
        for (ItemStack stack : this.inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack (int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack (int slot, int amount) {
        ItemStack stack = Inventories.splitStack(this.inventory, slot, amount);
        if (!stack.isEmpty()) {
            this.markDirty();
            this.updateEggCounter();
        }

        return stack;
    }

    @Override
    public ItemStack removeStack (int slot) {
        ItemStack stack = this.inventory.get(slot);
        this.inventory.set(slot, ItemStack.EMPTY);
        if (!stack.isEmpty()) {
            this.markDirty();
            this.updateEggCounter();
        }

        return stack;
    }

    @Override
    public void setStack (int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        this.markDirty();
        this.updateEggCounter();
    }

    @Override
    public boolean canPlayerUse (PlayerEntity player) {
        return true;
    }

    @Override
    public void clear () {
        this.inventory.clear();
        this.markDirty();
        this.updateEggCounter();
    }

    @Unique
    private void updateEggCounter () {
        this.eggCounter.getDisplay().set(DataComponentTypes.CUSTOM_NAME, Text.translatable("gui.button.daycareplus.eggs_held", this.count(), this.size())
            .styled(Styles.WHITE_NO_ITALICS));
        this.eggCounter.update();
    }

    @Inject(method = "TICKER$lambda$14", at = @At("HEAD"))
    private static void tick (World world, BlockPos pos, BlockState blockState, PokemonPastureBlockEntity pasture, CallbackInfo info) {
        IMixinPastureBlockEntity imixin = (IMixinPastureBlockEntity)(Object)pasture;
        if (imixin.shouldBreed()) {
            if (imixin.getExtension() == null) imixin.setExtension(new PastureExtension(pasture, Long.MAX_VALUE));
            imixin.getExtension().tick();
        }
        else {
            imixin.setExtension(null);
        }
    }

    @Inject(method = "writeNbt", at = @At("TAIL"))
    private void addNbt (NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo info) {
        NbtCompound breederNbt = new NbtCompound();
        breederNbt.putBoolean("isBreeder", this.isBreeder);

        if (this.extension != null) breederNbt.putLong("prevTick", this.extension.getPrevTime());
        else if (this.world != null) breederNbt.putLong("prevTick", this.world.getTime());
        else breederNbt.putLong("prevTick", Long.MAX_VALUE);
        Inventories.writeNbt(breederNbt, this.inventory, registryLookup);

        nbt.put("daycarePlus", breederNbt);
    }

    @Inject(method = "readNbt", at = @At("TAIL"))
    private void getNbt (NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup, CallbackInfo info) {
        if (nbt.contains("daycarePlus") && nbt.get("daycarePlus") instanceof NbtCompound daycareNbt) {
            if (daycareNbt.contains("isBreeder")) this.isBreeder = daycareNbt.getBoolean("isBreeder");
            if (daycareNbt.contains("prevTick") && this.isBreeder) {
                this.extension = new PastureExtension((PokemonPastureBlockEntity)(Object)this, daycareNbt.getLong("prevTick"));
            }
            this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
            Inventories.readNbt(daycareNbt, this.inventory, registryLookup);
        }
    }

    @Inject(method = "getMaxTethered", at = @At("HEAD"), cancellable = true, remap = false)
    private void restrictDaycare (CallbackInfoReturnable<Integer> cir) {
        if (this.isBreeder) cir.setReturnValue(2);
    }
}

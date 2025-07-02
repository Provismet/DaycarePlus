package com.provismet.cobblemon.daycareplus.item.component;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.provismet.cobblemon.daycareplus.api.EggHelper;
import com.provismet.cobblemon.daycareplus.config.Options;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.dynamic.Codecs;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public record EggBagDataComponent (List<ItemStack> contents, int capacity, Optional<String> tier) {
    public static final EggBagDataComponent DEFAULT = new EggBagDataComponent(List.of(), 0, Optional.empty());

    public static final Codec<EggBagDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemStack.CODEC.listOf().fieldOf("items").forGetter(EggBagDataComponent::contents),
        Codecs.POSITIVE_INT.fieldOf("capacity").forGetter(EggBagDataComponent::capacity),
        Codec.STRING.optionalFieldOf("tier").forGetter(EggBagDataComponent::tier)
    ).apply(instance, EggBagDataComponent::new));

    public static final PacketCodec<RegistryByteBuf, EggBagDataComponent> PACKET_CODEC = PacketCodec.tuple(
        ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()),
        EggBagDataComponent::contents,
        PacketCodecs.INTEGER,
        EggBagDataComponent::capacity,
        PacketCodecs.optional(PacketCodecs.STRING),
        EggBagDataComponent::tier,
        EggBagDataComponent::new
    );

    public EggBagDataComponent (int capacity, @Nullable String tier) {
        this(List.of(), capacity, Optional.ofNullable(tier));
    }

    public EggBagDataComponent add (ItemStack stack) {
        Builder builder = new Builder(this);
        builder.add(stack);
        return builder.build();
    }

    public EggBagDataComponent addCopyAndEmpty (ItemStack stack) {
        Builder builder = new Builder(this);
        Optional<ItemStack> maybeEgg = EggHelper.tryGetEgg(stack.copy());
        if (maybeEgg.isPresent() && builder.add(maybeEgg.get())) stack.setCount(0);
        return builder.build();
    }

    public EggBagDataComponent addAll (Iterable<ItemStack> stacks) {
        Builder builder = new Builder(this);
        for (ItemStack stack : stacks) {
            builder.add(stack.copyAndEmpty());
        }
        return builder.build();
    }

    public EggBagDataComponent addAllCopiesAndEmpty (Iterable<ItemStack> stacks) {
        Builder builder = new Builder(this);
        for (ItemStack stack : stacks) {
            Optional<ItemStack> maybeEgg = EggHelper.tryGetEgg(stack.copy());
            if (maybeEgg.isPresent() && builder.add(maybeEgg.get())) stack.setCount(0);
        }
        return builder.build();
    }

    public EggBagDataComponent remove (int index) {
        if (this.get(index).isEmpty()) return this;

        Builder builder = new Builder(this);
        builder.remove(index);
        return builder.build();
    }

    public EggBagDataComponent validate () {
        Builder builder = new Builder(this);
        builder.validate();
        return builder.build();
    }

    public Optional<ItemStack> get (int index) {
        if (index < 0 || index >= this.contents.size()) {
            return Optional.empty();
        }
        return Optional.of(this.contents.get(index));
    }

    public boolean isEmpty () {
        return this.contents.isEmpty();
    }

    public boolean isFull () {
        return this.contents.size() >= this.capacity;
    }

    public BundleContentsComponent asBundle () {
        return new BundleContentsComponent(Lists.transform(this.contents, ItemStack::copy));
    }

    public static class Builder {
        private final List<ItemStack> mutableContents;
        private final int capacity;
        private final Optional<String> tier;

        public Builder (EggBagDataComponent component) {
            this.mutableContents = new LinkedList<>();
            this.mutableContents.addAll(component.contents);
            this.capacity = component.capacity;
            this.tier = component.tier;
        }

        public boolean add (ItemStack stack) {
            if (stack != null && stack.isOf(DPItems.POKEMON_EGG) && this.mutableContents.size() < this.capacity) {
                this.mutableContents.addLast(stack);
                return true;
            }
            return false;
        }

        public ItemStack remove () {
            if (this.mutableContents.isEmpty()) return ItemStack.EMPTY;
            return this.mutableContents.removeFirst();
        }

        public ItemStack remove (int index) {
            if (index >= this.mutableContents.size()) return ItemStack.EMPTY;
            return this.mutableContents.remove(index);
        }

        public void validate () {
            this.mutableContents.removeIf(ItemStack::isEmpty);
        }

        public EggBagDataComponent build () {
            if (this.tier.isPresent()) {
                int newCapacity = Options.getBagSettings(this.tier.get()).capacity();
                return new EggBagDataComponent(this.mutableContents.stream().toList(), newCapacity, this.tier);
            }
            return new EggBagDataComponent(this.mutableContents.stream().toList(), this.capacity, this.tier);
        }
    }
}

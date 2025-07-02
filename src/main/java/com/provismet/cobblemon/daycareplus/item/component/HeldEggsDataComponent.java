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

public record HeldEggsDataComponent(List<ItemStack> contents, int capacity, Optional<String> tier) {
    public static final HeldEggsDataComponent DEFAULT = new HeldEggsDataComponent(List.of(), 0, Optional.empty());

    public static final Codec<HeldEggsDataComponent> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        ItemStack.CODEC.listOf().fieldOf("items").forGetter(HeldEggsDataComponent::contents),
        Codecs.POSITIVE_INT.fieldOf("capacity").forGetter(HeldEggsDataComponent::capacity),
        Codec.STRING.optionalFieldOf("tier").forGetter(HeldEggsDataComponent::tier)
    ).apply(instance, HeldEggsDataComponent::new));

    public static final PacketCodec<RegistryByteBuf, HeldEggsDataComponent> PACKET_CODEC = PacketCodec.tuple(
        ItemStack.PACKET_CODEC.collect(PacketCodecs.toList()),
        HeldEggsDataComponent::contents,
        PacketCodecs.INTEGER,
        HeldEggsDataComponent::capacity,
        PacketCodecs.optional(PacketCodecs.STRING),
        HeldEggsDataComponent::tier,
        HeldEggsDataComponent::new
    );

    public HeldEggsDataComponent (int capacity, @Nullable String tier) {
        this(List.of(), capacity, Optional.ofNullable(tier));
    }

    public HeldEggsDataComponent add (ItemStack stack) {
        Builder builder = new Builder(this);
        builder.add(stack);
        return builder.build();
    }

    public HeldEggsDataComponent addCopyAndEmpty (ItemStack stack) {
        Builder builder = new Builder(this);
        Optional<ItemStack> maybeEgg = EggHelper.tryGetEgg(stack.copy());
        if (maybeEgg.isPresent() && builder.add(maybeEgg.get())) stack.setCount(0);
        return builder.build();
    }

    public HeldEggsDataComponent addAll (Iterable<ItemStack> stacks) {
        Builder builder = new Builder(this);
        for (ItemStack stack : stacks) {
            builder.add(stack.copyAndEmpty());
        }
        return builder.build();
    }

    public HeldEggsDataComponent addAllCopiesAndEmpty (Iterable<ItemStack> stacks) {
        Builder builder = new Builder(this);
        for (ItemStack stack : stacks) {
            Optional<ItemStack> maybeEgg = EggHelper.tryGetEgg(stack.copy());
            if (maybeEgg.isPresent() && builder.add(maybeEgg.get())) stack.setCount(0);
        }
        return builder.build();
    }

    public HeldEggsDataComponent remove (int index) {
        if (this.get(index).isEmpty()) return this;

        Builder builder = new Builder(this);
        builder.remove(index);
        return builder.build();
    }

    public HeldEggsDataComponent validate () {
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

        public Builder (HeldEggsDataComponent component) {
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

        public HeldEggsDataComponent build () {
            if (this.tier.isPresent()) {
                int newCapacity = Options.getIncubatorSettings(this.tier.get()).capacity();
                return new HeldEggsDataComponent(this.mutableContents.stream().toList(), newCapacity, this.tier);
            }
            return new HeldEggsDataComponent(this.mutableContents.stream().toList(), this.capacity, this.tier);
        }
    }
}

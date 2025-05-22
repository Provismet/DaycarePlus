package com.provismet.cobblemon.daycareplus.registries;

import com.mojang.serialization.Codec;
import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import eu.pb4.polymer.core.api.other.PolymerComponent;
import net.minecraft.component.ComponentType;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.dynamic.Codecs;

import java.util.function.UnaryOperator;

public abstract class DPItemDataComponents {
    public static final ComponentType<String> POKEMON_PROPERTIES = register("pokemon_properties", builder -> builder.codec(Codec.STRING).packetCodec(PacketCodecs.STRING));
    public static final ComponentType<Integer> EGG_STEPS = register("egg_steps", builder -> builder.codec(Codecs.NONNEGATIVE_INT).packetCodec(PacketCodecs.INTEGER));

    private static <T> ComponentType<T> register (String name, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        ComponentType<T> component = Registry.register(Registries.DATA_COMPONENT_TYPE, DaycarePlusServer.identifier(name), builderOperator.apply(ComponentType.builder()).build());
        PolymerComponent.registerDataComponent(component);
        return component;
    }

    public static void init () {}
}

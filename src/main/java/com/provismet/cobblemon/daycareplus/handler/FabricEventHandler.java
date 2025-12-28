package com.provismet.cobblemon.daycareplus.handler;

import com.provismet.cobblemon.daycareplus.storage.IncubatorCollection;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

import java.util.concurrent.CompletableFuture;

public interface FabricEventHandler {
    static void register () {
        ServerPlayConnectionEvents.JOIN.register(FabricEventHandler::onPlayerJoin);
        ServerPlayConnectionEvents.DISCONNECT.register(FabricEventHandler::onPlayerDisconnect);
        ServerLifecycleEvents.BEFORE_SAVE.register(FabricEventHandler::beforeSave);
    }

    private static void onPlayerJoin (ServerPlayNetworkHandler handler, PacketSender packetSender, MinecraftServer server) {
        CompletableFuture.runAsync(() -> IncubatorCollection.loadFromJson(handler.getPlayer()));
    }

    private static void onPlayerDisconnect (ServerPlayNetworkHandler handler, MinecraftServer server) {
        String playerUUID = handler.getPlayer().getUuidAsString();
        CompletableFuture.runAsync(() -> IncubatorCollection.remove(server, playerUUID));
    }

    private static void beforeSave (MinecraftServer server, boolean flush, boolean force) {
        CompletableFuture.runAsync(() -> IncubatorCollection.saveAll(server));
    }
}

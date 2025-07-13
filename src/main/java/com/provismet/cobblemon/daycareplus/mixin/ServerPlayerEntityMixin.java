package com.provismet.cobblemon.daycareplus.mixin;

import com.mojang.authlib.GameProfile;
import com.provismet.cobblemon.daycareplus.storage.IncubatorCollection;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.CompletableFuture;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin (World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("HEAD"))
    private void readIncubators (NbtCompound nbt, CallbackInfo ci) {
        CompletableFuture.runAsync(() -> IncubatorCollection.loadFromJson(this));
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("HEAD"))
    private void saveIncubators (NbtCompound nbt, CallbackInfo ci) {
        CompletableFuture.runAsync(() -> IncubatorCollection.getOrCreate(this).saveToJson(this));
    }
}

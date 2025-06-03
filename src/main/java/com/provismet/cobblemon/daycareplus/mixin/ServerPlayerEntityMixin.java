package com.provismet.cobblemon.daycareplus.mixin;

import ca.landonjw.gooeylibs2.api.container.GooeyContainer;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.cobblemon.mod.common.util.PlayerExtensionsKt;
import com.mojang.authlib.GameProfile;
import com.provismet.cobblemon.daycareplus.gui.EggBagGUI;
import com.provismet.cobblemon.daycareplus.item.EggBagItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin (World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Unique private static final Set<String> HATCH_ABILITIES = Set.of("flamebody", "steamengine", "magmaarmor");

    // Tick bags here instead of in the item itself, this enforces only one bag ticks at a time.
    @Inject(method = "tick", at = @At("HEAD"))
    private void tickBags (CallbackInfo info) {
        if (this.age % 20 != 0) return;

        for (int i = 0; i < this.getInventory().size(); ++i) {
            ItemStack stack = this.getInventory().getStack(i);
            if (stack.getItem() instanceof EggBagItem bag) {
                int abilityMultiplier = 1;

                for (Pokemon pokemon : PlayerExtensionsKt.party((ServerPlayerEntity)(Object)this)) {
                    if (HATCH_ABILITIES.contains(pokemon.getAbility().getName().toLowerCase(Locale.ROOT))) {
                        abilityMultiplier = 2;
                        break;
                    }
                }

                bag.tickEggs(stack, (ServerPlayerEntity)(Object)this, 20 * abilityMultiplier);
                break;
            }
        }

        if (this.currentScreenHandler instanceof GooeyContainer gooeyContainer && gooeyContainer.getPage() instanceof EggBagGUI gui) {
            gui.reset();
        }
    }
}

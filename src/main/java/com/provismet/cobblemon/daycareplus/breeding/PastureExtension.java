package com.provismet.cobblemon.daycareplus.breeding;

import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.provismet.cobblemon.daycareplus.config.Options;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.UUID;

public class PastureExtension {
    private final UUID uuid;
    private final PokemonPastureBlockEntity blockEntity;
    private long prevTime;

    public PastureExtension (PokemonPastureBlockEntity blockEntity, long prevTime, UUID uuid) {
        this.blockEntity = blockEntity;
        this.prevTime = prevTime;
        this.uuid = uuid;
    }

    public long getPrevTime () {
        return this.prevTime;
    }

    public Optional<PotentialPokemonProperties> predictEgg () {
        if (this.blockEntity.getTetheredPokemon().size() != 2) return Optional.empty();
        Pokemon parent1 = this.blockEntity.getTetheredPokemon().getFirst().getPokemon();
        Pokemon parent2 = this.blockEntity.getTetheredPokemon().getLast().getPokemon();

        return BreedingUtils.getOffspring(parent1, parent2);
    }

    public void tick () {
        if (this.blockEntity.getWorld() instanceof ServerWorld world) {
            if (world.getTime() % 20 == 0) {
                world.spawnParticles(
                    ParticleTypes.HEART,
                    this.blockEntity.getPos().getX() + 0.5,
                    this.blockEntity.getPos().getY() + 1.5,
                    this.blockEntity.getPos().getZ() + 0.5,
                    1,
                    0, 0, 0,
                    0
                );
            }

            long ticksToProcess = Math.max(0, world.getTime() - prevTime);
            this.prevTime = world.getTime();
            long eggAttempts = ticksToProcess / Options.getTicksPerEggAttempt();

            if (world.getTime() % Options.getTicksPerEggAttempt() == 0) ++eggAttempts;

            int successfulEggs = 0;
            PlayerEntity owner = null;
            if (this.blockEntity.getOwnerId() != null) {
                owner = this.blockEntity.getWorld().getPlayerByUuid(this.blockEntity.getOwnerId());
            }

            for (int i = 0; i < eggAttempts; ++i) {
                if (world.getRandom().nextDouble() > Options.getSuccessRatePerEggAttempt()) continue;

                Optional<PotentialPokemonProperties> optionalEgg = this.predictEgg();
                if (optionalEgg.isPresent()) {
                    PotentialPokemonProperties potentialEgg = optionalEgg.get();
                    if (owner != null) {
                        if (eggAttempts == 1) owner.sendMessage(Text.translatable("message.chat.daycareplus.egg_produced"));
                        else ++successfulEggs;
                    }

                    ItemStack egg = DPItems.POKEMON_EGG.createEggItem(potentialEgg.createPokemonProperties());
                    ((PastureContainer)(Object)this.blockEntity).add(egg);
                }
            }

            if (successfulEggs > 0) {
                owner.sendMessage(Text.translatable("message.chat.daycareplus.multiple_egg_produced", successfulEggs));
            }
        }
    }
}

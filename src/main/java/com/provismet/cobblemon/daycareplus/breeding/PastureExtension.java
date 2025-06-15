package com.provismet.cobblemon.daycareplus.breeding;

import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.api.events.CobblemonEvents;
import com.cobblemon.mod.common.api.events.pokemon.CollectEggEvent;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.cobblemon.daycareplus.api.DaycarePlusEvents;
import com.provismet.cobblemon.daycareplus.config.Options;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

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

    private void tryApplyMirrorHerb (Pokemon potentialHolder, Pokemon other) {
        if (!potentialHolder.heldItem().isOf(CobblemonItems.MIRROR_HERB)) return;
        DaycarePlusServer.LOGGER.info("Attempting mirror herb.");

        PlayerEntity owner = null;
        if (this.blockEntity.getOwnerId() != null && this.blockEntity.getWorld() != null) {
            owner = this.blockEntity.getWorld().getPlayerByUuid(this.blockEntity.getOwnerId());
        }

        for (Move move : other.getMoveSet()) {
            DaycarePlusServer.LOGGER.info("Testing {} from {}", move.getName(), other.getDisplayName().getString());
            if (potentialHolder.getForm().getMoves().getEggMoves().stream().anyMatch(moveTemplate -> moveTemplate.getName().equalsIgnoreCase(move.getName()))) {
                // Avoid relearning moves you already have.
                if (potentialHolder.getMoveSet().getMoves().stream().map(Move::getTemplate).anyMatch(moveTemplate -> moveTemplate.getName().equalsIgnoreCase(move.getName()))) {
                    DaycarePlusServer.LOGGER.info("{} is already in the moveset of {}", move.getName(), potentialHolder.getDisplayName().getString());
                    continue;
                }
                boolean alreadyLearnt = false;
                for (BenchedMove benchedMove : potentialHolder.getBenchedMoves()) {
                    if (benchedMove.getMoveTemplate().getName().equalsIgnoreCase(move.getName())) {
                        alreadyLearnt = true;
                        DaycarePlusServer.LOGGER.info("{} is already in the benched moves of {}", move.getName(), potentialHolder.getDisplayName().getString());
                        break;
                    }
                }
                if (alreadyLearnt) continue;

                if (potentialHolder.getMoveSet().add(move.getTemplate().create()) && owner != null) {
                    owner.sendMessage(Text.translatable("message.chat.daycareplus.move_learnt", potentialHolder.getDisplayName(), move.getDisplayName()));
                }
            }
        }
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

            if ((world.getTime() + this.uuid.getLeastSignificantBits()) % Options.getTicksPerEggAttempt() == 0) ++eggAttempts;

            int calculatedEggs = 0;
            PlayerEntity owner = null;
            boolean applyMirrorHerb = false;
            if (this.blockEntity.getOwnerId() != null) {
                owner = this.blockEntity.getWorld().getPlayerByUuid(this.blockEntity.getOwnerId());
            }

            for (int i = 0; i < eggAttempts; ++i) {
                if (world.getRandom().nextDouble() > Options.getSuccessRatePerEggAttempt()) continue;
                applyMirrorHerb = true;

                Optional<PotentialPokemonProperties> optionalEgg = this.predictEgg();
                if (optionalEgg.isPresent()) {
                    PotentialPokemonProperties potentialEgg = optionalEgg.get();
                    if (owner != null) {
                        if (eggAttempts == 1) owner.sendMessage(Text.translatable("message.chat.daycareplus.egg_produced"));
                        else ++calculatedEggs;
                    }

                    PokemonProperties properties = potentialEgg.createPokemonProperties();
                    if (owner instanceof ServerPlayerEntity serverPlayer) {
                        CobblemonEvents.COLLECT_EGG.emit(new CollectEggEvent(properties, potentialEgg.getPrimary(), potentialEgg.getSecondary(), serverPlayer));
                    }
                    DaycarePlusEvents.PRE_EGG_PRODUCED.invoker().beforeItemCreated(properties);

                    ItemStack egg = DPItems.POKEMON_EGG.createEggItem(properties);
                    DaycarePlusEvents.POST_EGG_PRODUCED.invoker().afterItemCreated(egg);

                    ((PastureContainer)(Object)this.blockEntity).add(egg);
                }
            }

            if (applyMirrorHerb) {
                Pokemon parent1 = this.blockEntity.getTetheredPokemon().getFirst().getPokemon();
                Pokemon parent2 = this.blockEntity.getTetheredPokemon().getLast().getPokemon();

                if (parent1 != null && parent2 != null) {
                    this.tryApplyMirrorHerb(parent1, parent2);
                    this.tryApplyMirrorHerb(parent2, parent1);
                }
            }

            calculatedEggs = MathHelper.clamp(calculatedEggs, 0, Options.getPastureInventorySize());
            if (calculatedEggs > 0 && owner != null) {
                if (calculatedEggs == 1) owner.sendMessage(Text.translatable("message.chat.daycareplus.single_egg_produced", calculatedEggs));
                else owner.sendMessage(Text.translatable("message.chat.daycareplus.multiple_egg_produced", calculatedEggs));
            }
        }
    }
}

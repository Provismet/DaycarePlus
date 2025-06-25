package com.provismet.cobblemon.daycareplus.command;

import com.cobblemon.mod.common.api.pokemon.PokemonProperties;
import com.cobblemon.mod.common.command.argument.PokemonPropertiesArgumentType;
import com.provismet.cobblemon.daycareplus.api.DaycarePlusEvents;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class GiveEggCommand {
    public static void register () {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> commandDispatcher.register(
            CommandManager.literal("daycareplus")
                .then(CommandManager.literal("give")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .then(CommandManager.argument("properties", PokemonPropertiesArgumentType.Companion.properties())
                            .executes(context -> {
                                ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                PokemonProperties properties = PokemonPropertiesArgumentType.Companion.getPokemonProperties(context, "properties");
                                ItemStack egg = DPItems.POKEMON_EGG.createEggItem(properties);
                                DaycarePlusEvents.POST_EGG_PRODUCED.invoker().afterItemCreated(egg); // Skip the pre event for the command. Don't let the property get edited.
                                player.giveItemStack(egg);
                                context.getSource().sendFeedback(() -> Text.of("Gave 1 Pokemon Egg to " + player.getName().getString()), false);
                                return 1;
                            })
                        )
                    )
                )
        ));
    }
}

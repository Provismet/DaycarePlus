package com.provismet.cobblemon.daycareplus.command;

import com.provismet.cobblemon.daycareplus.breeding.BreedingLink;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ResetDaycaresCommand {
    public static void register () {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandRegistryAccess, registrationEnvironment) -> commandDispatcher.register(
            CommandManager.literal("daycareplus")
                .then(CommandManager.literal("clear")
                    .requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
                    .then(CommandManager.argument("player", EntityArgumentType.player())
                        .executes(context -> {
                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                            BreedingLink.remove(player);
                            context.getSource().sendFeedback(() -> Text.of("Cleared daycare memory of " + player.getName().getString()), false);
                            return 1;
                        })
                    )
                )
        ));
    }
}

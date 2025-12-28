package com.provismet.cobblemon.daycareplus;

import com.provismet.cobblemon.daycareplus.api.DaycarePlusEvents;
import com.provismet.cobblemon.daycareplus.api.DaycarePlusInitializer;
import com.provismet.cobblemon.daycareplus.breeding.BreedingUtils;
import com.provismet.cobblemon.daycareplus.command.DPCommands;
import com.provismet.cobblemon.daycareplus.compatibility.CobblemonSizeVariationCompatibility;
import com.provismet.cobblemon.daycareplus.config.DaycarePlusOptions;
import com.provismet.cobblemon.daycareplus.config.IncubatorTiers;
import com.provismet.cobblemon.daycareplus.handler.CobblemonEventHandler;
import com.provismet.cobblemon.daycareplus.handler.FabricEventHandler;
import com.provismet.cobblemon.daycareplus.networking.callback.PacketCallbacksC2S;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.registries.DPItemGroups;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import com.provismet.cobblemon.daycareplus.registries.DPPayloads;
import com.provismet.cobblemon.daycareplus.registries.DPStats;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaycarePlusMain implements ModInitializer {
	public static final String MODID = "daycareplus";
	public static final Logger LOGGER = LoggerFactory.getLogger("Daycare+");

	public static Identifier identifier (String path) {
		return Identifier.of(MODID, path);
	}

	@Override
	public void onInitialize () {
		IncubatorTiers.load();

		PolymerResourcePackUtils.addModAssets(MODID);

		DPItems.init();
		DPItemDataComponents.init();
		DPItemGroups.register();
		DPStats.init();
		DPCommands.register();
        DPPayloads.register();
        PacketCallbacksC2S.register();

		CobblemonEventHandler.register();
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new BreedingUtils());
        FabricEventHandler.register();

		FabricLoader.getInstance().getEntrypointContainers("daycareplus", DaycarePlusInitializer.class).forEach(
			initializer -> {
				try {
					initializer.getEntrypoint().onInitialize();
				}
				catch (Throwable e) {
					DaycarePlusMain.LOGGER.error("Daycare+ failed to initialise sidemod entrypoint from {} due to errors provided by it:", initializer.getProvider().getMetadata().getName(), e);
				}
			}
		);

        if (DaycarePlusOptions.doCobblemonSizeVariationCompatibility() && FabricLoader.getInstance().isModLoaded("cobblemonsizevariations"))
            DaycarePlusEvents.EGG_PROPERTIES_CREATED.register(new CobblemonSizeVariationCompatibility());
	}
}
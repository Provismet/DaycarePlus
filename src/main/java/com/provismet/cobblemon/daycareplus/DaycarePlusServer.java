package com.provismet.cobblemon.daycareplus;

import com.provismet.cobblemon.daycareplus.breeding.BreedingUtils;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.registries.DPItemGroups;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.DedicatedServerModInitializer;

import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaycarePlusServer implements DedicatedServerModInitializer {
	public static final String MODID = "daycareplus";
	public static final Logger LOGGER = LoggerFactory.getLogger("Daycare+");

	public static Identifier identifier (String path) {
		return Identifier.of(MODID, path);
	}

	@Override
	public void onInitializeServer () {
		PolymerResourcePackUtils.markAsRequired();
		PolymerResourcePackUtils.addModAssets(MODID);

		DPItems.init();
		DPItemDataComponents.init();
		DPItemGroups.register();

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new BreedingUtils());
	}
}
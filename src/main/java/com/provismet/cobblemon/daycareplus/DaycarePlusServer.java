package com.provismet.cobblemon.daycareplus;

import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.DedicatedServerModInitializer;

import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaycarePlusServer implements DedicatedServerModInitializer {
	public static final String MODID = "daycareplus";
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static Identifier identifier (String path) {
		return Identifier.of(MODID, path);
	}

	@Override
	public void onInitializeServer () {
		PolymerResourcePackUtils.markAsRequired();
		PolymerResourcePackUtils.addModAssets(MODID);

		DPItems.init();
		DPItemDataComponents.init();
	}
}
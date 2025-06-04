package com.provismet.cobblemon.daycareplus;

import com.provismet.cobblemon.daycareplus.config.Options;
import com.provismet.cobblemon.daycareplus.registries.DPItemDataComponents;
import com.provismet.cobblemon.daycareplus.registries.DPItemGroups;
import com.provismet.cobblemon.daycareplus.registries.DPItems;
import com.provismet.cobblemon.daycareplus.registries.DPStats;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class DaycarePlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient () {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            Options.load();
            DPItems.init();
            DPItemDataComponents.init();
            DPItemGroups.register();
            DPStats.init();
        }
    }
}

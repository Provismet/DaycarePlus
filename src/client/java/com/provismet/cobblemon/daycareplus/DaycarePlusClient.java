package com.provismet.cobblemon.daycareplus;

import com.provismet.cobblemon.daycareplus.config.ClientOptions;
import com.provismet.cobblemon.daycareplus.networking.callback.PacketCallbacksS2C;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.ClientModInitializer;

public class DaycarePlusClient implements ClientModInitializer {
    @Override
    public void onInitializeClient () {
        ClientOptions.load();
        PacketCallbacksS2C.register();
        PolymerResourcePackUtils.markAsRequired();
    }
}

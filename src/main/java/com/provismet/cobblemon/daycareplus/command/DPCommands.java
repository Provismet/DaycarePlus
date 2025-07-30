package com.provismet.cobblemon.daycareplus.command;

public class DPCommands {
    public static void register () {
        GiveEggCommand.register();
        ResetDaycaresCommand.register();
        IncubatorCommand.register();
    }
}

package com.provismet.cobblemon.daycareplus.config;

public class Options {
    // Egg Production
    private static long ticksPerEggAttempt = 400;
    private static double successRatePerEggAttempt = 0.75;
    private static int pastureInventorySize = 32;

    // Egg Hatching
    private static double pointsPerEggCycle = 1;

    // Shiny Chance
    private static float shinyChanceMultiplier = 1;
    private static float masudaMultiplier = 2;
    private static float crystalMultiplier = 1;

    public static long getTicksPerEggAttempt () {
        return ticksPerEggAttempt;
    }

    public static double getSuccessRatePerEggAttempt () {
        return successRatePerEggAttempt;
    }

    public static int getPastureInventorySize () {
        return pastureInventorySize;
    }

    public static int getEggPoints (int eggCycles) {
        return (int)(pointsPerEggCycle * eggCycles);
    }

    public static float getShinyChanceMultiplier () {
        return shinyChanceMultiplier;
    }

    public static float getMasudaMultiplier () {
        return masudaMultiplier;
    }

    public static float getCrystalMultiplier () {
        return crystalMultiplier;
    }
}

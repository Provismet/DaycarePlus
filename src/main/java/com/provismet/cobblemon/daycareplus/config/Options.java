package com.provismet.cobblemon.daycareplus.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.provismet.cobblemon.daycareplus.DaycarePlusServer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Options {
    private static final String FILE = "./config/daycareplus.json";

    // Egg Production
    private static long ticksPerEggAttempt = 12000;
    private static double successRatePerEggAttempt = 0.75;
    private static int pastureInventorySize = 128;
    private static int maxPasturesPerPlayer = 3;

    // Egg Hatching
    private static double pointsPerEggCycle = 200;

    // Shiny Chance
    private static float shinyChanceMultiplier = 1;
    private static float masudaMultiplier = 2;
    private static float crystalMultiplier = 1;

    // Egg Moves
    private static boolean inheritEggMovesFromBothParents = true; // This is true in gen6+

    // Egg Bags
    private static EggBagSettings leather = new EggBagSettings(8, 1);
    private static EggBagSettings iron = new EggBagSettings(64, 2);
    private static EggBagSettings gold = new EggBagSettings(32, 8);
    private static EggBagSettings diamond = new EggBagSettings(96, 4);
    private static EggBagSettings netherite = new EggBagSettings(128, 8);

    public static long getTicksPerEggAttempt () {
        return ticksPerEggAttempt;
    }

    public static double getSuccessRatePerEggAttempt () {
        return successRatePerEggAttempt;
    }

    public static int getPastureInventorySize () {
        return pastureInventorySize;
    }

    public static int getMaxPasturesPerPlayer () {
        return maxPasturesPerPlayer;
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

    public static boolean doGen6EggMoves () {
        return inheritEggMovesFromBothParents;
    }

    public static EggBagSettings getLeather () {
        return leather;
    }

    public static EggBagSettings getIron () {
        return iron;
    }

    public static EggBagSettings getGold () {
        return gold;
    }

    public static EggBagSettings getDiamond () {
        return diamond;
    }

    public static EggBagSettings getNetherite () {
        return netherite;
    }

    public static void save () {
        JsonObject json = new JsonObject();

        JsonObject eggProduction = new JsonObject();
        eggProduction.addProperty("ticksPerEggAttempt", ticksPerEggAttempt);
        eggProduction.addProperty("successRatePerEggAttempt", successRatePerEggAttempt);
        eggProduction.addProperty("pastureInventorySize", pastureInventorySize);
        eggProduction.addProperty("maxPasturesPerPlayer", maxPasturesPerPlayer);
        json.add("eggProduction", eggProduction);

        JsonObject shinyChance = new JsonObject();
        shinyChance.addProperty("standardMultiplier", shinyChanceMultiplier);
        shinyChance.addProperty("masudaMultiplier", masudaMultiplier);
        shinyChance.addProperty("crystalMultiplier", crystalMultiplier);
        json.add("shinyChance", shinyChance);

        JsonObject breedingRules = new JsonObject();
        breedingRules.addProperty("inheritMovesFromBothParents", inheritEggMovesFromBothParents);
        breedingRules.addProperty("ticksPerEggCycle", pointsPerEggCycle);
        json.add("breedingRules", breedingRules);

        JsonObject eggBags = new JsonObject();
        eggBags.add("leather", leather.toJson());
        eggBags.add("iron", iron.toJson());
        eggBags.add("gold", gold.toJson());
        eggBags.add("diamond", diamond.toJson());
        eggBags.add("netherite", netherite.toJson());
        json.add("eggBags", eggBags);

        try (FileWriter writer = new FileWriter(FILE)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(json));
        }
        catch (IOException e) {
            DaycarePlusServer.LOGGER.error("Error whilst saving config: ", e);
        }
    }

    public static void load () {
        File file = new File(FILE);
        File folder = file.getParentFile();
        if (!folder.exists()) folder.mkdirs();

        if (!file.exists()) {
            save();
        }

        try {
            JsonElement json = JsonParser.parseReader(new FileReader(file));
            if (json instanceof JsonObject config) {
                if (config.get("eggProduction") instanceof JsonObject eggProduction) {
                    ticksPerEggAttempt = eggProduction.getAsJsonPrimitive("ticksPerEggAttempt").getAsInt();
                    successRatePerEggAttempt = eggProduction.getAsJsonPrimitive("successRatePerEggAttempt").getAsDouble();
                    pastureInventorySize = eggProduction.getAsJsonPrimitive("pastureInventorySize").getAsInt();
                    maxPasturesPerPlayer = eggProduction.getAsJsonPrimitive("maxPasturesPerPlayer").getAsInt();
                }
                if (config.get("shinyChance") instanceof JsonObject shinyChance) {
                    shinyChanceMultiplier = shinyChance.getAsJsonPrimitive("standardMultiplier").getAsFloat();
                    masudaMultiplier = shinyChance.getAsJsonPrimitive("masudaMultiplier").getAsFloat();
                    crystalMultiplier = shinyChance.getAsJsonPrimitive("crystalMultiplier").getAsFloat();
                }
                if (config.get("breedingRules") instanceof JsonObject breedingRules) {
                    inheritEggMovesFromBothParents = breedingRules.getAsJsonPrimitive("inheritMovesFromBothParents").getAsBoolean();
                    pointsPerEggCycle = breedingRules.getAsJsonPrimitive("ticksPerEggCycle").getAsInt();
                }
                if (config.get("eggBags") instanceof JsonObject eggBags) {
                    leather = EggBagSettings.fromJson(eggBags.getAsJsonObject("leather"));
                    iron = EggBagSettings.fromJson(eggBags.getAsJsonObject("iron"));
                    gold = EggBagSettings.fromJson(eggBags.getAsJsonObject("gold"));
                    diamond = EggBagSettings.fromJson(eggBags.getAsJsonObject("diamond"));
                    netherite = EggBagSettings.fromJson(eggBags.getAsJsonObject("netherite"));
                }
            }
        }
        catch (FileNotFoundException e) {
            DaycarePlusServer.LOGGER.info("No config found, creating default.");
        }
        catch (Exception e) {
            DaycarePlusServer.LOGGER.error("Error reading Daycare+ config: ", e);
        }
        save();
    }

    public record EggBagSettings (int capacity, int eggsToTick) {
        public static EggBagSettings fromJson (JsonObject json) {
            int capacity = 1;
            int eggs = 1;
            if (json.has("capacity")) {
                capacity = json.getAsJsonPrimitive("capacity").getAsInt();
            }
            if (json.has("eggsToTickSimultaneously")) {
                eggs = json.getAsJsonPrimitive("eggsToTickSimultaneously").getAsInt();
            }
            return new EggBagSettings(capacity, eggs);
        }

        public JsonObject toJson () {
            JsonObject json = new JsonObject();
            json.addProperty("capacity", this.capacity);
            json.addProperty("eggsToTickSimultaneously", this.eggsToTick);
            return json;
        }
    }
}

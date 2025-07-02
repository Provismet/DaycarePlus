package com.provismet.cobblemon.daycareplus.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.lilylib.util.json.JsonBuilder;
import com.provismet.lilylib.util.json.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Options {
    private static final String FILE = "./config/daycareplus.json";

    // Egg Production
    private static long ticksPerEggAttempt = 12000;
    private static double successRatePerEggAttempt = 0.75;
    private static int pastureInventorySize = 128;
    private static int maxPasturesPerPlayer = 3;
    private static boolean showShinyChance = true;

    // Competitive Breeding
    private static boolean competitiveBreeding = false;
    private static int maxFertility = 8;
    private static boolean allowBreedingWithoutFertility = false;
    private static boolean consumeHeldItems = true;

    // Egg Hatching
    private static int pointsPerEggCycle = 200;
    private static boolean showEggTooltip = true;

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

    public static boolean shouldShowShinyChance () {
        return showShinyChance;
    }

    public static boolean doCompetitiveBreeding () {
        return competitiveBreeding;
    }

    public static int getMaxFertility () {
        return maxFertility;
    }

    public static boolean shouldAllowBreedingWithoutFertility () {
        return allowBreedingWithoutFertility;
    }

    public static boolean shouldConsumeHeldItems () {
        return consumeHeldItems;
    }

    public static int getEggPoints (int eggCycles) {
        return pointsPerEggCycle * eggCycles;
    }

    public static boolean shouldShowEggTooltip () {
        return showEggTooltip;
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

    public static EggBagSettings getBagSettings (String tier) {
        return switch (tier) {
            case "iron" -> iron;
            case "gold" -> gold;
            case "diamond" -> diamond;
            case "netherite" -> netherite;
            default -> leather;
        };
    }

    public static void save () {
        JsonBuilder builder = new JsonBuilder()
            .append(
                "eggProduction", new JsonBuilder()
                    .append("ticksPerEggAttempt", ticksPerEggAttempt)
                    .append("successRatePerEggAttempt", successRatePerEggAttempt)
                    .append("pastureInventorySize", pastureInventorySize)
                    .append("maxPasturesPerPlayer", maxPasturesPerPlayer)
                    .append("showShinyChance", showShinyChance))
            .append(
                "competitiveMode", new JsonBuilder()
                    .append("useCompetitiveMode", competitiveBreeding)
                    .append("maxFertility", maxFertility)
                    .append("allowBreedingWithoutFertility", allowBreedingWithoutFertility)
                    .append("consumeHeldItems", consumeHeldItems))
            .append(
                "shinyChance", new JsonBuilder()
                    .append("standardMultiplier", shinyChanceMultiplier)
                    .append("masudaMultiplier", masudaMultiplier)
                    .append("crystalMultiplier", crystalMultiplier))
            .append(
                "breedingRules", new JsonBuilder()
                    .append("inheritMovesFromBothParents", inheritEggMovesFromBothParents)
                    .append("ticksPerEggCycle", pointsPerEggCycle)
                    .append("showEggTooltip", showEggTooltip))
            .append(
                "eggBags", new JsonBuilder()
                    .append("leather", leather.toJson())
                    .append("iron", iron.toJson())
                    .append("gold", gold.toJson())
                    .append("diamond", diamond.toJson())
                    .append("netherite", netherite.toJson()));

        try (FileWriter writer = new FileWriter(FILE)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(builder.getJson()));
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
            JsonReader reader = JsonReader.file(file);
            if (reader != null) {
                reader.getObjectAsReader("eggProduction").ifPresent(eggProduction -> {
                    eggProduction.getInteger("ticksPerEggAttempt").ifPresent(val -> ticksPerEggAttempt = val);
                    eggProduction.getDouble("successRatePerEggAttempt").ifPresent(val -> successRatePerEggAttempt = val);
                    eggProduction.getInteger("pastureInventorySize").ifPresent(val -> pastureInventorySize = val);
                    eggProduction.getInteger("maxPasturesPerPlayer").ifPresent(val -> maxPasturesPerPlayer = val);
                    eggProduction.getBoolean("showShinyChance").ifPresent(val -> showShinyChance = val);
                });

                reader.getObjectAsReader("competitiveMode").ifPresent(competitiveMode -> {
                    competitiveMode.getBoolean("useCompetitiveMode").ifPresent(val -> competitiveBreeding = val);
                    competitiveMode.getInteger("maxFertility").ifPresent(val -> maxFertility = val);
                    competitiveMode.getBoolean("allowBreedingWithoutFertility").ifPresent(val -> allowBreedingWithoutFertility = val);
                    competitiveMode.getBoolean("consumeHeldItems").ifPresent(val -> consumeHeldItems = val);
                });

                reader.getObjectAsReader("shinyChance").ifPresent(shinyChance -> {
                    shinyChance.getFloat("standardMultiplier").ifPresent(val -> shinyChanceMultiplier = val);
                    shinyChance.getFloat("masudaMultiplier").ifPresent(val -> masudaMultiplier = val);
                    shinyChance.getFloat("crystalMultiplier").ifPresent(val -> crystalMultiplier = val);
                });

                reader.getObjectAsReader("breedingRules").ifPresent(breedingRules -> {
                    breedingRules.getBoolean("inheritMovesFromBothParents").ifPresent(val -> inheritEggMovesFromBothParents = val);
                    breedingRules.getInteger("ticksPerEggCycle").ifPresent(val -> pointsPerEggCycle = val);
                    breedingRules.getBoolean("showEggTooltip").ifPresent(val -> showEggTooltip = val);
                });

                reader.getObjectAsReader("eggBags").ifPresent(eggBags -> {
                    eggBags.getObject("leather").ifPresent(val -> leather = EggBagSettings.fromJson(val));
                    eggBags.getObject("iron").ifPresent(val -> iron = EggBagSettings.fromJson(val));
                    eggBags.getObject("gold").ifPresent(val -> gold = EggBagSettings.fromJson(val));
                    eggBags.getObject("diamond").ifPresent(val -> diamond = EggBagSettings.fromJson(val));
                    eggBags.getObject("netherite").ifPresent(val -> netherite = EggBagSettings.fromJson(val));
                });
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

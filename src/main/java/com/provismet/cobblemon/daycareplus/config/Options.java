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
    private static boolean useShinyEvent = true;

    // Egg Moves
    private static boolean inheritEggMovesFromBothParents = true; // This is true in gen6+

    // Incubators
    private static IncubatorSettings copper = new IncubatorSettings(8, 1);
    private static IncubatorSettings iron = new IncubatorSettings(64, 2);
    private static IncubatorSettings gold = new IncubatorSettings(32, 32);
    private static IncubatorSettings diamond = new IncubatorSettings(96, 4);
    private static IncubatorSettings netherite = new IncubatorSettings(128, 8);

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

    public static boolean shouldUseShinyChanceEvent () {
        return useShinyEvent;
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

    public static IncubatorSettings getCopper () {
        return copper;
    }

    public static IncubatorSettings getIron () {
        return iron;
    }

    public static IncubatorSettings getGold () {
        return gold;
    }

    public static IncubatorSettings getDiamond () {
        return diamond;
    }

    public static IncubatorSettings getNetherite () {
        return netherite;
    }

    public static IncubatorSettings getIncubatorSettings (String tier) {
        return switch (tier) {
            case "iron" -> iron;
            case "gold" -> gold;
            case "diamond" -> diamond;
            case "netherite" -> netherite;
            default -> copper;
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
                    .append("useEventTrigger", useShinyEvent)
                    .append("standardMultiplier", shinyChanceMultiplier)
                    .append("masudaMultiplier", masudaMultiplier)
                    .append("crystalMultiplier", crystalMultiplier))
            .append(
                "breedingRules", new JsonBuilder()
                    .append("inheritMovesFromBothParents", inheritEggMovesFromBothParents)
                    .append("ticksPerEggCycle", pointsPerEggCycle)
                    .append("showEggTooltip", showEggTooltip))
            .append(
                "incubators", new JsonBuilder()
                    .append("copper", copper.toJson())
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
                    shinyChance.getBoolean("useEventTrigger").ifPresent(val -> useShinyEvent = val);;
                    shinyChance.getFloat("standardMultiplier").ifPresent(val -> shinyChanceMultiplier = val);
                    shinyChance.getFloat("masudaMultiplier").ifPresent(val -> masudaMultiplier = val);
                    shinyChance.getFloat("crystalMultiplier").ifPresent(val -> crystalMultiplier = val);
                });

                reader.getObjectAsReader("breedingRules").ifPresent(breedingRules -> {
                    breedingRules.getBoolean("inheritMovesFromBothParents").ifPresent(val -> inheritEggMovesFromBothParents = val);
                    breedingRules.getInteger("ticksPerEggCycle").ifPresent(val -> pointsPerEggCycle = val);
                    breedingRules.getBoolean("showEggTooltip").ifPresent(val -> showEggTooltip = val);
                });

                reader.getObjectAsReader("incubators").ifPresent(incubators -> {
                    incubators.getObject("copper").ifPresent(val -> copper = IncubatorSettings.fromJson(val));
                    incubators.getObject("iron").ifPresent(val -> iron = IncubatorSettings.fromJson(val));
                    incubators.getObject("gold").ifPresent(val -> gold = IncubatorSettings.fromJson(val));
                    incubators.getObject("diamond").ifPresent(val -> diamond = IncubatorSettings.fromJson(val));
                    incubators.getObject("netherite").ifPresent(val -> netherite = IncubatorSettings.fromJson(val));
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

    public record IncubatorSettings(int capacity, int eggsToTick) {
        public static IncubatorSettings fromJson (JsonObject json) {
            int capacity = 1;
            int eggs = 1;
            if (json.has("capacity")) {
                capacity = json.getAsJsonPrimitive("capacity").getAsInt();
            }
            if (json.has("eggsToTickSimultaneously")) {
                eggs = json.getAsJsonPrimitive("eggsToTickSimultaneously").getAsInt();
            }
            return new IncubatorSettings(capacity, eggs);
        }

        public JsonObject toJson () {
            JsonObject json = new JsonObject();
            json.addProperty("capacity", this.capacity);
            json.addProperty("eggsToTickSimultaneously", this.eggsToTick);
            return json;
        }
    }
}

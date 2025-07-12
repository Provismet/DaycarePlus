package com.provismet.cobblemon.daycareplus.config;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import com.provismet.lilylib.util.json.JsonBuilder;
import com.provismet.lilylib.util.json.JsonReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class IncubatorTiers {
    private static final String FILE = "./config/daycareplus/incubators.json";
    private static final Map<String, IncubatorSettings> settings = new HashMap<>();

    // Incubator defaults
    private static final IncubatorSettings COPPER = new IncubatorSettings(8, 1);
    private static final IncubatorSettings IRON = new IncubatorSettings(64, 2);
    private static final IncubatorSettings GOLD = new IncubatorSettings(32, 32);
    private static final IncubatorSettings DIAMOND = new IncubatorSettings(96, 4);
    private static final IncubatorSettings NETHERITE = new IncubatorSettings(128, 8);

    public static Optional<IncubatorSettings> get (String tier) {
        return Optional.ofNullable(settings.get(tier));
    }

    public static void load () {
        File file = new File(FILE);
        File folder = file.getParentFile();
        if (!folder.exists()) folder.mkdirs();

        settings.putIfAbsent("copper", COPPER);
        settings.putIfAbsent("iron", IRON);
        settings.putIfAbsent("gold", GOLD);
        settings.putIfAbsent("diamond", DIAMOND);
        settings.putIfAbsent("netherite", NETHERITE);

        if (!file.exists()) {
            save();
        }

        try {
            JsonReader reader = JsonReader.file(file);
            if (reader != null) {
                JsonElement element = JsonParser.parseReader(new FileReader(file));
                if (!(element instanceof JsonObject json)) {
                    save();
                    return;
                }

                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    if (entry.getValue() instanceof JsonObject jsonObject) {
                        settings.put(entry.getKey(), IncubatorSettings.fromJson(jsonObject));
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            DaycarePlusServer.LOGGER.info("No incubator config found, creating default.");
        }
        catch (Exception e) {
            DaycarePlusServer.LOGGER.error("Error reading Daycare+ incubator config: ", e);
        }
        save();
    }

    public static void save () {
        JsonBuilder builder = new JsonBuilder();

        for (Map.Entry<String, IncubatorSettings> entry : settings.entrySet()) {
            builder.append(entry.getKey(), entry.getValue().toJson());
        }

        try (FileWriter writer = new FileWriter(FILE)) {
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(builder.getJson()));
        }
        catch (IOException e) {
            DaycarePlusServer.LOGGER.error("Error whilst saving config: ", e);
        }
    }

    public record IncubatorSettings (int capacity, int eggsToTick) {
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

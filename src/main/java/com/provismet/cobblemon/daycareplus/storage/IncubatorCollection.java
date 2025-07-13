package com.provismet.cobblemon.daycareplus.storage;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.provismet.cobblemon.daycareplus.DaycarePlusServer;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public class IncubatorCollection implements Iterable<Map.Entry<String, EggStorage>> {
    public static final Codec<IncubatorCollection> CODEC = Codec.unboundedMap(Codec.STRING, EggStorage.CODEC).xmap(IncubatorCollection::new, IncubatorCollection::getStorageMap);
    private static final Map<String, IncubatorCollection> playerMap = new HashMap<>();

    private final Map<String, EggStorage> storageMap;

    public IncubatorCollection (Map<String, EggStorage> storages) {
        this.storageMap = new HashMap<>(storages); // Force this to always be mutable.
    }

    public static IncubatorCollection getOrCreate (PlayerEntity player) {
        return playerMap.computeIfAbsent(player.getUuidAsString(), string -> new IncubatorCollection(Map.of()));
    }

    public static IncubatorCollection getCollection (String uuidString) {
        return playerMap.get(uuidString);
    }

    public void put (String label, EggStorage storage) {
        this.storageMap.put(label, storage);
    }

    public Optional<EggStorage> get (String label) {
        return Optional.ofNullable(this.storageMap.get(label));
    }

    public boolean has (String label) {
        return this.storageMap.containsKey(label);
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<String, EggStorage>> iterator () {
        return this.storageMap.entrySet().iterator();
    }

    public void saveToJson (PlayerEntity owner) {
        File file = new File("./world/incubators/" + owner.getUuidAsString() + ".json");
        DataResult<JsonElement> result = CODEC.encodeStart(JsonOps.INSTANCE, this);
        result.ifSuccess(json -> {
            String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(json);
            file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(jsonString);
            }
            catch (IOException e) {
                DaycarePlusServer.LOGGER.error("Failed to save incubator data for {}, uuid: {}", owner.getName().getString(), owner.getUuidAsString());
                DaycarePlusServer.LOGGER.error("Incubator JSON: {}", jsonString);
                DaycarePlusServer.LOGGER.error("Stack Trace: ", e);
            }
        });
    }

    public static void loadFromJson (PlayerEntity owner) {
        File file = new File("./world/incubators/" + owner.getUuidAsString() + ".json");
        try {
            JsonElement json = JsonParser.parseReader(new FileReader(file));
            DataResult<Pair<IncubatorCollection, JsonElement>> result = CODEC.decode(JsonOps.INSTANCE, json);
            IncubatorCollection collection;

            if (result.isSuccess()) {
                collection = result.getOrThrow().getFirst();
            }
            else {
                collection = new IncubatorCollection(Map.of());
            }
            playerMap.putIfAbsent(owner.getUuidAsString(), collection);
        }
        catch (FileNotFoundException ignored) {

        }
        catch (Exception e) {
            DaycarePlusServer.LOGGER.error("Failed to read incubator file {}", file.getAbsolutePath());
            DaycarePlusServer.LOGGER.error("Stack Trace: ", e);
        }
    }

    private Map<String, EggStorage> getStorageMap () {
        return this.storageMap;
    }
}

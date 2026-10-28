package com.smartstudyplanner.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility providing properly configured Gson instance for Java 17+ Date/Time serialization.
 */
public class GsonUtil {

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, (JsonSerializer<LocalDate>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src != null ? src.format(DateTimeFormatter.ISO_LOCAL_DATE) : ""))
            .registerTypeAdapter(LocalDate.class, (JsonDeserializer<LocalDate>) (json, typeOfT, context) ->
                    json != null && !json.getAsString().isEmpty() ? LocalDate.parse(json.getAsString()) : null)

            .registerTypeAdapter(LocalTime.class, (JsonSerializer<LocalTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src != null ? src.format(DateTimeFormatter.ISO_LOCAL_TIME) : ""))
            .registerTypeAdapter(LocalTime.class, (JsonDeserializer<LocalTime>) (json, typeOfT, context) ->
                    json != null && !json.getAsString().isEmpty() ? LocalTime.parse(json.getAsString()) : null)

            .registerTypeAdapter(LocalDateTime.class, (JsonSerializer<LocalDateTime>) (src, typeOfSrc, context) ->
                    new JsonPrimitive(src != null ? src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : ""))
            .registerTypeAdapter(LocalDateTime.class, (JsonDeserializer<LocalDateTime>) (json, typeOfT, context) ->
                    json != null && !json.getAsString().isEmpty() ? LocalDateTime.parse(json.getAsString()) : null)
            .create();

    public static Gson getGson() {
        return gson;
    }
}


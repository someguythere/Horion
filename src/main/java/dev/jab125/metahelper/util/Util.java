package dev.jab125.metahelper.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class Util {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    public static JsonObject jsonObject(String str) {
        return GSON.fromJson(str, JsonObject.class);
    }

    public static <T> String toString(T obj) {
        return GSON.toJson(obj);
    }

    public static JsonArray jsonArray(String str) {
        return GSON.fromJson(str, JsonArray.class);
    }
}

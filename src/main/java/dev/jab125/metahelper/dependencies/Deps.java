package dev.jab125.metahelper.dependencies;

import com.google.gson.JsonObject;
import dev.jab125.metahelper.util.Changelog;

import java.util.List;

public interface Deps {
    JsonObject get(List<String> mcVersions, JsonObject previous) throws Throwable;

    String id();

    default List<Changelog> changelogs(JsonObject before, JsonObject after) {
        return List.of();
    }
}

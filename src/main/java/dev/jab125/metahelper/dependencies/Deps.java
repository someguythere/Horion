package dev.jab125.metahelper.dependencies;

import com.google.gson.JsonObject;
import dev.jab125.metahelper.util.Changelog;
import dev.jab125.metahelper.version.Version;

import java.util.List;
import java.util.Map;

public interface Deps {
    Map<String, Object> get(List<String> mcVersions) throws Throwable;

    String id();

    default List<Changelog> changelogs(JsonObject before, JsonObject after) {
        return List.of();
    }
}

package dev.jab125.metahelper.dependencies;

import dev.jab125.metahelper.version.Version;

import java.util.List;
import java.util.Map;

public interface Deps {
    Map<String, Object> get(List<String> mcVersions) throws Throwable;

    String id();
}

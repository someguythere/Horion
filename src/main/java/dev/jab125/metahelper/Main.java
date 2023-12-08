package dev.jab125.metahelper;

import dev.jab125.metahelper.dependencies.*;
import dev.jab125.metahelper.util.Util;
import okhttp3.OkHttpClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static final OkHttpClient CLIENT = new OkHttpClient.Builder().build();
    public static final Deps[] deps = {
            new Fabric(),
            new Quilt(),
            new Forge(),
            new NeoForge(),
            new ArchitecturyApi(),
            new RoughlyEnoughItems(),
            new ClothConfig()
    };

    public static void main(String[] args) throws Throwable {
        List<String> mcVersions = List.of("1.16.5", "1.18.2", "1.19.2", "1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4");
        Map<String, Object> depMap = new LinkedHashMap<>();
        for (Deps dep : deps) {
            depMap.put(dep.id(), dep.get(mcVersions));
        }
        Files.writeString(Path.of("meta.json"), Util.toString(depMap));
    }
}

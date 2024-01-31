package dev.jab125.metahelper.dependencies;

import com.google.gson.JsonObject;
import dev.jab125.metahelper.Main;
import okhttp3.Request;
import okhttp3.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.jab125.metahelper.util.Util.jsonArray;

public class ArchitecturyApi implements Deps {
    public static final String ARCHITECTURY_API_URL = "https://api.modrinth.com/v2/project/architectury-api/version";
    @Override
    public JsonObject get(List<String> mcVersions) throws Throwable {
        JsonObject obj = new JsonObject();
        JsonObject fabric = new JsonObject();
        JsonObject forge = new JsonObject();
        JsonObject neoforge = new JsonObject();
        Map<String, JsonObject> loaders = Map.of("fabric", fabric, "forge", forge, "neoforge", neoforge);
        Request request = new Request.Builder()
                .url(ARCHITECTURY_API_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request).execute()) {
            List<JsonObject> array = (List<JsonObject>) (Object) jsonArray(response.body().string()).asList();
            for (String s : loaders.keySet()) {
                for (String mcVersion : mcVersions) {
                    try {
                        String dep = array.stream().filter(a -> a.getAsJsonArray("game_versions").asList().stream().map(b -> b.getAsString()).toList().contains(mcVersion) && a.getAsJsonArray("loaders").asList().stream().map(b -> b.getAsString()).toList().contains(s)).findFirst().orElseThrow().getAsJsonPrimitive("version_number").getAsString().split("\\+")[0];
                        loaders.get(s).addProperty(mcVersion, "dev.architectury:architectury-" + s + ":" + dep);
                    } catch (Throwable t) {
                        System.err.println("Failed to fetch version for " + mcVersion);
                    }
                }
            }
        }
        obj.add("fabric", fabric);
        obj.add("forge", forge);
        obj.add("neoforge", neoforge);
        return obj;
    }

    @Override
    public String id() {
        return "architectury-api";
    }
}

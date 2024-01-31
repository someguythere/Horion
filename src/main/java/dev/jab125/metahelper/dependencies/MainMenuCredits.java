package dev.jab125.metahelper.dependencies;

import com.google.gson.JsonObject;
import dev.jab125.metahelper.Main;
import okhttp3.Request;
import okhttp3.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.jab125.metahelper.util.Util.jsonArray;

public class MainMenuCredits implements Deps {
    public static final String MAIN_MENU_CREDITS_URL = "https://api.modrinth.com/v2/project/main-menu-credits/version";
    @Override
    public JsonObject get(List<String> mcVersions) throws Throwable {
        JsonObject obj = new JsonObject();
        JsonObject fabric = new JsonObject();
        Map<String, JsonObject> loaders = Map.of("fabric", fabric);
        Request request = new Request.Builder()
                .url(MAIN_MENU_CREDITS_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request).execute()) {
            List<JsonObject> array = (List<JsonObject>) (Object) jsonArray(response.body().string()).asList();
            for (String s : loaders.keySet()) {
                for (String mcVersion : mcVersions) {
                    try {
                        String dep = array.stream().filter(a -> a.getAsJsonArray("game_versions").asList().stream().map(b -> b.getAsString()).toList().contains(mcVersion) && a.getAsJsonArray("loaders").asList().stream().map(b -> b.getAsString()).toList().contains(s)).findFirst().orElseThrow().getAsJsonPrimitive("id").getAsString().split("\\+")[0];
                        String prefix = "maven.modrinth:main-menu-credits:";
                        loaders.get(s).addProperty(mcVersion, prefix + dep);
                    } catch (Throwable t) {
                        System.err.println("Failed to fetch version for " + mcVersion);
                    }
                }
            }
        }
        obj.add("fabric", fabric);
        return obj;
    }

    @Override
    public String id() {
        return "main-menu-credits";
    }
}

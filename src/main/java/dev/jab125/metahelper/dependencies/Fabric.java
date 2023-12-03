package dev.jab125.metahelper.dependencies;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jab125.metahelper.Main;
import dev.jab125.metahelper.version.Version;
import okhttp3.OkHttp;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.jab125.metahelper.util.Util.jsonArray;
import static dev.jab125.metahelper.util.Util.jsonObject;

public class Fabric implements Deps {
    public static final String FABRIC_META = "https://meta.fabricmc.net/v2/versions";
    public static final String FABRIC_API_URL = "https://api.modrinth.com/v2/project/fabric-api/version";

    public static final String MOD_MENU_URL = "https://api.modrinth.com/v2/project/modmenu/version";

    @Override
    public Map<String, Object> get(List<String> mcVersions) throws Throwable {
        Map<String, Object> obj = new LinkedHashMap<>();
        Request request = new Request.Builder()
                .url(FABRIC_META)
                .build();
        try (Response response = Main.CLIENT.newCall(request).execute()) {
            JsonObject object = jsonObject(response.body().string());
            JsonArray loader = object.getAsJsonArray("loader");
            String fabricLoader = loader.asList().stream().filter(a -> a.getAsJsonObject().getAsJsonPrimitive("stable").getAsBoolean()).findFirst().orElseThrow().getAsJsonObject().get("maven").getAsString();
            obj.put("loader", fabricLoader);

            JsonArray mappings = object.getAsJsonArray("mappings");
            Map<String, String> yarn = new LinkedHashMap<>();
            for (String mcVersion : mcVersions) {
                try {
                    yarn.put(mcVersion, mappings.asList().stream().filter(a -> mcVersion.equals(a.getAsJsonObject().getAsJsonPrimitive("gameVersion").getAsString())).findFirst().orElseThrow().getAsJsonObject().getAsJsonPrimitive("maven").getAsString());
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.put("yarn", yarn);
        }
        Request request2 = new Request.Builder()
                .url(FABRIC_API_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request2).execute()) {
            List<JsonObject> array = (List<JsonObject>) (Object) jsonArray(response.body().string()).asList();
            Map<String, String> fabricApi = new LinkedHashMap<>();
            for (String mcVersion : mcVersions) {
                try {
                    String dep = array.stream().filter(a -> a.getAsJsonArray("game_versions").asList().stream().map(b -> b.getAsString()).toList().contains(mcVersion)).findFirst().orElseThrow().getAsJsonPrimitive("version_number").getAsString();
                    fabricApi.put(mcVersion, "net.fabricmc.fabric-api:fabric-api:" + dep);
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.put("fabric-api", fabricApi);
        }

        Request request3 = new Request.Builder()
                .url(MOD_MENU_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request3).execute()) {
            List<JsonObject> array = (List<JsonObject>) (Object) jsonArray(response.body().string()).asList();
            Map<String, String> modMenu = new LinkedHashMap<>();
            for (String mcVersion : mcVersions) {
                try {
                    JsonObject object = array.stream().filter(a -> a.getAsJsonArray("game_versions").asList().stream().map(b -> b.getAsString()).toList().contains(mcVersion)).findFirst().orElseThrow();
                    //String dep = object.getAsJsonPrimitive("version_number").getAsString();
                    String fileName = object.getAsJsonArray("files").get(0).getAsJsonObject().getAsJsonPrimitive("filename").getAsString();
                    String dep = fileName.substring(8, fileName.length()-4); // some files on Modrinth seem to be retroactively added.
                    modMenu.put(mcVersion, (dep.contains("+build.") ? "io.github.prospector:modmenu:" : dep.matches("[^-]+-\\d\\d?\\d?") ? "io.github.prospector.modmenu:ModMenu:" : "com.terraformersmc:modmenu:") + dep);
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.put("modmenu", modMenu);
        }
        return obj;
    }

    @Override
    public String id() {
        return "fabric";
    }
}

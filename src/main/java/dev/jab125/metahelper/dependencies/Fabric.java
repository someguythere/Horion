package dev.jab125.metahelper.dependencies;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.jab125.metahelper.Main;
import okhttp3.Request;
import okhttp3.Response;

import java.util.List;

import static dev.jab125.metahelper.util.Util.jsonArray;
import static dev.jab125.metahelper.util.Util.jsonObject;

public class Fabric implements Deps {
    public static final String FABRIC_META = "https://meta.fabricmc.net/v2/versions";
    public static final String FABRIC_API_URL = "https://api.modrinth.com/v2/project/fabric-api/version";

    public static final String MOD_MENU_URL = "https://api.modrinth.com/v2/project/modmenu/version";

    @Override
    public JsonObject get(List<String> mcVersions, JsonObject previous) throws Throwable {
        JsonObject obj = new JsonObject();
        Request request = new Request.Builder()
                .url(FABRIC_META)
                .build();
        try (Response response = Main.CLIENT.newCall(request).execute()) {
            JsonObject object = jsonObject(response.body().string());
            JsonArray loader = object.getAsJsonArray("loader");
            String fabricLoader = loader.asList().stream().filter(a -> a.getAsJsonObject().getAsJsonPrimitive("stable").getAsBoolean()).findFirst().orElseThrow().getAsJsonObject().get("maven").getAsString();
            obj.addProperty("loader", fabricLoader);

            JsonArray mappings = object.getAsJsonArray("mappings");
            JsonObject yarn = new JsonObject();
            for (String mcVersion : mcVersions) {
                try {
                    yarn.addProperty(mcVersion, mappings.asList().stream().filter(a -> mcVersion.equals(a.getAsJsonObject().getAsJsonPrimitive("gameVersion").getAsString())).findFirst().orElseThrow().getAsJsonObject().getAsJsonPrimitive("maven").getAsString() + ":v2");
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.add("yarn", yarn);
        }
        Request request2 = new Request.Builder()
                .url(FABRIC_API_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request2).execute()) {
            List<JsonObject> array = (List<JsonObject>) (Object) jsonArray(response.body().string()).asList();
            JsonObject fabricApi = new JsonObject();
            for (String mcVersion : mcVersions) {
                try {
                    String dep = array.stream().filter(a -> a.getAsJsonArray("game_versions").asList().stream().map(b -> b.getAsString()).toList().contains(mcVersion)).findFirst().orElseThrow().getAsJsonPrimitive("version_number").getAsString();
                    fabricApi.addProperty(mcVersion, "net.fabricmc.fabric-api:fabric-api:" + dep);
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.add("fabric-api", fabricApi);
        }

        Request request3 = new Request.Builder()
                .url(MOD_MENU_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request3).execute()) {
            List<JsonObject> array = (List<JsonObject>) (Object) jsonArray(response.body().string()).asList();
            JsonObject modMenu = new JsonObject();
            for (String mcVersion : mcVersions) {
                try {
                    JsonObject object = array.stream().filter(a -> a.getAsJsonArray("game_versions").asList().stream().map(b -> b.getAsString()).toList().contains(mcVersion)).findFirst().orElseThrow();
                    //String dep = object.getAsJsonPrimitive("version_number").getAsString();
                    String fileName = object.getAsJsonArray("files").get(0).getAsJsonObject().getAsJsonPrimitive("filename").getAsString();
                    String dep = fileName.substring(8, fileName.length()-4); // some files on Modrinth seem to be retroactively added.
                    modMenu.addProperty(mcVersion, (dep.contains("+build.") ? "io.github.prospector:modmenu:" : dep.matches("[^-]+-\\d\\d?\\d?") ? "io.github.prospector.modmenu:ModMenu:" : "com.terraformersmc:modmenu:") + dep);
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.add("modmenu", modMenu);
        }
        return obj;
    }

    @Override
    public String id() {
        return "fabric";
    }
}

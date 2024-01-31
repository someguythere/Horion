package dev.jab125.metahelper.dependencies;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.jab125.metahelper.Main;
import dev.jab125.metahelper.util.Metadata;
import okhttp3.Request;
import okhttp3.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.jab125.metahelper.util.Util.jsonObject;

public class Quilt implements Deps {
    public static final String QUILT_META = "https://meta.quiltmc.org/v3/versions";
    public static final String QUILT_STANDARD_LIBRARIES_URL = "https://maven.quiltmc.org/repository/release/org/quiltmc/qsl/maven-metadata.xml";

    public static final String QUILTED_FABRIC_API_URL = "https://maven.quiltmc.org/repository/release/org/quiltmc/quilted-fabric-api/quilted-fabric-api/maven-metadata.xml";

    @Override
    public JsonObject get(List<String> mcVersions) throws Throwable {
        JsonObject obj = new JsonObject();
        Request request = new Request.Builder()
                .url(QUILT_META)
                .build();
        try (Response response = Main.CLIENT.newCall(request).execute()) {
            JsonObject object = jsonObject(response.body().string());
            JsonArray loader = object.getAsJsonArray("loader");
            String quiltLoader = loader.asList().stream().filter(a -> !a.getAsJsonObject().getAsJsonPrimitive("version").getAsString().contains("beta")).findFirst().orElseThrow().getAsJsonObject().get("maven").getAsString();
            obj.addProperty("loader", quiltLoader);
        }
        Request request2 = new Request.Builder()
                .url(QUILT_STANDARD_LIBRARIES_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request2).execute()) {
            JsonObject qsl = new JsonObject();
            XmlMapper xmlMapper = new XmlMapper();
            Metadata mavenMetadata = xmlMapper.readValue(response.body().bytes(), Metadata.class);
            List<String> versions = reverse(mavenMetadata.versioning.versions.stream()).toList();
            for (String mcVersion : mcVersions) {
                try {
                    qsl.addProperty(mcVersion, "org.quiltmc:qsl:" + versions.stream().filter(a -> a.endsWith("+" + mcVersion)).findFirst().orElseThrow());
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.add("qsl",  qsl);
        }

        Request request3 = new Request.Builder()
                .url(QUILTED_FABRIC_API_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request3).execute()) {
            JsonObject qfapi = new JsonObject();
            XmlMapper xmlMapper = new XmlMapper();
            Metadata mavenMetadata = xmlMapper.readValue(response.body().bytes(), Metadata.class);
            List<String> versions = reverse(mavenMetadata.versioning.versions.stream()).toList();
            for (String mcVersion : mcVersions) {
                try {
                    qfapi.addProperty(mcVersion, "org.quiltmc.quilted-fabric-api:quilted-fabric-api:" + versions.stream().filter(a -> a.endsWith("-" + mcVersion)).findFirst().orElseThrow());
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.add("quilted-fabric-api", qfapi);
        }
//            List<JsonObject> array = (List<JsonObject>) (Object) jsonArray(response.body().string()).asList();
//            Map<String, String> fabricApi = new LinkedHashMap<>();
//            for (String mcVersion : mcVersions) {
//                try {
//                    String dep = array.stream().filter(a -> a.getAsJsonArray("game_versions").asList().stream().map(b -> b.getAsString()).toList().contains(mcVersion)).findFirst().orElseThrow().getAsJsonPrimitive("version_number").getAsString();
//                    fabricApi.put(mcVersion, "net.fabricmc.fabric-api:fabric-api:" + dep);
//                } catch (Throwable t) {
//                    System.err.println("Failed to fetch version for " + mcVersion);
//                }
//            }
//            obj.put("fabric-api", fabricApi);
//        }
//
//        Request request3 = new Request.Builder()
//                .url(MOD_MENU_URL)
//                .build();
//        try (Response response = Main.CLIENT.newCall(request3).execute()) {
//            List<JsonObject> array = (List<JsonObject>) (Object) jsonArray(response.body().string()).asList();
//            Map<String, String> modMenu = new LinkedHashMap<>();
//            for (String mcVersion : mcVersions) {
//                try {
//                    JsonObject object = array.stream().filter(a -> a.getAsJsonArray("game_versions").asList().stream().map(b -> b.getAsString()).toList().contains(mcVersion)).findFirst().orElseThrow();
//                    //String dep = object.getAsJsonPrimitive("version_number").getAsString();
//                    String fileName = object.getAsJsonArray("files").get(0).getAsJsonObject().getAsJsonPrimitive("filename").getAsString();
//                    String dep = fileName.substring(8, fileName.length()-4); // some files on Modrinth seem to be retroactively added.
//                    modMenu.put(mcVersion, (dep.contains("+build.") ? "io.github.prospector:modmenu:" : dep.matches("[^-]+-\\d\\d?\\d?") ? "io.github.prospector.modmenu:ModMenu:" : "com.terraformersmc:modmenu:") + dep);
//                } catch (Throwable t) {
//                    System.err.println("Failed to fetch version for " + mcVersion);
//                }
//            }
//            obj.put("modmenu", modMenu);
//        }
        return obj;
    }

    @Override
    public String id() {
        return "quilt";
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<T> reverse(Stream<T> input) {
        Object[] temp = input.toArray();
        return (Stream<T>) IntStream.range(0, temp.length)
                .mapToObj(i -> temp[temp.length - i - 1]);
    }
}

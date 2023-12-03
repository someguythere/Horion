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

public class Forge implements Deps {
    public static final String FORGE_URL = "https://maven.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml";

    @Override
    public Map<String, Object> get(List<String> mcVersions) throws Throwable {
        Map<String, Object> obj = new LinkedHashMap<>();
        Request request = new Request.Builder()
                .url(FORGE_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request).execute()) {
            Map<String, String> forge = new LinkedHashMap<>();
            XmlMapper xmlMapper = new XmlMapper();
            Metadata mavenMetadata = xmlMapper.readValue(response.body().bytes(), Metadata.class);
            List<String> versions = mavenMetadata.versioning.versions.stream().toList();
            for (String mcVersion : mcVersions) {
                try {
                    forge.put(mcVersion, "net.minecraftforge:forge:" + versions.stream().filter(a -> mcVersion.equals(a.split("-")[0])).findFirst().orElseThrow());
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.put("forge", forge);
        }
        return obj;
    }

    @Override
    public String id() {
        return "forge";
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<T> reverse(Stream<T> input) {
        Object[] temp = input.toArray();
        return (Stream<T>) IntStream.range(0, temp.length)
                .mapToObj(i -> temp[temp.length - i - 1]);
    }
}

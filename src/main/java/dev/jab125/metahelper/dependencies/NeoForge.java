package dev.jab125.metahelper.dependencies;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.JsonObject;
import dev.jab125.metahelper.Main;
import dev.jab125.metahelper.util.Metadata;
import okhttp3.Request;
import okhttp3.Response;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NeoForge implements Deps {
    public static final String FORGE_URL = "https://maven.neoforged.net/net/neoforged/neoforge/maven-metadata.xml";

    @Override
    public JsonObject get(List<String> mcVersions, JsonObject previous) throws Throwable {
        JsonObject obj = new JsonObject();
        Request request = new Request.Builder()
                .url(FORGE_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request).execute()) {
            JsonObject neoforge = new JsonObject();
            XmlMapper xmlMapper = new XmlMapper();
            Metadata mavenMetadata = xmlMapper.readValue(response.body().bytes(), Metadata.class);
            List<String> versions = reverse(mavenMetadata.versioning.versions.stream()).toList();
            for (String mcVersion : mcVersions) {
                try {
                    neoforge.addProperty(mcVersion, "net.neoforged:neoforge:" + versions.stream().filter(a -> {
                        String[] split = a.split("\\.");
                        String major = split[0];
                        String minor = split[1];
                        return mcVersion.equals("1." + major + "." + minor);
                    }).findFirst().orElseThrow());
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.add("neoforge", neoforge);
        }
        return obj;
    }

    @Override
    public String id() {
        return "neoforge";
    }

    @SuppressWarnings("unchecked")
    static <T> Stream<T> reverse(Stream<T> input) {
        Object[] temp = input.toArray();
        return (Stream<T>) IntStream.range(0, temp.length)
                .mapToObj(i -> temp[temp.length - i - 1]);
    }
}

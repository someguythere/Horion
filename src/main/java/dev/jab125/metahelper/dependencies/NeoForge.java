package dev.jab125.metahelper.dependencies;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import dev.jab125.metahelper.Main;
import dev.jab125.metahelper.util.Metadata;
import okhttp3.Request;
import okhttp3.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NeoForge implements Deps {
    public static final String FORGE_URL = "https://maven.neoforged.net/net/neoforged/neoforge/maven-metadata.xml";

    @Override
    public Map<String, Object> get(List<String> mcVersions) throws Throwable {
        Map<String, Object> obj = new LinkedHashMap<>();
        Request request = new Request.Builder()
                .url(FORGE_URL)
                .build();
        try (Response response = Main.CLIENT.newCall(request).execute()) {
            Map<String, String> neoforge = new LinkedHashMap<>();
            XmlMapper xmlMapper = new XmlMapper();
            Metadata mavenMetadata = xmlMapper.readValue(response.body().bytes(), Metadata.class);
            List<String> versions = reverse(mavenMetadata.versioning.versions.stream()).toList();
            for (String mcVersion : mcVersions) {
                try {
                    neoforge.put(mcVersion, "net.neoforged:neoforge:" + versions.stream().filter(a -> {
                        String[] split = a.split("\\.");
                        String major = split[0];
                        String minor = split[1];
                        return mcVersion.equals("1." + major + "." + minor);
                    }).findFirst().orElseThrow());
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.put("neoforge", neoforge);
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

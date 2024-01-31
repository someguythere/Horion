package dev.jab125.metahelper.dependencies;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.jab125.metahelper.Main;
import dev.jab125.metahelper.util.Changelog;
import dev.jab125.metahelper.util.DiscordWebhook;
import dev.jab125.metahelper.util.Metadata;
import okhttp3.Request;
import okhttp3.Response;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class NeoForge implements Deps {
    public static final String FORGE_URL = "https://maven.neoforged.net/net/neoforged/neoforge/maven-metadata.xml";
    private final List<JsonObject> changelogs = new ArrayList<>();
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
                JsonElement neoforge1 = previous.get("neoforge").getAsJsonObject().get(mcVersion);
                String prevNeoForgeVer;
                if (neoforge1 == null || neoforge1.isJsonNull()) {
                    prevNeoForgeVer = null;
                } else {
                    prevNeoForgeVer = neoforge1.getAsString();
                }
                try {
                    String neoforgeVer = "net.neoforged:neoforge:" + versions.stream().filter(a -> {
                        String[] split = a.split("\\.");
                        String major = split[0];
                        String minor = split[1];
                        return mcVersion.equals("1." + major + "." + minor);
                    }).findFirst().orElseThrow();
                    if (!neoforgeVer.equals(prevNeoForgeVer)) {
                        JsonObject object = new JsonObject();
                        object.addProperty("minecraft", mcVersion);
                        object.addProperty("previous", prevNeoForgeVer == null ? "" : prevNeoForgeVer.substring("net.neoforged:neoforge:".length()));
                        object.addProperty("current", neoforgeVer.substring("net.neoforged:neoforge:".length()));
                        changelogs.add(object);
                    }
                    neoforge.addProperty(mcVersion, neoforgeVer);
                } catch (Throwable t) {
                    System.err.println("Failed to fetch version for " + mcVersion);
                }
            }
            obj.add("neoforge", neoforge);
        }
        return obj;
    }

    @Override
    public List<Changelog> changelogs(JsonObject before, JsonObject after) {
        ArrayList<DiscordWebhook.EmbedObject> embedObjects = new ArrayList<>();
        for (JsonObject changelog : this.changelogs) {
            String minecraft = changelog.getAsJsonPrimitive("minecraft").getAsString();
            String previous = changelog.getAsJsonPrimitive("previous").getAsString();
            String current = changelog.getAsJsonPrimitive("current").getAsString();
            DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject().setColor(Color.ORANGE);
            embedObject.setTitle("New NeoForge version!");
            embedObject.addField("Minecraft version", minecraft, true);
            embedObject.addField("Latest", "**" + previous + "** -> **" + current + "**", true);
            embedObjects.add(embedObject);
        }
        if (embedObjects.isEmpty()) return Deps.super.changelogs(before, after);
        return List.of(new Changelog(null, embedObjects));
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

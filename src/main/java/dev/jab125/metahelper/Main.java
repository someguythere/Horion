package dev.jab125.metahelper;

import com.google.gson.JsonObject;
import dev.jab125.metahelper.dependencies.*;
import dev.jab125.metahelper.util.Changelog;
import dev.jab125.metahelper.util.DiscordWebhook;
import dev.jab125.metahelper.util.Util;
import okhttp3.OkHttpClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class Main {

    public static final OkHttpClient CLIENT = new OkHttpClient.Builder().build();
    public static final Deps[] deps = {
            new Fabric(),
            new Quilt(),
            new Forge(),
            new NeoForge(),
            new ArchitecturyApi(),
            new RoughlyEnoughItems(),
            new ClothConfig(),
            new MainMenuCredits(),
            new FancyMenu(),
            new Konkrete()
    };

    public static void main(String[] args) throws Throwable {
        List<String> mcVersions = List.of("1.16.5", "1.18.2", "1.19.2", "1.19.3", "1.19.4", "1.20", "1.20.1", "1.20.2", "1.20.3", "1.20.4");
        JsonObject depMap = new JsonObject();
        JsonObject jsonObject = Util.jsonObject(Files.readString(Path.of("meta.json")));
        for (Deps dep : deps) {
            depMap.add(dep.id(), dep.get(mcVersions, jsonObject.get(dep.id()).getAsJsonObject()));
        }
        String s = Util.toString(depMap);
        final String url = System.getenv("DISCORD_WEBHOOK_URL");
        if (url != null) {
            for (Deps dep : deps) {
                List<Changelog> changelogs = dep.changelogs(jsonObject.get(dep.id()).getAsJsonObject(), depMap.get(dep.id()).getAsJsonObject());
                if (changelogs != null && !changelogs.isEmpty()) {
                    for (Changelog changelog : changelogs) {
                        DiscordWebhook discordWebhook = new DiscordWebhook(url);
                        for (DiscordWebhook.EmbedObject embed : changelog.embeds()) {
                            discordWebhook.addEmbed(embed);
                        }
                        discordWebhook.setAvatarUrl(changelog.icon());
                        discordWebhook.execute();
                    }
                }
            }
        }
        Files.writeString(Path.of("meta.json"), s);
    }
}

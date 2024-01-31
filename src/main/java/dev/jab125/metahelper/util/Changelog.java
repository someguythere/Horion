package dev.jab125.metahelper.util;

import java.util.List;

public record Changelog(String icon, DiscordWebhook.EmbedObject[] embeds) {
	public Changelog(String icon, List<DiscordWebhook.EmbedObject> embeds) {
		this(icon, embeds.toArray(new DiscordWebhook.EmbedObject[0]));
	}
}

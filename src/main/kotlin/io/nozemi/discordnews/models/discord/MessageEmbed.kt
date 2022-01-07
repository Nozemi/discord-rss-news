package io.nozemi.discordnews.models.discord

import com.fasterxml.jackson.annotation.JsonProperty

data class MessageEmbed(
    val title: String? = null,
    val type: String? = null,
    val description: String? = null,
    val url: String? = null,
    // TODO: Look into using a date type instead of String.
    val timestamp: String? = null,
    val color: Int? = null,
    val footer: MessageEmbedFooter? = null,
    val image: MessageEmbedImage? = null,
    val thumbnail: MessageEmbedThumbnail? = null,
    val video: MessageEmbedVideo? = null,
    val provider: MessageEmbedProvider? = null,
    val author: MessageEmbedAuthor? = null,
    val fields: List<MessageEmbedField>? = null
) {
    override fun toString(): String {
        return title ?: "lol"
    }
}

data class MessageEmbedFooter(
    val lol: String
)

data class MessageEmbedImage(
    val url: String,
    @JsonProperty("proxy_url")
    val proxyUrl: String? = null,
    val height: Int? = null,
    val width: Int? = null
)

data class MessageEmbedThumbnail(
    val lol: String
)

data class MessageEmbedVideo(
    val lol: String
)

data class MessageEmbedProvider(
    val lol: String
)

data class MessageEmbedAuthor(
    val name: String,
    val url: String? = null,
    @JsonProperty("icon_url")
    val iconUrl: String? = null,
    @JsonProperty("proxy_icon_url")
    val proxyIconUrl: String? = null
)

data class MessageEmbedField(
    val lol: String
)
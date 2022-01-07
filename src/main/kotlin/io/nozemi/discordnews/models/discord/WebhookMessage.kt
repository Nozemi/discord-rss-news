package io.nozemi.discordnews.models.discord

import com.fasterxml.jackson.annotation.JsonIgnore
import io.nozemi.discordnews.models.DiscordWebhookConfig
import io.nozemi.discordnews.models.NewsArticleEmbedModel

data class WebhookMessage(
    @JsonIgnore
    val config: DiscordWebhookConfig,
    val content: String? = null,
    val username: String? = null,
    val avatar_url: String? = null,
    val tts: Boolean = false,
    val embeds: List<MessageEmbed> = listOf(),
    val allowedMentions: String? = null,
    val files: List<String> = listOf()
) {
    val isValid: Boolean = ((content != null && content.isNotEmpty()) || embeds.isNotEmpty() || files.isNotEmpty())

    override fun toString(): String {
        return "WebhookMessage(embeds(${embeds.size})=$embeds, files=${files.size})"
    }

    companion object {
        fun from(articles: List<NewsArticleEmbedModel>, webhookConfig: DiscordWebhookConfig) = WebhookMessage(
            config = webhookConfig,
            embeds = articles.map {
                MessageEmbed(
                    title = it.title,
                    description = it.ingress,
                    url = it.url,
                    author = when {
                        it.author != null -> MessageEmbedAuthor(name = it.author)
                        else -> null
                    },
                    image = when {
                        it.thumbnailUrl != null -> MessageEmbedImage(url = it.thumbnailUrl)
                        else -> null
                    }
                )
            }
        )
    }
}
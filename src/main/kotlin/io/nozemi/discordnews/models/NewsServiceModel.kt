package io.nozemi.discordnews.models

import com.github.michaelbull.logging.InlineLogger

private val logger = InlineLogger()

/**
 *
 * @param feedSourceUrl The URL to the feed where articles are found. Can be anything you want.
 * @param fetchInterval The interval (in minutes) to be fetching articles.
 * @param discordWebhookConfig The Discord webhook ID and token - used to send messages.
 * @param defaultThumbnailUrl The default thumbnail to use when can't be fetched from the article.
 * @param feedType The type of feed we'll be parsing.
 */
data class NewsServiceModel(
    val feedSourceUrl: String,
    val fetchInterval: Long,
    val discordWebhookConfig: DiscordWebhookConfig,
    val defaultThumbnailUrl: String? = null,
    val feedType: FeedType = FeedType.RSS,
    val valueMap: ElementsValueMap = ElementsValueMap()
)

data class XmlRoot(val version: String, val channel: List<*>) {

    fun parseArticles(valueMap: ElementsValueMap): List<NewsArticleEmbedModel> {
        return channel
            .asSequence().filter { it !is String }
            .filter { it as LinkedHashMap<*, *>
                logger.trace { "Requires a title in: $it" }
                it.containsKey(valueMap.title)
            }
            .filter { it as LinkedHashMap<*, *>
                logger.trace { "Requires a ingress in: $it" }
                it.containsKey(valueMap.ingress)
            }
            .filter { it as LinkedHashMap<*, *>
                logger.trace { "Requires a url in: $it" }
                it.containsKey(valueMap.url)
            }
            .filter { it as LinkedHashMap<*, *>
                logger.trace { "Requires a publish date in: $it" }
                it.containsKey(valueMap.published)
            }
            .filter { it as LinkedHashMap<*, *>
                logger.trace { "Requires a publish date in: $it" }
                it.containsKey(valueMap.guid)
            }
            .map { it as LinkedHashMap<*, *>
                NewsArticleEmbedModel.from(it, valueMap)
            }.toList()
    }
}

enum class NewsFetchError(val message: String) {
    REQUEST_FAILED("Request has failed for unknown reason."),
    REQUEST_FAILED_ACCESS_DENIED("Request failed because access was denied."),
    NO_ARTICLES_FOUND("Request failed because response was empty.")
}

enum class FeedType {
    RSS,
    XML,
    JSON
}

data class ElementsValueMap(
    val guid: String = "guid",
    val articles: String = "channel",
    val article: String = "item",
    val title: String = "title",
    val url: String = "link",
    val ingress: String = "description",
    val thumbnailUrl: String = "img",
    val published: String = "pubDate",
    val author: String = "author"
)

data class DiscordWebhookConfig(
    val id: String,
    val token: String
)
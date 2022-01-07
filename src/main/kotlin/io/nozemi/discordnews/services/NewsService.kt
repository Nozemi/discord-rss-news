package io.nozemi.discordnews.services

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.michaelbull.logging.InlineLogger
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.nozemi.discordnews.models.NewsArticleEmbedModel
import org.springframework.http.HttpMethod
import com.github.michaelbull.result.Result
import io.nozemi.discordnews.helpers.executeHttpRequest

private val logger = InlineLogger()

/**
 * News monitoring service
 *
 * @param feedSourceUrl The URL to the feed where articles are found. Can be anything you want.
 * @param fetchInterval The interval (in minutes) to be fetching articles.
 * @param discordWebHookUrl The Discord webhook URL to publish fetched articles to.
 * @param defaultThumbnailUrl The default thumbnail to use when can't be fetched from the article.
 * @param feedType The type of feed we'll be parsing.
 */
class NewsService(
    private val feedSourceUrl: String,
    private val fetchInterval: Int,
    private val discordWebHookUrl: String,
    private val defaultThumbnailUrl: String? = null,
    private val feedType: FeedType = FeedType.RSS,
    private val valueMap: ElementsValueMap
) {
    private val xmlMapper = ObjectMapper(XmlFactory())
        .findAndRegisterModules()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false)
        .enable(SerializationFeature.INDENT_OUTPUT)

    fun fetchNews(): Result<List<NewsArticleEmbedModel>, NewsFetchError> {
        logger.trace { "Preparing request to $feedSourceUrl..." }

        return when (val responseBody = executeHttpRequest<String>(feedSourceUrl, HttpMethod.GET)) {
            is Ok -> {
                val parsedXml = xmlMapper.readValue<XmlRoot>(responseBody.component1())
                logger.debug { "Parsed response body to XmlRoot...\n$parsedXml" }

                val articles = parsedXml.parseArticles(valueMap)
                logger.debug { "Found ${articles.size} articles..." }

                when {
                    articles.isEmpty() -> Err(NewsFetchError.NO_ARTICLES_FOUND)
                    else -> Ok(articles)
                }
            }
            else -> Err(NewsFetchError.REQUEST_FAILED)
        }
    }
}

data class XmlRoot(
    val version: String,
    val channel: List<*>
) {
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
    val articles: String,
    val article: String,
    val title: String,
    val url: String,
    val ingress: String,
    val thumbnailUrl: String,
    val published: String,
)
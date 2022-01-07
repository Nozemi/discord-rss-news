package io.nozemi.discordnews.models

import com.github.michaelbull.logging.InlineLogger
import io.nozemi.discordnews.services.ElementsValueMap
import java.time.Instant
import java.util.*
import kotlin.collections.LinkedHashMap

private val logger = InlineLogger()

data class NewsArticleEmbedModel(
    val title: String,
    val ingress: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val author: String? = null,
    val published: Date,
    val lastEdited: Date? = null
) {

    companion object {
        fun from(map: LinkedHashMap<*, *>, valueMap: ElementsValueMap): NewsArticleEmbedModel {
            logger.debug { "Parsing article: $map..." }

            return NewsArticleEmbedModel(
                title = (map[valueMap.title.replace("article/", "")] ?: "unknown") as String,
                ingress = (map[valueMap.ingress.replace("article/", "")] ?: "unknown") as String,
                url = (map[valueMap.url.replace("article/", "")] ?: "unknown") as String,
                published = Date.from(Instant.now())
            )
        }
    }
}
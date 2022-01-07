package io.nozemi.discordnews.models

import com.github.michaelbull.logging.InlineLogger
import io.github.furstenheim.CopyDown
import io.github.furstenheim.OptionsBuilder
import io.nozemi.discordnews.helpers.toLinkedList
import java.time.Instant
import java.util.*

private val logger = InlineLogger()


data class NewsArticleEmbedModel(
    val guid: String,
    val title: String,
    val ingress: String,
    val url: String,
    val thumbnailUrl: String? = null,
    val author: String? = null,
    val published: Date,
    val lastEdited: Date? = null
) {

    override fun toString(): String {
        return "NewsArticleEmbedModel(guid=$guid, title=$title)"
    }

    companion object {
        fun from(map: LinkedHashMap<*, *>, valueMap: ElementsValueMap): NewsArticleEmbedModel {
            logger.trace { "Parsing article: $map..." }

            val guid = if (map[valueMap.guid] is LinkedHashMap<*, *>) {
                (map[valueMap.guid] as LinkedHashMap<*, *>).values.last().toString()
            } else {
                map[valueMap.guid].toString()
            }

            val optionsBuilder = OptionsBuilder.anOptions()
            val options = optionsBuilder.build()
            val converter = CopyDown(options)
            val rawIngress = map[valueMap.ingress].toString()
                .replace("<img(.*?)>".toRegex(), "")
                .replace("<a(.*)><\\/a>".toRegex(), "")
            logger.trace { "Converting HTML to markdown for: $rawIngress" }
            val ingressLines = converter.convert(rawIngress.trim()).trim().lines().toLinkedList()
            while (ingressLines.first().isBlank()) {
                ingressLines.remove()
            }
            val ingress = ingressLines.joinToString("\n")

            return NewsArticleEmbedModel(
                guid = guid,
                title = map[valueMap.title].toString(),
                ingress = ingress,
                url = map[valueMap.url].toString(),
                published = Date.from(Instant.now()),
                author = map[valueMap.author]?.toString()
            )
        }
    }
}
package io.nozemi.discordnews

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.XmlFactory
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.github.michaelbull.logging.InlineLogger
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.toResultOr
import io.nozemi.discordnews.models.NewsArticleEmbedModel
import io.nozemi.discordnews.services.NewsFetchError
import io.nozemi.discordnews.services.ServiceLoader
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

private val logger = InlineLogger()

@Service
class ServiceManager(
    val serviceLoader: ServiceLoader
) : InitializingBean {

    private fun start() {
        serviceLoader.services.forEach { service ->
            when (val result = service.fetchNews()) {
                is Ok -> push(result.component1())
                is Err -> logger.error { result.component2().message }
                else -> error("Something has gone wrong...")
            }
        }
    }

    private fun push(articles: List<NewsArticleEmbedModel>) {
        articles.forEach { logger.info { it } }
    }

    override fun afterPropertiesSet() {
        serviceLoader.readNewsSources()

        this.start()
    }
}
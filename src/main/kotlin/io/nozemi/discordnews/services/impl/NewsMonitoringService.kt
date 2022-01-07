package io.nozemi.discordnews.services.impl

import com.fasterxml.jackson.module.kotlin.readValue
import com.github.michaelbull.logging.InlineLogger
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.nozemi.discordnews.helpers.executeHttpRequest
import io.nozemi.discordnews.helpers.xmlMapper
import io.nozemi.discordnews.models.*
import io.nozemi.discordnews.models.discord.WebhookMessage
import io.nozemi.discordnews.repositories.ArticleSourcesRepository
import io.nozemi.discordnews.services.ApplicationService
import io.nozemi.discordnews.services.ServiceConfigLoader
import kotlinx.coroutines.*
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service

private val logger = InlineLogger()

@Service
class NewsMonitoringService(
    val serviceLoader: ServiceConfigLoader,
    val discordWebhookService: DiscordWebhookService,
    val articleSourcesRepository: ArticleSourcesRepository
) : ApplicationService {

    override suspend fun start(scope: CoroutineScope) {
        logger.info { "Hello from news monitoring!" }
        serviceLoader.services.forEach {
            scope.launch {
                while (true) {
                    logger.info { "Hello from monitoring service instance: ${it.feedSourceUrl}" }
                    it.publishNews(this@NewsMonitoringService)
                    delay(it.fetchInterval * 1000 * 60)
                }
            }
        }
    }

    private suspend fun NewsServiceModel.fetchNews(): Result<List<NewsArticleEmbedModel>, NewsFetchError> {
        logger.trace { "Preparing request to $feedSourceUrl..." }
        return when (val responseBody = executeHttpRequest<String>(feedSourceUrl, HttpMethod.GET)) {
            is Ok -> {
                withContext(Dispatchers.IO) {
                    val response = responseBody.component1().first ?: return@withContext Err(NewsFetchError.REQUEST_FAILED)
                    val parsedXml = withContext(Dispatchers.IO) {
                        xmlMapper().readValue<XmlRoot>(response)
                    }
                    logger.trace { "Parsed response body to XmlRoot...\n$parsedXml" }

                    val articles = withContext(Dispatchers.IO) {
                        parsedXml.parseArticles(valueMap)
                    }
                    logger.debug { "Found ${articles.size} articles..." }

                    when {
                        articles.isEmpty() -> Err(NewsFetchError.NO_ARTICLES_FOUND)
                        else -> Ok(articles)
                    }
                }
            }
            else -> Err(NewsFetchError.REQUEST_FAILED)
        }
    }

    private suspend fun NewsServiceModel.publishNews(monitor: NewsMonitoringService) {
        val alreadySentMessages = withContext(Dispatchers.IO) {
            monitor.articleSourcesRepository.findFirstByIdentifier(this@publishNews.feedSourceUrl)
        }.articles
        when (val result = this.fetchNews()) {
            is Ok -> {
                withContext(Dispatchers.IO) {
                    result.component1()
                        .filter { article ->
                            if (alreadySentMessages.any { it.identifier == article.guid }) {
                                logger.debug { "Skipping article, it has already been sent: $article" }
                            }
                            alreadySentMessages.none { it.identifier == article.guid }
                        }
                        .windowed(1)
                        .forEach {
                            val articleSource = monitor.articleSourcesRepository.findFirstByIdentifier(this@publishNews.feedSourceUrl)
                            articleSource.articles.addAll(it.map { article -> SubmittedArticleModel(identifier = article.guid) })
                            monitor.articleSourcesRepository.save(articleSource)

                            monitor.discordWebhookService.queue(
                                WebhookMessage.from(it, this@publishNews.discordWebhookConfig)
                            )
                        }
                }
            }
            is Err -> logger.error { result.component2().message }
            else -> error("Something went terribly wrong!")
        }
    }
}
package io.nozemi.discordnews.services.impl

import com.github.michaelbull.logging.InlineLogger
import com.github.michaelbull.result.Ok
import io.nozemi.discordnews.helpers.executeHttpRequest
import io.nozemi.discordnews.models.discord.WebhookMessage
import io.nozemi.discordnews.services.ApplicationService
import kotlinx.coroutines.*
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import java.util.*

private val logger = InlineLogger()

@Service
class DiscordWebhookService(
    private val activeRateLimits: MutableMap<String, WebhookRateLimit> = mutableMapOf(),
    private val messageQueue: Queue<WebhookMessage> = LinkedList()
) : ApplicationService {

    suspend fun queue(message: WebhookMessage) {
        logger.debug { "Added message to queue $message" }
        messageQueue.add(message)
    }

    private fun calculateDelay(): Long {
        val message = messageQueue.first()

        val url = "https://discord.com/api/webhooks/${message.config.id}/${message.config.token}"

        val activeRateLimit = activeRateLimits[url]
        if (activeRateLimit != null && activeRateLimit.reset > System.currentTimeMillis() / 1000L && activeRateLimit.remaining < 2) {
            val delay = activeRateLimit.reset - System.currentTimeMillis() / 1000L
            logger.debug { "Delaying message for $delay seconds." }
            return delay * 1000
        }

        return 250
    }

    private suspend fun processMessageQueue(scope: CoroutineScope) {
        withContext(Dispatchers.IO) {
            scope.launch {
                while (messageQueue.isNotEmpty()) {
                    delay(calculateDelay())

                    if (messageQueue.isEmpty()) return@launch
                    val message = messageQueue.remove() ?: return@launch

                    val url = "https://discord.com/api/webhooks/${message.config.id}/${message.config.token}"

                    if (message.isValid) {
                        val headers = HttpHeaders()
                        headers.contentType = MediaType.APPLICATION_JSON
                        val entity = HttpEntity<WebhookMessage>(message, headers)
                        logger.debug { "Sending message to Discord!" }
                        val result = executeHttpRequest<Any>(url, HttpMethod.POST, entity)
                        if (result is Ok) {
                            val responseHeaders = result.component1().second
                            activeRateLimits[url] = WebhookRateLimit(
                                limit = responseHeaders["x-ratelimit-limit"]!!.last().toInt(),
                                remaining = responseHeaders["x-ratelimit-remaining"]!!.last().toInt(),
                                reset = responseHeaders["x-ratelimit-reset"]!!.last().toLong(),
                                resetAfter = responseHeaders["x-ratelimit-reset-after"]!!.last().toInt()
                            )
                        }
                    } else {
                        logger.error { "Message is invalid: $message" }
                    }
                }
            }
        }
    }

    override suspend fun start(scope: CoroutineScope) {
        scope.launch {
            while (true) {
                logger.info { "Hello from Discord webhook service! We have ${messageQueue.size} messages to process at the moment!" }
                delay(1000)
                processMessageQueue(this)
            }
        }
    }
}

data class WebhookRateLimit(
    val limit: Int,
    val remaining: Int,
    val reset: Long,
    val resetAfter: Int
)
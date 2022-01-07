package io.nozemi.discordnews.helpers

import com.github.michaelbull.logging.InlineLogger
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.nio.charset.Charset

enum class HttpRequestError {
    HTTP_REQUEST_FAILED,
    HTTP_ACCESS_DENIED,
    HTTP_SERVER_ERROR,
    HTTP_EMPTY_RESPONSE
}

suspend inline fun <reified T> executeHttpRequest(
    url: String,
    method: HttpMethod = HttpMethod.GET,
    entity: HttpEntity<*>? = null
): Result<Pair<T?, HttpHeaders>, HttpRequestError> = with(RestTemplate()) {
    val response = withContext(Dispatchers.IO) {
        this@with.messageConverters.add(StringHttpMessageConverter(Charset.forName("UTF-8")))
        this@with.exchange<T>(url, method, entity)
    }

    when {
        response.statusCode.is4xxClientError -> return Err(HttpRequestError.HTTP_ACCESS_DENIED)
        response.statusCode.is5xxServerError -> return Err(HttpRequestError.HTTP_SERVER_ERROR)
        response.statusCode.isError -> return Err(HttpRequestError.HTTP_REQUEST_FAILED)
        !response.statusCode.is2xxSuccessful -> return Err(HttpRequestError.HTTP_REQUEST_FAILED)
    }

    return if (response.body == null && response.statusCodeValue != 204) {
        InlineLogger().error { "Response body was empty for request to: $url..." }
        Err(HttpRequestError.HTTP_EMPTY_RESPONSE)
    } else {
        InlineLogger().trace { "Successfully got response from ${url}:\n${response.body ?: ""}" }
        Ok(Pair(response.body, response.headers))
    }
}

package io.nozemi.discordnews.helpers

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.github.michaelbull.logging.InlineLogger
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import org.springframework.http.HttpMethod
import org.springframework.http.converter.StringHttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.nio.charset.Charset
import org.springframework.web.client.exchange
import com.github.michaelbull.result.Result

enum class HttpRequestError {
    HTTP_REQUEST_FAILED,
    HTTP_ACCESS_DENIED,
    HTTP_SERVER_ERROR,
    HTTP_EMPTY_RESPONSE
}

inline fun <reified T> executeHttpRequest(
    url: String,
    method: HttpMethod = HttpMethod.GET
): Result<T, HttpRequestError> = with(RestTemplate()) {
    this.messageConverters.add(StringHttpMessageConverter(Charset.forName("UTF-8")))
    val response = this.exchange<T>(url, method)

    when {
        response.statusCode.is4xxClientError -> return Err(HttpRequestError.HTTP_ACCESS_DENIED)
        response.statusCode.is5xxServerError -> return Err(HttpRequestError.HTTP_SERVER_ERROR)
        response.statusCode.isError -> return Err(HttpRequestError.HTTP_REQUEST_FAILED)
        !response.statusCode.is2xxSuccessful -> return Err(HttpRequestError.HTTP_REQUEST_FAILED)
    }

    return if (response.body == null) {
        InlineLogger().error { "Response body was empty for request to: $url..." }
        Err(HttpRequestError.HTTP_EMPTY_RESPONSE)
    } else {
        InlineLogger().debug { "Successfully got response from ${url}:\n${response.body}" }
        Ok(response.body!!)
    }
}

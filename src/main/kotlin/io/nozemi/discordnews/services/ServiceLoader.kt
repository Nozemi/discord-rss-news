package io.nozemi.discordnews.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.readValue
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import org.springframework.stereotype.Component
import java.io.File

@Component
class ServiceLoader(
    val services: MutableList<NewsService> = mutableListOf()
) {

    private val yamlMapper = ObjectMapper(YAMLFactory())
        .findAndRegisterModules()
        .registerKotlinModule()
        .enable(SerializationFeature.INDENT_OUTPUT)

    fun readNewsSources() {
        File("./data/sources").walkTopDown()
            .filter { it.isFile }
            .forEach { services.add(yamlMapper.readValue(it)) }
    }
}
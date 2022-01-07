package io.nozemi.discordnews.services

import com.fasterxml.jackson.module.kotlin.readValue
import io.nozemi.discordnews.helpers.yamlMapper
import io.nozemi.discordnews.models.ArticleSourceModel
import io.nozemi.discordnews.models.NewsServiceModel
import io.nozemi.discordnews.repositories.ArticleSourcesRepository
import org.springframework.stereotype.Component
import java.io.File

@Component
class ServiceConfigLoader(
    val services: MutableList<NewsServiceModel> = mutableListOf(),
    val articleSourcesRepository: ArticleSourcesRepository
) {
    fun loadSourceConfigurations() {
        File("./data/sources/enabled").walkTopDown()
            .filter { it.isFile }
            .forEach {
                val articleSource: NewsServiceModel = yamlMapper().readValue(it)
                services.add(articleSource)
                articleSourcesRepository.save(ArticleSourceModel(
                    identifier = articleSource.feedSourceUrl
                ))
            }
    }
}
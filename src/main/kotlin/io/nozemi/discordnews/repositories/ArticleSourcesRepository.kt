package io.nozemi.discordnews.repositories

import io.nozemi.discordnews.models.ArticleSourceModel
import io.nozemi.discordnews.models.SubmittedArticleModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleSourcesRepository : JpaRepository<ArticleSourceModel, Int> {
    fun findFirstByIdentifier(identifier: String): ArticleSourceModel
    fun findFirstByArticlesContains(article: SubmittedArticleModel): ArticleSourceModel
}
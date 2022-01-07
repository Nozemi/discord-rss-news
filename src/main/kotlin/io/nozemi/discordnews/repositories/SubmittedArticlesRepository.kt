package io.nozemi.discordnews.repositories

import io.nozemi.discordnews.models.SubmittedArticleModel
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SubmittedArticlesRepository : JpaRepository<SubmittedArticleModel, Int>
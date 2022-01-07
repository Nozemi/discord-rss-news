package io.nozemi.discordnews.models

import javax.persistence.*

@Entity
class ArticleSourceModel(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,
    val identifier: String = "",
    @OneToMany(cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val articles: MutableList<SubmittedArticleModel> = mutableListOf()
)
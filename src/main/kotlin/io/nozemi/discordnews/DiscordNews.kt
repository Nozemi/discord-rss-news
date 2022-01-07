package io.nozemi.discordnews

import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder

@SpringBootApplication
class DiscordNews

fun main(args: Array<String>) {
    SpringApplicationBuilder(DiscordNews::class.java)
        .web(WebApplicationType.NONE)
        .run(*args)
}
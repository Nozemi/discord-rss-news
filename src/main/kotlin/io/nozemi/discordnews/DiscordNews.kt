package io.nozemi.discordnews

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class DiscordNews

fun main(args: Array<String>) {
    runApplication<DiscordNews>(*args)
}
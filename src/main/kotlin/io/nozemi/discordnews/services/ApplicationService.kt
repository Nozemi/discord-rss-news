package io.nozemi.discordnews.services

import kotlinx.coroutines.CoroutineScope
import org.springframework.stereotype.Service

@Service
interface ApplicationService {
    suspend fun start(scope: CoroutineScope)
}
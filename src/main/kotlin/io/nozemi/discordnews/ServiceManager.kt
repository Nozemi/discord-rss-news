package io.nozemi.discordnews

import com.github.michaelbull.logging.InlineLogger
import io.nozemi.discordnews.services.ApplicationService
import io.nozemi.discordnews.services.ServiceConfigLoader
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.runBlocking
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Service

private val logger = InlineLogger()

class MonitoringThread(val execute: () -> Unit) : Thread() {

    override fun run() {
        while (true) {
            execute()
        }
    }
}

@Service
class ServiceManager(
    val serviceLoader: ServiceConfigLoader,
    val services: List<ApplicationService>
) : InitializingBean {

    @OptIn(DelicateCoroutinesApi::class)
    private fun start() {
        runBlocking {
            services.forEach {
                launch(newSingleThreadContext(it.javaClass.name)) {
                    it.start(this)
                }
            }
        }
    }

    override fun afterPropertiesSet() {
        serviceLoader.loadSourceConfigurations()
        this.start()
    }
}
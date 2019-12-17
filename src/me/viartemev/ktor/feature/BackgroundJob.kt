package me.viartemev.ktor.me.viartemev.ktor.feature

import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.launch
import mu.KotlinLogging
import java.io.Closeable
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

@KtorExperimentalAPI
class BackgroundJob(configuration: JobConfiguration) : Closeable {
    private val job = configuration.job
    private val name = configuration.name

    class JobConfiguration {
        var name: String? = null
        var job: ClosableJob? = null
        var startOnSeparateThread = false
    }

    object BackgroundJobFeature : ApplicationFeature<Application, JobConfiguration, BackgroundJob> {
        override val key: AttributeKey<BackgroundJob> = AttributeKey("BackgroundJob")

        override fun install(pipeline: Application, configure: JobConfiguration.() -> Unit): BackgroundJob {
            val configuration = JobConfiguration().apply(configure)
            val backgroundJob = BackgroundJob(configuration)
            if (configuration.startOnSeparateThread) {
                configuration.job?.let { thread(name = configuration.name) { it.run() } }
            } else {
                pipeline.launch { backgroundJob.job?.run() }
            }
            return backgroundJob
        }
    }

    override fun close() {
        logger.info { "Closing $name job" }
        job?.close()
        logger.info { "Job $name closed" }
    }
}

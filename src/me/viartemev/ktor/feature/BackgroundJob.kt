package me.viartemev.ktor.me.viartemev.ktor.feature

import io.ktor.application.Application
import io.ktor.application.ApplicationFeature
import io.ktor.util.AttributeKey
import io.ktor.util.KtorExperimentalAPI
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
    }

    object BackgroundJobFeature : ApplicationFeature<Application, JobConfiguration, BackgroundJob> {
        override val key: AttributeKey<BackgroundJob> = AttributeKey("BackgroundJob")

        override fun install(pipeline: Application, configure: JobConfiguration.() -> Unit): BackgroundJob {
            val configuration = JobConfiguration().apply(configure)
            val backgroundJob = BackgroundJob(configuration)
            configuration.job?.let { thread(name = configuration.name) { it.run() } }
            return backgroundJob
        }
    }

    override fun close() {
        logger.info { "Closing $name job" }
        job?.close()
        logger.info { "Job $name closed" }
    }
}

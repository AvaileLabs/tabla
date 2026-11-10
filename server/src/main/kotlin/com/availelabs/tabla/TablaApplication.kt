package com.availelabs.tabla

import com.availelabs.tabla.ingestion.APP_TIME_ZONE_ID
import com.availelabs.tabla.ingestion.IngestionJobs
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@SpringBootApplication(proxyBeanMethods = false)
@ConfigurationPropertiesScan
@EnableScheduling
class TablaApplication

fun main(args: Array<String>) {
    runApplication<TablaApplication>(*args)
}

@Component
class ApplicationLifecycle(
    private val ingestion: IngestionJobs
) : ApplicationRunner {
    override fun run(args: ApplicationArguments) =
        ingestion.ingestNow()

    @Scheduled(cron = "@daily", zone = APP_TIME_ZONE_ID)
    fun runDailyTasks() =
        ingestion.scheduleIngestionForToday()
}

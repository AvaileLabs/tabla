package com.availelabs.tabla.ingestion.internal

import com.availelabs.tabla.ingestion.IngestionPipeline
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.security.SecureRandom
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaInstant

@Component
class DailyIngestionScheduler(
    private val pipeline: IngestionPipeline,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    private val taskScheduler: TaskScheduler
) {
    private val random = SecureRandom()

    @Scheduled(cron = "@midnight", zone = "America/New_York")
    fun runDaily() {
        val today = Clock.System.todayIn(APP_TIME_ZONE)
        val start = today.atStartOfDayIn(APP_TIME_ZONE)
        val end = today.atTime(5, 0).toInstant(APP_TIME_ZONE)

        val scheduledAt = start +
                random.nextLong((end - start).inWholeMilliseconds).milliseconds

        taskScheduler.schedule(
            { pipeline.run() },
            scheduledAt.toJavaInstant()
        )
    }
}

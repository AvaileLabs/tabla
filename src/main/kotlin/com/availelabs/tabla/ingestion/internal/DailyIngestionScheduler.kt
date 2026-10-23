package com.availelabs.tabla.ingestion.internal

import com.availelabs.tabla.ingestion.APP_TIME_ZONE
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.todayIn
import java.security.SecureRandom
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class DailyIngestionScheduler(
    private val scheduleRun: (Instant) -> Unit
) {
    private val random = SecureRandom()

    fun scheduleToday() {
        val today = Clock.System.todayIn(APP_TIME_ZONE)
        val start = today.atStartOfDayIn(APP_TIME_ZONE)
        val end = today.atTime(5, 0).toInstant(APP_TIME_ZONE)

        val scheduledAt = start +
                random.nextLong((end - start).inWholeMilliseconds).milliseconds

        scheduleRun(scheduledAt)
    }
}

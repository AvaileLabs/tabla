package com.availelabs.tabla.nasdaq

import com.availelabs.tabla.nasdaq.persistence.NasdaqListingRepository
import kotlinx.datetime.*
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.security.SecureRandom
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.toJavaInstant

@Component
class NasdaqDirectoryIngestionJob(
    private val ingestor: NasdaqDirectoryIngestor,
    private val repository: NasdaqListingRepository,
    @Suppress("SpringJavaInjectionPointsAutowiringInspection") // file report later
    private val taskScheduler: TaskScheduler
) : ApplicationRunner {

    private val zone = TimeZone.of("America/New_York")
    private val random = SecureRandom()

    override fun run(args: ApplicationArguments) = ingest()

    @Scheduled(cron = "@midnight", zone = "America/New_York")
    fun runDaily() {
        val today = Clock.System.todayIn(zone)
        val start = today.atStartOfDayIn(zone)
        val end = today.atTime(5, 0).toInstant(zone)
        val scheduledAt = start + random.nextLong((end - start).inWholeMilliseconds).milliseconds

        taskScheduler.schedule({ ingest() }, scheduledAt.toJavaInstant())
    }

    private fun ingest() {
        val retrievedAt = repository.getRetrievalTimeOfNasdaqDirectoryOrNull()
        val lastRetrievedDate = retrievedAt?.toLocalDateTime(zone)?.date
        if (lastRetrievedDate == Clock.System.todayIn(zone)) return

        repository.replaceNasdaqDirectory(ingestor.ingest())
    }
}

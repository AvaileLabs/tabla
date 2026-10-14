package com.availelabs.tabla.securities

import com.availelabs.tabla.securities.persistence.SecurityListingRepository
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
class NasdaqSecurityDirectoryIngestionJob(
    private val ingestor: NasdaqSecurityDirectoryIngestor,
    private val repository: SecurityListingRepository,
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

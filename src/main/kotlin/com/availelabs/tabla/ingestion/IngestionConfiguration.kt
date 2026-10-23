package com.availelabs.tabla.ingestion

import com.availelabs.tabla.ingestion.edgar.EdgarProperties
import com.availelabs.tabla.ingestion.internal.DailyIngestionScheduler
import com.availelabs.tabla.ingestion.internal.IngestionPipeline
import com.availelabs.tabla.ingestion.snapshot.SourceSnapshotRepository
import com.availelabs.tabla.ingestion.snapshot.SourceSnapshotRetentionJob
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import kotlin.time.toJavaInstant

@Configuration(proxyBeanMethods = false)
class IngestionConfiguration {

    @Bean
    fun ingestionJobs(
        snapshots: SourceSnapshotRepository,
        edgarProperties: EdgarProperties,
        @Suppress("SpringJavaInjectionPointsAutowiringInspection")
        taskScheduler: TaskScheduler
    ): IngestionJobs {
        val pipeline = IngestionPipeline(snapshots, edgarProperties)
        val dailyIngestion = DailyIngestionScheduler { scheduledAt ->
            taskScheduler.schedule(pipeline::run, scheduledAt.toJavaInstant())
        }
        val retention = SourceSnapshotRetentionJob(snapshots)

        return IngestionJobs(pipeline, dailyIngestion, retention)
    }
}

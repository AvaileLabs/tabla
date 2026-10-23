package com.availelabs.tabla.ingestion

import com.availelabs.tabla.ingestion.internal.DailyIngestionScheduler
import com.availelabs.tabla.ingestion.internal.IngestionPipeline
import com.availelabs.tabla.ingestion.snapshot.SourceSnapshotRetentionJob

class IngestionJobs internal constructor(
    private val pipeline: IngestionPipeline,
    private val dailyIngestion: DailyIngestionScheduler,
    private val retention: SourceSnapshotRetentionJob
) {
    fun ingestNow() {
        pipeline.run()
    }

    fun scheduleIngestionForToday() {
        dailyIngestion.scheduleToday()
    }

    fun retainSnapshots() {
        retention.run()
    }
}

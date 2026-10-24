package com.availelabs.tabla.ingestion

import com.availelabs.tabla.ingestion.internal.DailyIngestionScheduler
import com.availelabs.tabla.ingestion.internal.IngestionPipeline
import org.springframework.stereotype.Service

@Service
class IngestionJobs internal constructor(
    private val pipeline: IngestionPipeline,
    private val dailyIngestion: DailyIngestionScheduler
) {
    fun ingestNow() {
        pipeline.run()
    }

    fun scheduleIngestionForToday() {
        dailyIngestion.scheduleToday()
    }
}

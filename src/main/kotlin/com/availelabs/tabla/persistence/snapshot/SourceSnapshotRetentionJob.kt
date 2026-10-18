package com.availelabs.tabla.persistence.snapshot

import com.availelabs.tabla.SourceCatalog
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SourceSnapshotRetentionJob(
    private val repository: SourceSnapshotRepository
) {
    private val logger = LoggerFactory.getLogger(SourceSnapshotRetentionJob::class.java)

    @Scheduled(cron = "@daily")
    fun runDaily() {
        for (sourceUri in repository.sourceUris()) {
            val keep = when (sourceUri) {
                SourceCatalog.nasdaqDirectory -> 365
                SourceCatalog.edgarCompanyTickers -> 10
                SourceCatalog.edgarMutualFundTickers -> 10
                else -> {
                    logger.warn("No snapshot retention policy for {}", sourceUri)
                    continue
                }
            }

            repository.retainLatestOf(sourceUri, keep)
        }
    }
}
package com.availelabs.tabla.persistence.snapshot

import com.availelabs.tabla.SourceCatalog
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class SourceSnapshotRetentionJob(
    private val repository: SourceSnapshotRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val policies = mapOf(
        SourceCatalog.nasdaqDirectory.uri to 365,
        SourceCatalog.edgarCompanyTickers.uri to 10,
        SourceCatalog.edgarMutualFundTickers.uri to 10
    )

    @Scheduled(cron = "@daily")
    fun runDaily() {
        for (sourceUri in repository.sourceUris()) {
            val keep = policies[sourceUri] ?: run {
                logger.warn("No snapshot retention policy for {}", sourceUri)
                continue
            }

            repository.retainLatestOf(sourceUri, keep)
        }
    }
}
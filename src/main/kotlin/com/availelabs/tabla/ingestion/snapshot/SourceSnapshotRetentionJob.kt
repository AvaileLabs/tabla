package com.availelabs.tabla.ingestion.snapshot

import com.availelabs.tabla.ingestion.source.SourceCatalog
import kotlinx.collections.immutable.persistentHashMapOf
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SourceSnapshotRetentionJob(
    private val repository: SourceSnapshotRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val policies = persistentHashMapOf(
        SourceCatalog.nasdaqDirectory.uri to 365,
        SourceCatalog.edgarCompanyTickers.uri to 10,
        SourceCatalog.edgarMutualFundTickers.uri to 10
    )

    fun run() {
        for (sourceUri in repository.sourceUris()) {
            val keep = policies[sourceUri] ?: run {
                logger.warn("No snapshot retention policy for {}", sourceUri)
                continue
            }

            repository.retainLatestOf(sourceUri, keep)
        }
    }
}

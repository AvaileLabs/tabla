package com.availelabs.tabla.ingestion.internal.snapshot

import com.availelabs.tabla.ingestion.internal.source.SourceCatalog
import kotlinx.collections.immutable.persistentHashMapOf
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class SourceSnapshotRetention(
    private val repository: SourceSnapshotRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val policies = persistentHashMapOf(
        SourceCatalog.nasdaqDirectory.uri to 45,
        SourceCatalog.edgarCompanyTickers.uri to 1,
        SourceCatalog.edgarMutualFundTickers.uri to 1
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

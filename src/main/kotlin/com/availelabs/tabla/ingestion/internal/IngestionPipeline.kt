package com.availelabs.tabla.ingestion.internal

import com.availelabs.tabla.ingestion.edgar.EdgarTickerMappingIngestor
import com.availelabs.tabla.ingestion.nasdaq.NasdaqDirectoryIngestor
import com.availelabs.tabla.ingestion.reconciliation.EdgarNasdaqListingReconciler
import com.availelabs.tabla.ingestion.snapshot.SourceSnapshotRepository
import com.availelabs.tabla.ingestion.snapshot.SourceSnapshotRetention
import com.availelabs.tabla.ingestion.source.SnapshotSource
import com.availelabs.tabla.ingestion.source.SourceCatalog
import com.availelabs.tabla.ingestion.source.SourceFile
import com.availelabs.tabla.provenance.Sourced
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class IngestionPipeline(
    private val repository: SourceSnapshotRepository,
    private val retention: SourceSnapshotRetention,
    private val nasdaqIngestor: NasdaqDirectoryIngestor,
    private val edgarIngestor: EdgarTickerMappingIngestor,
    private val tickerReconciliation: EdgarNasdaqListingReconciler
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun run() {
        try {
            ingest(nasdaqIngestor.source, nasdaqIngestor::download)
            ingest(edgarIngestor.companySource, edgarIngestor::downloadCompanies)
            ingest(edgarIngestor.fundSource, edgarIngestor::downloadFunds)

            val rawNasdaq = checkNotNull(repository.latestOf(SourceCatalog.nasdaqDirectory))
            { "NASDAQ snapshot must exist before reconciliation" }
            val rawEdgar = checkNotNull(repository.latestOf(SourceCatalog.edgarCompanyTickers))
            { "SEC EDGAR snapshot must exist before reconciliation" }

            val nasdaq = nasdaqIngestor.parse(rawNasdaq)
            val edgar = edgarIngestor.ingestCompanies(rawEdgar)

            val _ = tickerReconciliation.reconcile(nasdaq, edgar)
        } finally {
            try {
                retention.run()
            } catch (exception: Exception) {
                logger.warn("Snapshot retention failed", exception)
            }
        }
    }

    private fun <T : SourceFile> ingest(source: SnapshotSource<T>, download: () -> Sourced<T>) {
        if (repository.wasRetrievedToday(source)) return

        val file = download()
        repository.save(source, file)
    }
}

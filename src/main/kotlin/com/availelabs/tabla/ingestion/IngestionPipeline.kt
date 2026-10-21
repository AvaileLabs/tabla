package com.availelabs.tabla.ingestion

import com.availelabs.tabla.ingestion.edgar.EdgarProperties
import com.availelabs.tabla.ingestion.edgar.EdgarTickerMappingIngestor
import com.availelabs.tabla.ingestion.internal.*
import com.availelabs.tabla.ingestion.nasdaq.NasdaqDirectoryIngestor
import com.availelabs.tabla.ingestion.reconciliation.EdgarNasdaqTickerReconciliation
import com.availelabs.tabla.ingestion.snapshot.SourceSnapshotRepository
import com.availelabs.tabla.ingestion.source.SourceCatalog
import com.availelabs.tabla.ingestion.source.SourceFile
import org.springframework.stereotype.Component

@Component
class IngestionPipeline(
    private val repository: SourceSnapshotRepository,
    edgarProperties: EdgarProperties
) {
    private val nasdaqIngestor = NasdaqDirectoryIngestor()
    private val edgarIngestor = EdgarTickerMappingIngestor(edgarProperties)
    private val registry = IngestionRegistry(nasdaqIngestor, edgarIngestor)
    private val tickerReconciliation = EdgarNasdaqTickerReconciliation()

    fun run() {
        registry.targets.forEach { ingest(it) }

        val rawNasdaq = checkNotNull(repository.latestOf(SourceCatalog.nasdaqDirectory))
        { "NASDAQ snapshot must exist before reconciliation" }
        val rawEdgar = checkNotNull(repository.latestOf(SourceCatalog.edgarCompanyTickers))
        { "SEC EDGAR snapshot must exist before reconciliation" }

        val nasdaq = nasdaqIngestor.parse(rawNasdaq)
        val edgar = edgarIngestor.ingestCompanies(rawEdgar)

        tickerReconciliation.reconcile(nasdaq, edgar)
    }

    private fun <T : SourceFile> ingest(target: IngestionTarget<T>) {
        if (repository.wasRetrievedToday(target.source)) return

        val file = target.download()
        repository.save(target.source, file)
    }
}

package com.availelabs.tabla.ingestion.internal

import com.availelabs.tabla.ingestion.edgar.EdgarTickerMappingIngestor
import com.availelabs.tabla.ingestion.nasdaq.NasdaqDirectoryIngestor
import com.availelabs.tabla.ingestion.source.SnapshotSource
import com.availelabs.tabla.ingestion.source.SourceFile
import com.availelabs.tabla.provenance.Sourced

data class IngestionTarget<T : SourceFile>(
    val source: SnapshotSource<T>,
    val download: () -> Sourced<T>
)

class IngestionRegistry(
    nasdaq: NasdaqDirectoryIngestor,
    edgar: EdgarTickerMappingIngestor
) {
    val targets: List<IngestionTarget<*>> = listOf(
        IngestionTarget(nasdaq.source, nasdaq::download),
        IngestionTarget(edgar.companySource, edgar::downloadCompanies),
        IngestionTarget(edgar.fundSource, edgar::downloadFunds)
    )
}

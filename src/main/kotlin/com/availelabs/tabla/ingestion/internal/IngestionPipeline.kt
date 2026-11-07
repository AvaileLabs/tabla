package com.availelabs.tabla.ingestion.internal

import com.availelabs.tabla.ingestion.internal.providers.edgar.EdgarTickerMappingIngestor
import com.availelabs.tabla.ingestion.internal.providers.nasdaq.NasdaqDirectoryIngestor
import com.availelabs.tabla.ingestion.internal.snapshot.SourceSnapshotRepository
import com.availelabs.tabla.ingestion.internal.snapshot.SourceSnapshotRetention
import com.availelabs.tabla.ingestion.internal.source.SnapshotSource
import com.availelabs.tabla.ingestion.internal.source.SourceFile
import com.availelabs.tabla.market.SecurityListingDataRepository
import com.availelabs.tabla.provenance.Sourced
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Service
class IngestionPipeline(
    private val repository: SourceSnapshotRepository,
    private val retention: SourceSnapshotRetention,
    private val nasdaqIngestor: NasdaqDirectoryIngestor,
    private val edgarIngestor: EdgarTickerMappingIngestor,
    private val securityListingDataRepository: SecurityListingDataRepository
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun run() {
        val nasdaqSnapshot = downloadIfStale(nasdaqIngestor.source, nasdaqIngestor::download)
        val companiesSnapshot = downloadIfStale(edgarIngestor.companySource, edgarIngestor::downloadCompanies)
        val fundsSnapshot = downloadIfStale(edgarIngestor.fundSource, edgarIngestor::downloadFunds)

        if (nasdaqSnapshot == null) return
        val nasdaqDf = nasdaqIngestor.parse(nasdaqSnapshot).value
        if (companiesSnapshot == null) return
        val companiesDf = edgarIngestor.ingestCompanies(companiesSnapshot).value
        if (fundsSnapshot == null) return
        val fundsDf = edgarIngestor.ingestMutualFunds(fundsSnapshot).value


    }

    private fun <T : SourceFile> downloadIfStale(
        source: SnapshotSource<T>,
        download: () -> Sourced<T>
    ): Sourced<T>? {
        if (repository.wasRetrievedToday(source)) {
            return repository.latestOf(source)
                ?: error("Snapshot missing: ${source.uri}")
        }

        val file = try {
            download()
        } catch (e: Exception) {
            val message = when (e) {
                is UnknownHostException -> "DNS resolution failed"
                is ConnectException -> "Website unreachable"
                is SocketTimeoutException -> "Website timed out"
                else -> "Snapshot download failed"
            }

            logger.error("$message: ${source.uri}", e)
            return null
        }

        repository.save(source, file)
        return file
    }
}

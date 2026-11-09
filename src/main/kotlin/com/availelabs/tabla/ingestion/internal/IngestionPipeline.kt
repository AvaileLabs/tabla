package com.availelabs.tabla.ingestion.internal

import com.availelabs.tabla.ingestion.internal.providers.edgar.EdgarTickerMappingIngestor
import com.availelabs.tabla.ingestion.internal.providers.nasdaq.NasdaqDirectoryIngestor
import com.availelabs.tabla.ingestion.internal.snapshot.SourceSnapshotRepository
import com.availelabs.tabla.ingestion.internal.source.SnapshotSource
import com.availelabs.tabla.ingestion.internal.source.SourceFile
import com.availelabs.tabla.market.nasdaq.NasdaqDirectoryWriter
import com.availelabs.tabla.market.sec.company.SecCompanyTickerWriter
import com.availelabs.tabla.market.sec.fund.FundShareClassWriter
import com.availelabs.tabla.provenance.Sourced
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

@Service
class IngestionPipeline(
    private val repository: SourceSnapshotRepository,
    private val nasdaqIngestor: NasdaqDirectoryIngestor,
    private val edgarIngestor: EdgarTickerMappingIngestor,
    private val nasdaqDirectory: NasdaqDirectoryWriter,
    private val companyTickers: SecCompanyTickerWriter,
    private val fundShareClasses: FundShareClassWriter
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun run() {
        refresh(nasdaqIngestor.source, nasdaqIngestor::download) {
            nasdaqDirectory.replaceAll(nasdaqIngestor.parse(it))
        }
        refresh(edgarIngestor.companySource, edgarIngestor::downloadCompanies) {
            companyTickers.replaceAll(edgarIngestor.ingestCompanies(it))
        }
        refresh(edgarIngestor.fundSource, edgarIngestor::downloadFunds) {
            fundShareClasses.replaceAll(edgarIngestor.ingestMutualFunds(it))
        }
    }

    private fun <T : SourceFile> refresh(
        source: SnapshotSource<T>,
        download: () -> Sourced<T>,
        ingest: (Sourced<T>) -> Unit
    ) {
        try {
            ingest(downloadIfStale(source, download))
            logger.info("Refreshed market data from {}", source.uri)
        } catch (e: InterruptedException) {
            Thread.currentThread().interrupt()
            throw e
        } catch (e: Exception) {
            val message = when (e) {
                is UnknownHostException -> "DNS resolution failed"
                is ConnectException -> "Website unreachable"
                is SocketTimeoutException -> "Website timed out"
                else -> "Source refresh failed"
            }

            logger.error("$message: ${source.uri}; previous market data retained", e)
        }
    }

    private fun <T : SourceFile> downloadIfStale(
        source: SnapshotSource<T>,
        download: () -> Sourced<T>
    ): Sourced<T> {
        if (repository.wasRetrievedToday(source))
            return repository.latestOf(source) ?: error("Snapshot missing: ${source.uri}")

        val file = download()
        repository.save(source, file)
        return file
    }
}

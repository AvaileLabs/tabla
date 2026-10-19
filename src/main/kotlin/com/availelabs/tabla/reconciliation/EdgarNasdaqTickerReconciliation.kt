package com.availelabs.tabla.reconciliation

import com.availelabs.tabla.edgar.EdgarCompanyTickerMapping
import com.availelabs.tabla.edgar.EdgarTickerMappingIngestor
import com.availelabs.tabla.edgar.ticker
import com.availelabs.tabla.nasdaq.*
import com.availelabs.tabla.persistence.snapshot.SourceSnapshotRepository
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.count
import org.jetbrains.kotlinx.dataframe.api.innerJoinWith
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class EdgarNasdaqTickerReconciliation(
    private val snapshotRepository: SourceSnapshotRepository,
    private val nasdaqIngestor: NasdaqDirectoryIngestor,
    private val edgarIngestor: EdgarTickerMappingIngestor
) {
    @EventListener(ApplicationReadyEvent::class)
    fun reconcile() {
        val rawNasdaq = checkNotNull(
            snapshotRepository.latestOf(nasdaqIngestor.source)
        ) { "NASDAQ snapshot must exist before reconciliation" }
        val [nasdaqDf, nasdaqSourceInfo] = nasdaqIngestor.parse(rawNasdaq)

        val rawEdgar = checkNotNull(
            snapshotRepository.latestOf(edgarIngestor.companySource)
        ) { "SEC EDGAR snapshot must exist before reconciliation" }
        val [edgarDf, edgarSourceInfo] = edgarIngestor.ingestCompanies(rawEdgar)

        val exactMatches = nasdaqDf.innerJoinWith(edgarDf) { nasdaq ->
            exact(nasdaq, right)
        }

        println("Exact matches: ${exactMatches.count()}")
        println(exactMatches)
    }
}

/* file report that imports aren't suggested automatically by IDE.
import com.availelabs.tabla.nasdaq.cqsSymbol
import com.availelabs.tabla.nasdaq.nasdaqSymbol
import com.availelabs.tabla.nasdaq.symbol
import com.availelabs.tabla.edgar.ticker
 */
private typealias Rule = (
    DataRow<NasdaqTradedSecurity>,
    DataRow<EdgarCompanyTickerMapping>
) -> Boolean

private val exact: Rule = { nasdaq, edgar ->
    nasdaq.symbolAliases().any { it == edgar.ticker }
}

private fun DataRow<NasdaqTradedSecurity>.symbolAliases() =
    sequenceOf(symbol, cqsSymbol, nasdaqSymbol)

private val rules: List<Rule> = listOf(
    exact
)

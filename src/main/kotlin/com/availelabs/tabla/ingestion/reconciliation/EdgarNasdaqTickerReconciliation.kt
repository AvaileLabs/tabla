package com.availelabs.tabla.ingestion.reconciliation

import com.availelabs.tabla.ingestion.edgar.EdgarCompanyTickerMapping
import com.availelabs.tabla.ingestion.edgar.cik
import com.availelabs.tabla.ingestion.edgar.name
import com.availelabs.tabla.ingestion.edgar.ticker
import com.availelabs.tabla.ingestion.nasdaq.*
import com.availelabs.tabla.market.Exchange
import com.availelabs.tabla.market.Listing
import com.availelabs.tabla.market.SecuritySymbols
import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.innerJoin
import org.jetbrains.kotlinx.dataframe.api.print
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.api.toDataFrame

class EdgarNasdaqTickerReconciliation {

    fun reconcile(
        nasdaq: Sourced<DataFrame<NasdaqTradedSecurity>>,
        edgar: Sourced<DataFrame<EdgarCompanyTickerMapping>>
    ): ImmutableList<Listing> {
        val nasdaqAliases = nasdaq.value.rows()
            .withIndex()
            .flatMap { [index, row] ->
                listOfNotNull(
                    row.symbol,
                    row.cqsSymbol,
                    row.nasdaqSymbol
                ).distinct().map { alias ->
                    NasdaqAlias(index, row.symbol, alias)
                }
            }
            .toDataFrame()

        val edgarTickers = edgar.value.rows()
            .withIndex()
            .map { [index, row] ->
                EdgarTicker(index, row.ticker, row.cik, row.name)
            }
            .toDataFrame()

        val matches = nasdaqAliases.innerJoin(edgarTickers) { ticker }
        matches.print()

        return matches.rows()
            .groupBy { it.nasdaqRowIndex }
            .map { [nasdaqRowIndex, tickerMatches] ->
                val security = nasdaq.value[nasdaqRowIndex]
                val ciks = tickerMatches.map { it.cik }.distinct()
                require(ciks.size == 1) {
                    "Conflicting EDGAR CIKs for ${security.symbol}: $ciks"
                }
                Listing.of(
                    cik = ciks.single(),
                    symbols = SecuritySymbols(
                        symbol = security.symbol,
                        nasdaqSymbol = security.nasdaqSymbol,
                        cqsSymbol = security.cqsSymbol
                    ),
                    name = security.securityName,
                    exchange = when (val code = security.listingExchange) {
                        'Q' -> Exchange.NASDAQ
                        'A' -> Exchange.NYSE_AMERICAN
                        'N' -> Exchange.NYSE
                        'P' -> Exchange.NYSE_ARCA
                        'Z' -> Exchange.CBOE_BZX
                        'V' -> Exchange.IEX
                        'F' -> Exchange.TXSE
                        'M' -> Exchange.NYSE_TEXAS
                        else -> error("Unknown NASDAQ listing exchange '$code' for ${security.symbol}")
                    }
                )
            }
            .toImmutableList()
    }
}

@DataSchema
data class NasdaqAlias(
    val nasdaqRowIndex: Int,
    val nasdaqSymbol: String,
    val ticker: String
)

@DataSchema
data class EdgarTicker(
    val edgarRowIndex: Int,
    val ticker: String,
    val cik: Int,
    val companyName: String
)

private data class TickerMatch(
    val nasdaqSymbol: String,
    val edgarTicker: String,
    val edgarCik: Int,
    val companyName: String,
    val matchedBy: String
)

private data class ReconciliationPair(
    val nasdaqRowIndex: Int,
    val edgarRowIndex: Int
)

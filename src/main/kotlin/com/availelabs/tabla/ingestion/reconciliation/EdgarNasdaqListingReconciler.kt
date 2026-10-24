package com.availelabs.tabla.ingestion.reconciliation

import com.availelabs.tabla.ingestion.edgar.EdgarCompanyTickerMapping
import com.availelabs.tabla.ingestion.edgar.cik
import com.availelabs.tabla.ingestion.edgar.ticker
import com.availelabs.tabla.ingestion.nasdaq.*
import com.availelabs.tabla.market.Exchange
import com.availelabs.tabla.market.SecurityListing
import com.availelabs.tabla.market.SecuritySymbols
import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.*
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.rows
import org.springframework.stereotype.Component

@Component
class EdgarNasdaqListingReconciler {
    fun reconcile(
        nasdaq: Sourced<DataFrame<NasdaqTradedSecurity>>,
        edgar: Sourced<DataFrame<EdgarCompanyTickerMapping>>
    ): ImmutableList<SecurityListing> {
        return matchListingsByExactTicker(nasdaq, edgar)
    }

    fun matchListingsByExactTicker(
        nasdaq: Sourced<DataFrame<NasdaqTradedSecurity>>,
        edgar: Sourced<DataFrame<EdgarCompanyTickerMapping>>
    ): ImmutableList<SecurityListing> {
        val ciksByTicker = indexCiksByTicker(edgar.value)

        return nasdaq.value.rows()
            .mapNotNull { security ->
                val ciks = findMatchingCiks(security, ciksByTicker)

                when (ciks.size) {
                    0 -> null
                    1 -> createListing(security, ciks.single())
                    else -> error(
                        "Conflicting EDGAR CIKs for ${security.symbol}: $ciks"
                    )
                }
            }
            .toPersistentList()
    }

    private fun indexCiksByTicker(
        edgar: DataFrame<EdgarCompanyTickerMapping>
    ): ImmutableMap<String, ImmutableSet<Int>> =
        edgar.rows()
            .groupBy(
                keySelector = { it.ticker },
                valueTransform = { it.cik }
            )
            .mapValues { it.value.toPersistentSet() }
            .toPersistentMap()

    private fun findMatchingCiks(
        security: DataRow<NasdaqTradedSecurity>,
        ciksByTicker: ImmutableMap<String, ImmutableSet<Int>>
    ): ImmutableSet<Int> =
        listOfNotNull(
            security.symbol,
            security.cqsSymbol,
            security.nasdaqSymbol
        )
            .flatMap { ciksByTicker[it].orEmpty() }
            .toPersistentSet()

    private fun createListing(security: DataRow<NasdaqTradedSecurity>, cik: Int): SecurityListing {
        return SecurityListing.of(
            cik = cik,
            symbols = SecuritySymbols(
                symbol = security.symbol,
                nasdaqSymbol = security.nasdaqSymbol,
                cqsSymbol = security.cqsSymbol
            ),
            name = security.securityName,
            exchange = when (val exchangeCode = security.listingExchange) {
                'Q' -> Exchange.NASDAQ
                'A' -> Exchange.NYSE_AMERICAN
                'N' -> Exchange.NYSE
                'P' -> Exchange.NYSE_ARCA
                'Z' -> Exchange.CBOE_BZX
                'V' -> Exchange.IEX
                'F' -> Exchange.TXSE
                'M' -> Exchange.NYSE_TEXAS
                else -> error("Unknown NASDAQ listing exchange '$exchangeCode' for ${security.symbol}")
            }
        )
    }
}

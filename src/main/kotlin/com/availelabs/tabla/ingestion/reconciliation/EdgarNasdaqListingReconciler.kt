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
    ): ImmutableList<SecurityListing> =
        matchListingsByExactTicker(nasdaq, edgar)

    private fun matchListingsByExactTicker(
        nasdaq: Sourced<DataFrame<NasdaqTradedSecurity>>,
        edgar: Sourced<DataFrame<EdgarCompanyTickerMapping>>
    ): ImmutableList<SecurityListing> {
        val mappingsByTicker = indexMappingsByTicker(edgar.value)

        return nasdaq.value.rows()
            .mapNotNull { security ->
                val matches = findMatchingMappings(security, mappingsByTicker)
                val ciks = matches.map { it.cik }.toPersistentSet()

                when (ciks.size) {
                    0 -> null
                    1 -> toListingData(security, matches.first())
                    else -> error(
                        "Conflicting EDGAR CIKs for ${security.symbol}: $ciks"
                    )
                }
            }
            .toPersistentList()
    }

    private fun indexMappingsByTicker(
        edgar: DataFrame<EdgarCompanyTickerMapping>
    ): ImmutableMap<String, ImmutableList<DataRow<EdgarCompanyTickerMapping>>> =
        edgar.rows()
            .groupBy { it.ticker }
            .mapValues { it.value.toPersistentList() }
            .toPersistentMap()

    private fun findMatchingMappings(
        security: DataRow<NasdaqTradedSecurity>,
        mappingsByTicker: ImmutableMap<String, ImmutableList<DataRow<EdgarCompanyTickerMapping>>>
    ): ImmutableList<DataRow<EdgarCompanyTickerMapping>> =
        listOfNotNull(
            security.symbol,
            security.cqsSymbol,
            security.nasdaqSymbol
        )
            .flatMap { mappingsByTicker[it].orEmpty() }
            .toPersistentList()

    private fun toListingData(
        security: DataRow<NasdaqTradedSecurity>,
        edgar: DataRow<EdgarCompanyTickerMapping>
    ): SecurityListing =
        SecurityListing.of(
            cik = edgar.cik,
            symbols = SecuritySymbols.of(
                symbol = security.symbol,
                nasdaqSymbol = security.nasdaqSymbol,
                cqsSymbol = security.cqsSymbol,
                edgarSymbol = edgar.ticker
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
                else -> error(
                    "Unknown NASDAQ listing exchange '$exchangeCode' for ${security.symbol}"
                )
            }
        )
}

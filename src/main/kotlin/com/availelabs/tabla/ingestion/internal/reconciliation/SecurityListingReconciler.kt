package com.availelabs.tabla.ingestion.internal.reconciliation

import com.availelabs.tabla.ingestion.internal.providers.edgar.EdgarCompanyTickerMapping
import com.availelabs.tabla.ingestion.internal.providers.edgar.cik
import com.availelabs.tabla.ingestion.internal.providers.edgar.exchange
import com.availelabs.tabla.ingestion.internal.providers.edgar.ticker
import com.availelabs.tabla.ingestion.internal.providers.nasdaq.*
import com.availelabs.tabla.market.Exchange
import com.availelabs.tabla.market.SecurityListing
import com.availelabs.tabla.market.SecuritySymbols
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentHashSet
import kotlinx.collections.immutable.toPersistentMap
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.map
import org.slf4j.LoggerFactory

class SecurityListingReconciler(
    private val nasdaqData: DataFrame<NasdaqTradedSecurity>,
    private val edgarCompanies: DataFrame<EdgarCompanyTickerMapping>
) {
    private val logger = LoggerFactory.getLogger(SecurityListingReconciler::class.java)

    fun reconcile() {
        val edgarCompaniesMap = edgarCompanies.map { it.ticker to it }.toMap().toPersistentMap()
        val nasdaqMap = nasdaqData.map {
            listOfNotNull(it.symbol, it.nasdaqSymbol, it.cqsSymbol).toImmutableSet() to it
        }.toPersistentHashSet()

        val exactPairs = nasdaqMap.mapNotNull { [symbols, nasdaqRow] ->
            val edgarCompanyRow = symbols.firstNotNullOfOrNull(edgarCompaniesMap::get)
                ?: return@mapNotNull null

            nasdaqRow to edgarCompanyRow
        }

        val matches = exactPairs.mapNotNull { [nasdaqData, edgarData] ->
            SecurityListing.of(
                cik = edgarData.cik,
                symbols = SecuritySymbols.of(
                    symbol = nasdaqData.symbol,
                    nasdaqSymbol = nasdaqData.nasdaqSymbol,
                    cqsSymbol = nasdaqData.cqsSymbol,
                    edgarSymbol = edgarData.ticker
                ),
                name = nasdaqData.securityName,
                exchange = when (nasdaqData.listingExchange) {
                    'Q' -> Exchange.NASDAQ
                    'A' -> Exchange.NYSE_AMERICAN
                    'N' -> Exchange.NYSE
                    'P' -> Exchange.NYSE_ARCA
                    'Z' -> Exchange.CBOE_BZX
                    'V' -> Exchange.IEX
                    'F' -> Exchange.TXSE
                    'M' -> Exchange.NYSE_TEXAS
                    else -> {
                        logger.error(
                            "Unknown NASDAQ listing exchange '{}' for {}\nEDGAR exchange '{}' for {}",
                            nasdaqData.listingExchange,
                            nasdaqData.symbol,
                            edgarData.exchange,
                            edgarData.ticker
                        )
                        return@mapNotNull null
                    }
                }
            )
        }
    }
}

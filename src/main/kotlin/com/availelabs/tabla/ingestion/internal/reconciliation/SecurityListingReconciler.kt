package com.availelabs.tabla.ingestion.internal.reconciliation

import com.availelabs.tabla.ingestion.internal.providers.edgar.EdgarCompanyTickerMapping
import com.availelabs.tabla.ingestion.internal.providers.edgar.ticker
import com.availelabs.tabla.ingestion.internal.providers.nasdaq.NasdaqTradedSecurity
import com.availelabs.tabla.ingestion.internal.providers.nasdaq.cqsSymbol
import com.availelabs.tabla.ingestion.internal.providers.nasdaq.nasdaqSymbol
import com.availelabs.tabla.ingestion.internal.providers.nasdaq.symbol
import com.availelabs.tabla.market.SecurityListing
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentHashSet
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.map

class SecurityListingReconciler(
    private val edgarCompanies: DataFrame<EdgarCompanyTickerMapping>,
    private val nasdaqData: DataFrame<NasdaqTradedSecurity>
) {
    fun reconcile(): ImmutableList<SecurityListing> {
        val edgarCompaniesMap = edgarCompanies.map { it.ticker to it }.toPersistentHashSet()
        val nasdaqMap = nasdaqData.map {
            listOfNotNull(it.symbol, it.nasdaqSymbol, it.cqsSymbol).toImmutableSet()
        }.toPersistentHashSet()

        val stockAndEtfsMatches = nasdaqMap.intersect(edgarCompaniesMap)
        TODO()
    }

}
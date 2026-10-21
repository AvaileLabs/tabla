package com.availelabs.tabla.ingestion.reconciliation

import com.availelabs.tabla.ingestion.edgar.EdgarCompanyTickerMapping
import com.availelabs.tabla.ingestion.edgar.ticker
import com.availelabs.tabla.ingestion.nasdaq.NasdaqTradedSecurity
import com.availelabs.tabla.ingestion.nasdaq.cqsSymbol
import com.availelabs.tabla.ingestion.nasdaq.nasdaqSymbol
import com.availelabs.tabla.ingestion.nasdaq.symbol
import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.api.count
import org.jetbrains.kotlinx.dataframe.api.innerJoinWith

class EdgarNasdaqTickerReconciliation {

    fun reconcile(
        nasdaq: Sourced<DataFrame<NasdaqTradedSecurity>>,
        edgar: Sourced<DataFrame<EdgarCompanyTickerMapping>>
    ) {
        val exactMatches = nasdaq.value.innerJoinWith(edgar.value) {
            exact(this, right)
        }

        println("Exact matches: ${exactMatches.count()}")
        println(exactMatches)
    }
}

/* file report that imports aren't suggested automatically by IDE.
import com.availelabs.tabla.ingestion.nasdaq.cqsSymbol
import com.availelabs.tabla.ingestion.nasdaq.nasdaqSymbol
import com.availelabs.tabla.ingestion.nasdaq.symbol
import com.availelabs.tabla.ingestion.edgar.ticker
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

private val rules: ImmutableList<Rule> = persistentListOf(
    exact
)

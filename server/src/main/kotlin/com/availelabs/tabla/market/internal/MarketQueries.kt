package com.availelabs.tabla.market.internal

import com.availelabs.tabla.market.nasdaq.ListingExchange
import com.availelabs.tabla.market.nasdaq.internal.*
import com.availelabs.tabla.market.sec.fund.internal.FundShareClassEntity
import com.availelabs.tabla.market.sec.fund.internal.classId
import com.availelabs.tabla.market.sec.fund.internal.seriesId
import com.availelabs.tabla.market.sec.fund.internal.symbol
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.babyfish.jimmer.sql.kt.ast.expression.*
import org.springframework.stereotype.Service

@Service
class MarketQueries(private val sqlClient: KSqlClient) {
    fun listings(query: String?, limit: Int, etf: Boolean?): ImmutableList<ListingResult> {
        validate(query, limit)
        return sqlClient.createQuery(NasdaqDirectoryEntryEntity::class) {
            where(etf?.let { table.etf eq it })
            where(containsText(query, table.symbol, table.nasdaqSymbol, table.name))
            orderBy(table.nasdaqSymbol)
            select(table)
        }.limit(limit).execute().map {
            ListingResult(it.nasdaqSymbol, it.symbol, it.name, it.exchange, it.etf)
        }.toImmutableList()
    }

    fun funds(query: String?, limit: Int): ImmutableList<FundResult> {
        validate(query, limit)
        return sqlClient.createQuery(FundShareClassEntity::class) {
            where(containsText(query, table.symbol, table.seriesId, table.classId))
            orderBy(table.classId)
            select(table)
        }.limit(limit).execute().map {
            FundResult(it.classId, it.seriesId, it.cik, it.symbol)
        }.toImmutableList()
    }

    private fun validate(query: String?, limit: Int) {
        require(limit in 1..100) { "limit must be between 1 and 100" }
        require(query == null || query.length <= 200) { "query must be at most 200 characters" }
    }
}

value class ListingResult(
    val nasdaqSymbol: String,
    val symbol: String,
    val name: String,
    val exchange: ListingExchange,
    val etf: Boolean
)

value class FundResult(val classId: String, val seriesId: String, val cik: Int, val symbol: String?)

private fun containsText(query: String?, vararg fields: KExpression<String>): KNonNullExpression<Boolean>? =
    query?.trim()?.takeIf { it.isNotEmpty() }?.let { text ->
        or(*fields.map { field ->
            sql(Boolean::class, "strpos(lower(%e), lower(%v)) > 0") {
                expression(field)
                value(text)
            }
        }.toTypedArray())
    }

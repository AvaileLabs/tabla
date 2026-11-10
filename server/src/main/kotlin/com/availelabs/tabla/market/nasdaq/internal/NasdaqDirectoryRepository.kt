package com.availelabs.tabla.market.nasdaq.internal

import com.availelabs.tabla.market.nasdaq.NasdaqDirectoryEntry
import com.availelabs.tabla.market.nasdaq.NasdaqDirectoryWriter
import com.availelabs.tabla.market.nasdaq.internal.dto.NasdaqDirectoryEntryInput
import com.availelabs.tabla.provenance.SourceTimestamp
import com.availelabs.tabla.provenance.Sourced
import kotlinx.collections.immutable.ImmutableList
import org.babyfish.jimmer.sql.ast.mutation.SaveMode
import org.babyfish.jimmer.sql.kt.KSqlClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
class NasdaqDirectoryRepository(
    private val sqlClient: KSqlClient
) : NasdaqDirectoryWriter {
    @Transactional
    override fun replaceAll(
        snapshot: Sourced<ImmutableList<NasdaqDirectoryEntry>>
    ) {
        val entries = snapshot.value
        require(entries.isNotEmpty()) { "NASDAQ directory must not be empty" }

        require(entries.distinctBy { it.symbols.nasdaqSymbol }.size == entries.size) {
            "NASDAQ directory entries must have distinct NASDAQ symbols"
        }

        val source = snapshot.sourceInfo
        val createdAt = source.createdAt
        require(createdAt == null || createdAt is SourceTimestamp.DateTime) {
            "NASDAQ source creation time must be a local date-time"
        }
        val inputs = entries.map { data ->
            NasdaqDirectoryEntryInput(
                symbol = data.symbols.symbol,
                nasdaqSymbol = data.symbols.nasdaqSymbol,
                cqsSymbol = data.symbols.cqsSymbol,
                name = data.name,
                exchange = data.exchange,
                nasdaqTraded = data.nasdaqTraded,
                etf = data.etf,
                roundLotSize = data.roundLotSize,
                marketCategory = data.marketCategory,
                financialStatus = data.financialStatus,
                nextShares = data.nextShares,
                sourceUri = source.uri,
                retrievedAt = source.retrievedAt,
                sourceCreatedAt = createdAt?.value
            )
        }

        sqlClient.createDelete(NasdaqDirectoryEntryEntity::class) {}.execute()
        sqlClient.saveInputs(inputs, SaveMode.INSERT_ONLY)
    }
}

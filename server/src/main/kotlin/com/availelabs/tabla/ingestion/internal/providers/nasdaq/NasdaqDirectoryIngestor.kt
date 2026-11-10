package com.availelabs.tabla.ingestion.internal.providers.nasdaq

import com.availelabs.tabla.ingestion.internal.source.NasdaqDirectoryRawFile
import com.availelabs.tabla.ingestion.internal.source.SourceCatalog
import com.availelabs.tabla.ingestion.internal.source.sourcedBy
import com.availelabs.tabla.market.nasdaq.ListingExchange
import com.availelabs.tabla.market.nasdaq.NasdaqDirectoryEntry
import com.availelabs.tabla.market.nasdaq.NasdaqSymbols
import com.availelabs.tabla.provenance.SourceTimestamp
import com.availelabs.tabla.provenance.Sourced
import com.availelabs.tabla.provenance.sourcedBy
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import kotlinx.io.*
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.springframework.stereotype.Component
import kotlin.time.Clock

@Component
class NasdaqDirectoryIngestor {
    val source = SourceCatalog.nasdaqDirectory

    fun download(): Sourced<NasdaqDirectoryRawFile> {
        val bytes = source.uri.toURL().openStream().asSource().buffered().use {
            it.readByteString()
        }

        return bytes.sourcedBy(
            source = source,
            retrievedAt = Clock.System.now()
        )
    }

    fun parse(file: Sourced<NasdaqDirectoryRawFile>): Sourced<ImmutableList<NasdaqDirectoryEntry>> {
        val rawTable = Buffer().apply { write(file.value.bytes) }.asInputStream().use {
            DataFrame.readCsv(
                inputStream = it,
                delimiter = '|',
                parserOptions = ParserOptions(nullStrings = setOf(""))
            ).cast<NasdaqTradedSecurity>(verify = false)
        }

        val prefix = "File Creation Time:"
        val fileCreationTime = rawTable
            .single { nasdaqTraded.startsWith(prefix) }
            .nasdaqTraded.removePrefix(prefix).trim()
            .let {
                LocalDateTime.Format {
                    monthNumber()
                    day()
                    year()
                    hour()
                    char(':')
                    minute()
                }.parse(it)
            }

        val df = rawTable
            .drop { nasdaqTraded.startsWith(prefix) }
            .cast<NasdaqTestIssueSchema>(verify = true)
            .filter { testIssue == 'N' }
            .remove { testIssue }
            .cast<NasdaqTradedSecurity>(verify = true)

        val listings = df.map { row ->
            NasdaqDirectoryEntry.of(
                symbols = NasdaqSymbols.of(row.symbol, row.nasdaqSymbol, row.cqsSymbol),
                name = row.securityName,
                exchange = when (row.listingExchange) {
                    'Q' -> ListingExchange.NASDAQ
                    'A' -> ListingExchange.NYSE_AMERICAN
                    'N' -> ListingExchange.NYSE
                    'P' -> ListingExchange.NYSE_ARCA
                    'Z' -> ListingExchange.CBOE_BZX
                    'V' -> ListingExchange.IEX
                    'F' -> ListingExchange.TXSE
                    'M' -> ListingExchange.NYSE_TEXAS
                    else -> error("Unknown listing exchange '${row.listingExchange}' for ${row.symbol}")
                },
                nasdaqTraded = row.nasdaqTraded.toFlag(),
                etf = row.etf.toString().toFlag(),
                roundLotSize = row.roundLotSize,
                marketCategory = row.marketCategory,
                financialStatus = row.financialStatus,
                nextShares = row.nextShares.toString().toFlag()
            )
        }.toImmutableList()

        return listings.sourcedBy(
            uri = file.sourceInfo.uri,
            retrievedAt = file.sourceInfo.retrievedAt,
            createdAt = SourceTimestamp.DateTime(fileCreationTime)
        )
    }
}

private fun String.toFlag(): Boolean = when (this) {
    "Y" -> true
    "N" -> false
    else -> error("Unknown NASDAQ flag '$this'")
}

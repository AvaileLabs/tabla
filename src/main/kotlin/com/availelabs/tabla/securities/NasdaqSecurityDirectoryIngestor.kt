package com.availelabs.tabla.securities

import com.availelabs.tabla.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import kotlin.time.Clock
import kotlin.time.Instant

@DataSchema
interface NasdaqTradedSecurity {
    // The footer shares this column, so CSV parsing infers String.
    val `Nasdaq Traded`: String
    val Symbol: String
    val `Security Name`: String
    val `Listing Exchange`: Char
    val `Market Category`: Char?
    val ETF: Char
    val `Round Lot Size`: Int
    val `Test Issue`: Char
    val `Financial Status`: Char?
    val `CQS Symbol`: String?
    val `NASDAQ Symbol`: String
    val NextShares: Char
}

@Component
class NasdaqSecurityDirectoryIngestor {
    private val logger = LoggerFactory.getLogger(NasdaqSecurityDirectoryIngestor::class.java)
    private val nasdaqTradedSecuritiesSource = URI("https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqtraded.txt")

    fun ingest() {
        try {
            val markEmptyFieldsAsNull = ParserOptions(nullStrings = setOf(""))

            val rawNasdaqListing = readListingTable(nasdaqTradedSecuritiesSource, markEmptyFieldsAsNull)
            val nasdaqListing = ParsedListingTable.parse(rawNasdaqListing)
            val etfs = nasdaqListing.toEtfs()

            println(etfs.size)
            val etfTable = dataFrameOf(
                "Ticker" to etfs.map { it.primaryListing.value.symbols },
                "Name" to etfs.map { it.primaryListing.value.name },
                "Exchange" to etfs.map { it.primaryListing.value.exchange.name },
            )
            etfTable.print(rowsLimit = null, valueLimit = null)
        } catch (e: Exception) {
            logger.error("Failed to ingest tickers", e)
            throw e
        }
    }

    private data class RetrievedListingTable(
        val value: DataFrame<NasdaqTradedSecurity>,
        val uri: URI,
        val retrievedAt: Instant
    )

    private class ParsedListingTable private constructor(
        val value: DataFrame<NasdaqTradedSecurity>,
        uri: URI,
        retrievedAt: Instant,
        fileCreationTime: LocalDateTime
    ) {
        val sourceInfo = SourceInfo(
            uri = uri,
            retrievedAt = retrievedAt,
            createdAt = SourceTimestamp.DateTime(fileCreationTime)
        )

        companion object {
            fun parse(rawTable: RetrievedListingTable): ParsedListingTable {
                val (normalizedTable, fileCreationTime) = normalizeListingTable(rawTable.value)

                return ParsedListingTable(
                    value = normalizedTable.cast<NasdaqTradedSecurity>(verify = true),
                    uri = rawTable.uri,
                    retrievedAt = rawTable.retrievedAt,
                    fileCreationTime = fileCreationTime
                )
            }

            private fun normalizeListingTable(
                rawTable: DataFrame<NasdaqTradedSecurity>
            ): Pair<DataFrame<NasdaqTradedSecurity>, LocalDateTime> {
                val prefix = "File Creation Time:"

                val rawFileCreationTime = rawTable
                    .single { `Nasdaq Traded`.startsWith(prefix) }
                    .`Nasdaq Traded`.removePrefix(prefix).trim()
                val fileCreationTime = parseCreationTime(rawFileCreationTime)

                val normalizedTable = rawTable
                    .drop { `Nasdaq Traded`.startsWith(prefix) }
                    .drop { `Test Issue` == 'Y' }

                return normalizedTable to fileCreationTime
            }

            private fun parseCreationTime(creationTime: String): LocalDateTime {
                val format = LocalDateTime.Format {
                    monthNumber()
                    day()
                    year()
                    hour()
                    char(':')
                    minute()
                }

                return format.parse(creationTime)
            }
        }
    }

    private fun readListingTable(source: URI, parserOptions: ParserOptions): RetrievedListingTable {
        val rawTable = DataFrame.readCsv(
            fileOrUrl = source.toString(),
            delimiter = '|',
            parserOptions = parserOptions,
        )
        return RetrievedListingTable(
            value = rawTable.cast<NasdaqTradedSecurity>(verify = false),
            uri = source,
            retrievedAt = Clock.System.now()
        )
    }

    private fun ParsedListingTable.toEtfs(): List<Etf> =
        value.filter { ETF == 'Y' }.map { row ->
            Etf(
                instrumentId = InstrumentId.generate(),
                primaryListing = Listing.of(
                    symbols = SecuritySymbols(
                        symbol = row.Symbol,
                        nasdaqSymbol = row.`NASDAQ Symbol`,
                        cqsSymbol = row.`CQS Symbol`
                    ),
                    name = row.`Security Name`,
                    exchange = exchangeOf(row.`Listing Exchange`)
                ).sourcedBy(sourceInfo),
                incomeHistory = HistoricalData(segments = emptyList())
            )
        }

    private fun exchangeOf(code: Char): Exchange =
        when (code) {
            'A' -> Exchange.NYSE_AMERICAN
            'F' -> Exchange.TXSE
            'M' -> Exchange.NYSE_TEXAS
            'N' -> Exchange.NYSE
            'P' -> Exchange.NYSE_ARCA
            'Q' -> Exchange.NASDAQ
            'Z' -> Exchange.CBOE_BZX
            'V' -> Exchange.IEX
            else -> error("Unsupported exchange code: $code")
        }
}

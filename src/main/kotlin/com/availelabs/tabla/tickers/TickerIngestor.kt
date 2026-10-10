package com.availelabs.tabla.tickers

import com.availelabs.tabla.*
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.DataRow
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.net.URI
import kotlin.time.Clock
import kotlin.time.Instant

// Do not further extend or alter ListingSchema.
@DataSchema
interface ListingSchema {
    val `Security Name`: String
    val `Test Issue`: Char
    val `Round Lot Size`: Int
    val ETF: Char

    @DataSchema
    interface NasdaqListing : ListingSchema {
        val Symbol: String
        val `Market Category`: Char
        val `Financial Status`: Char
        val NextShares: Char
    }

    @DataSchema
    interface OtherUsListing : ListingSchema {
        val `ACT Symbol`: String
        val Exchange: Char
        val `CQS Symbol`: String
        val `NASDAQ Symbol`: String
    }
}

@Component
class TickerIngestor {
    private val logger = LoggerFactory.getLogger(TickerIngestor::class.java)
    private val nasdaqListedSecuritiesSource = URI("https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqlisted.txt")
    private val otherUsListedSecuritiesSource = URI("https://www.nasdaqtrader.com/dynamic/SymDir/otherlisted.txt")

    fun ingest() {
        try {
            val markEmptyFieldsAsNull = ParserOptions(nullStrings = setOf(""))

            val rawNasdaqListing = readListingTable(nasdaqListedSecuritiesSource, markEmptyFieldsAsNull)
            val rawOtherUsListing = readListingTable(otherUsListedSecuritiesSource, markEmptyFieldsAsNull)
            val nasdaqListing = ParsedListingTable.parse<ListingSchema.NasdaqListing>(rawNasdaqListing)
            val otherUsListing = ParsedListingTable.parse<ListingSchema.OtherUsListing>(rawOtherUsListing)

            val etfs = nasdaqListing.toEtfs(
                ticker = { it.Symbol },
                exchange = { Exchange.NASDAQ }
            ) + otherUsListing.toEtfs(
                ticker = { it.`ACT Symbol` },
                exchange = { exchangeOf(it.Exchange) }
            )

            println(etfs.size)
            val etfTable = dataFrameOf(
                "Ticker" to etfs.map { it.primaryListing.value.ticker },
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
        val value: DataFrame<*>,
        val uri: URI,
        val retrievedAt: Instant
    )

    private class ParsedListingTable<T : ListingSchema> private constructor(
        val value: DataFrame<T>,
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
            inline fun <reified T : ListingSchema> parse(rawTable: RetrievedListingTable): ParsedListingTable<T> {
                // Whitelisted schemas; do not add new schemas.
                require(
                    T::class == ListingSchema.NasdaqListing::class || T::class == ListingSchema.OtherUsListing::class
                ) {
                    "Unsupported schema: ${T::class}. Supported schemas are NasdaqListing and OtherUsListing."
                }

                val (normalizedTable, fileCreationTime) = normalizeListingTable(rawTable.value)

                return ParsedListingTable(
                    value = normalizedTable.cast<T>(verify = true),
                    uri = rawTable.uri,
                    retrievedAt = rawTable.retrievedAt,
                    fileCreationTime = fileCreationTime
                )
            }

            private fun normalizeListingTable(rawTable: DataFrame<*>): Pair<DataFrame<*>, LocalDateTime> {
                val testIssueColumn = "Test Issue"

                require(testIssueColumn in rawTable.columnNames()) { "Missing required column: \"$testIssueColumn\"." }

                val prefix = "File Creation Time:"

                val rawFileCreationTime = rawTable
                    .single { row -> row.values().first().toString().startsWith(prefix) }
                    .values().first().toString().removePrefix(prefix).trim()
                val fileCreationTime = parseCreationTime(rawFileCreationTime)

                val normalizedTable = rawTable
                    .drop { row -> row.values().first().toString().startsWith(prefix) }
                    .drop { row -> row[testIssueColumn].toString() == "Y" }

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
            value = rawTable,
            uri = source,
            retrievedAt = Clock.System.now()
        )
    }

    private fun <T : ListingSchema> ParsedListingTable<T>.toEtfs(
        ticker: (DataRow<T>) -> String,
        exchange: (DataRow<T>) -> Exchange
    ): List<Etf> =
        value.filter { ETF == 'Y' }.map { row ->
            Etf(
                instrumentId = InstrumentId.generate(),
                primaryListing = Listing.of(
                    ticker = ticker(row),
                    name = row.`Security Name`,
                    exchange = exchange(row)
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
            'Z' -> Exchange.CBOE_BZX
            'V' -> Exchange.IEX
            else -> error("Unsupported exchange code: $code")
        }
}

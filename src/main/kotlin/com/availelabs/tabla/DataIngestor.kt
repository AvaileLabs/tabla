package com.availelabs.tabla

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory
import java.net.URI
import kotlin.time.Clock

interface ListingSchema {

    @DataSchema
    interface NasdaqListing : ListingSchema {
        val Symbol: String
        val `Security Name`: String
        val `Market Category`: Char
        val `Test Issue`: Char
        val `Financial Status`: Char
        val `Round Lot Size`: Int
        val ETF: Char
        val NextShares: Char
    }

    @DataSchema
    interface OtherUsListing : ListingSchema {
        val `ACT Symbol`: String
        val `Security Name`: String
        val Exchange: Char
        val `CQS Symbol`: String
        val ETF: Char
        val `Round Lot Size`: Int
        val `Test Issue`: Char
        val `NASDAQ Symbol`: String
    }
}

class DataIngestor {
    private val logger = LoggerFactory.getLogger(DataIngestor::class.java)
    private val nasdaqListedSecuritiesSource = URI("https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqlisted.txt")
    private val otherUsListedSecuritiesSource = URI("https://www.nasdaqtrader.com/dynamic/SymDir/otherlisted.txt")

    fun ingestTickers() {
        try {
            val markEmptyFieldsAsNull = ParserOptions(nullStrings = setOf(""))

            val rawNasdaqListing = DataFrame.readCsv(
                fileOrUrl = nasdaqListedSecuritiesSource.toString(),
                delimiter = '|',
                parserOptions = markEmptyFieldsAsNull,
            )
            val nasdaqListingsRetrievedAt = Clock.System.now()

            val rawOtherUsListing = DataFrame.readCsv(
                fileOrUrl = otherUsListedSecuritiesSource.toString(),
                delimiter = '|',
                parserOptions = markEmptyFieldsAsNull,
            )
            val otherUsListingsRetrievedAt = Clock.System.now()

            val (nasdaqListing, nasdaqFileCreationTime) =
                parseListingTable<ListingSchema.NasdaqListing>(rawNasdaqListing)
            val (otherUsListing, otherUsFileCreationTime) =
                parseListingTable<ListingSchema.OtherUsListing>(rawOtherUsListing)

            val etfs = mutableListOf<Etf>()

            nasdaqListing
                .filter { ETF == 'Y' }
                .forEach { row ->
                    etfs.add(
                        Etf(
                            instrumentId = InstrumentId.generate(),
                            primaryListing = Listing.of(
                                ticker = row.Symbol,
                                exchange = Exchange.NASDAQ
                            ).sourcedBy(
                                sourceInfo = SourceInfo(
                                    uri = nasdaqListedSecuritiesSource,
                                    retrievedAt = nasdaqListingsRetrievedAt,
                                    createdAt = SourceTimestamp.DateTime(nasdaqFileCreationTime)
                                )
                            ),
                            incomeHistory = HistoricalData(segments = emptyList())
                        )
                    )
                }

            otherUsListing
                .filter { ETF == 'Y' }
                .forEach { row ->
                    etfs.add(
                        Etf(
                            instrumentId = InstrumentId.generate(),
                            primaryListing = Listing.of(
                                ticker = row.`ACT Symbol`,
                                exchange = exchangeOf(row.Exchange)
                            ).sourcedBy(
                                sourceInfo = SourceInfo(
                                    uri = otherUsListedSecuritiesSource,
                                    retrievedAt = otherUsListingsRetrievedAt,
                                    createdAt = SourceTimestamp.DateTime(otherUsFileCreationTime)
                                )
                            ),
                            incomeHistory = HistoricalData(segments = emptyList())
                        )
                    )
                }

            print(etfs)
        } catch (e: Exception) {
            logger.error("Failed to ingest tickers", e)
            throw e
        }
    }

    private inline fun <reified T : ListingSchema> parseListingTable(rawTable: DataFrame<*>): Pair<DataFrame<T>, LocalDateTime> {
        // Whitelisted schemas; do not add a new schema unless it has a compatible structure, which is unlikely.
        val testIssueColumn = "Test Issue"

        require(
            T::class == ListingSchema.NasdaqListing::class || T::class == ListingSchema.OtherUsListing::class
        ) {
            "Unsupported schema: ${T::class}. Supported schemas are NasdaqListing and OtherUsListing."
        }
        require(testIssueColumn in rawTable.columnNames()) { "Missing required column: \"$testIssueColumn\"." }

        val prefix = "File Creation Time:"
        val rawFileCreationTime = rawTable
            .single { row -> row.values().first().toString().startsWith(prefix) }
            .values().first().toString().removePrefix(prefix).trim()
        val fileCreationTime = parseCreationTime(rawFileCreationTime)

        val normalizedTable = rawTable
            .drop { row -> row.values().first().toString().startsWith(prefix) }
            .drop { row -> row[testIssueColumn].toString() == "Y" }
            .cast<T>(verify = true)

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

fun main() {
    DataIngestor().ingestTickers()
}

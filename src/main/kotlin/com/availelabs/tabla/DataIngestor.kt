package com.availelabs.tabla

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

@Component
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
                                name = row.`Security Name`,
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
                                name = row.`Security Name`,
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

    private inline fun <reified T : ListingSchema> parseListingTable(rawTable: DataFrame<*>): Pair<DataFrame<T>, LocalDateTime> {
        // Whitelisted schemas; do not add a new schema unless it has a compatible structure, which is unlikely.
        require(
            T::class == ListingSchema.NasdaqListing::class || T::class == ListingSchema.OtherUsListing::class
        ) {
            "Unsupported schema: ${T::class}. Supported schemas are NasdaqListing and OtherUsListing."
        }

        val (normalizedTable, fileCreationTime) = normalizeListingTable(rawTable)

        return normalizedTable.cast<T>(verify = true) to fileCreationTime
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

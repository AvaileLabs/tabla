package com.availelabs.tabla

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.ParserOptions
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.drop
import org.jetbrains.kotlinx.dataframe.api.single
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory

@DataSchema
interface NasdaqListing {
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
interface OtherUsListing {
    val `ACT Symbol`: String
    val `Security Name`: String
    val Exchange: Char
    val `CQS Symbol`: String
    val ETF: Char
    val `Round Lot Size`: Int
    val `Test Issue`: Char
    val `NASDAQ Symbol`: String
}

class DataIngestor {
    private val logger = LoggerFactory.getLogger(DataIngestor::class.java)
    private val nasdaqListedSecuritiesSource = "https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqlisted.txt"
    private val otherUsListedSecuritiesSource = "https://www.nasdaqtrader.com/dynamic/SymDir/otherlisted.txt"

    fun ingestTickers() {
        try {
            val markEmptyFieldsAsNull = ParserOptions(nullStrings = setOf(""))

            val rawNasdaqListing = DataFrame.readCsv(
                fileOrUrl = nasdaqListedSecuritiesSource,
                delimiter = '|',
                parserOptions = markEmptyFieldsAsNull,
            )

            val rawOtherUsListing = DataFrame.readCsv(
                fileOrUrl = otherUsListedSecuritiesSource,
                delimiter = '|',
                parserOptions = markEmptyFieldsAsNull,
            )

            val (nasdaqListing, nasdaqFileCreationTime) = parseListingTable<NasdaqListing>(rawNasdaqListing)
            val (otherUsListing, otherUsFileCreationTime) = parseListingTable<OtherUsListing>(rawOtherUsListing)

            val etfs = mutableListOf<Etf>()
        } catch (e: Exception) {
            logger.error("Failed to ingest tickers", e)
        }
    }

    private inline fun <reified T> parseListingTable(rawTable: DataFrame<*>): Pair<DataFrame<T>, LocalDateTime> {
        require(
            T::class == NasdaqListing::class || T::class == OtherUsListing::class
        ) {
            "Unsupported schema: ${T::class}. Supported schemas are NasdaqListing and OtherUsListing."
        }

        val prefix = "File Creation Time:"
        val rawFileCreationTime = rawTable
            .single { row -> row.values().first().toString().startsWith(prefix) }
            .values().first().toString().removePrefix(prefix).trim()
        val fileCreationTime = parseCreationTime(rawFileCreationTime)

        val normalizedTable = rawTable
            .drop { row -> row.values().first().toString().startsWith(prefix) }

        val verifiedTable = normalizedTable.cast<T>(verify = true)

        return verifiedTable to fileCreationTime
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

fun main() {
    DataIngestor().ingestTickers()
}

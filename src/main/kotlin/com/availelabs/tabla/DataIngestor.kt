package com.availelabs.tabla

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

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

            val rawNasdaqListings = DataFrame.readCsv(
                fileOrUrl = nasdaqListedSecuritiesSource,
                delimiter = '|',
                parserOptions = markEmptyFieldsAsNull,
            )

            val nasdaqListings = rawNasdaqListings
                .drop { row -> row[0].toString().startsWith("File Creation Time:") }
                .cast<NasdaqListing>(verify = true)

            val otherUsListings = DataFrame.readCsv(
                fileOrUrl = otherUsListedSecuritiesSource,
                delimiter = '|',
                parserOptions = markEmptyFieldsAsNull,
            )
                .drop { row -> row[0].toString().startsWith("File Creation Time:") }
                .cast<OtherUsListing>(verify = true)

            nasdaqListings.filter { Symbol == "NA" }
                .print(rowsLimit = null, valueLimit = null)

        } catch (e: Exception) {
            logger.error("Failed to ingest tickers", e)
        }
    }

    private fun parseListingTable(rawTable: DataFrame<*>, schema: KClass<*>): Pair<DataFrame<*>, String> {
        require(
            schema == NasdaqListing::class || schema == OtherUsListing::class
        ) {
            "Unsupported schema: $schema. Supported schemas are NasdaqListing and OtherUsListing."
        }

        val prefix = "File Creation Time:"
        val fileCreationTime = rawTable
            .single { row -> row.values().first().toString().startsWith(prefix) }
            .values().first().toString().removePrefix(prefix).trim()

        val normalizedTable = rawTable
            .drop { row -> row.values().first().toString().startsWith(prefix) }

        val verifiedTable = when (schema) {
            NasdaqListing::class ->
                normalizedTable.cast<NasdaqListing>(verify = true)

            OtherUsListing::class ->
                normalizedTable.cast<OtherUsListing>(verify = true)

            else ->
                error("Unsupported schema: $schema")
        }

        return verifiedTable to fileCreationTime
    }
}

fun main() {
    DataIngestor().ingestTickers()
}

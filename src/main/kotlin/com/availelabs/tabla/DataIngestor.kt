package com.availelabs.tabla

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.ParserOptions
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.api.filter
import org.jetbrains.kotlinx.dataframe.api.print
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory

@DataSchema
interface NasdaqListing {
    val Symbol: String
    val `Security Name`: String?
    val `Market Category`: Char?
    val `Test Issue`: Char?
    val `Financial Status`: Char?
    val `Round Lot Size`: Int?
    val ETF: Char?
    val NextShares: Char?
}

@DataSchema
interface OtherUsListing {
    val `ACT Symbol`: String
    val `Security Name`: String?
    val Exchange: Char?
    val `CQS Symbol`: String?
    val ETF: Char?
    val `Round Lot Size`: Int?
    val `Test Issue`: Char?
    val `NASDAQ Symbol`: String?
}

class DataIngestor {
    private val logger = LoggerFactory.getLogger(DataIngestor::class.java)
    private val nasdaqListedSecuritiesSource = "https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqlisted.txt"
    private val otherUsListedSecuritiesSource = "https://www.nasdaqtrader.com/dynamic/SymDir/otherlisted.txt"

    fun ingestTickers() {
        try {
            val markEmptyFieldsAsNull = ParserOptions(nullStrings = setOf(""))

            val nasdaqListings = DataFrame.readCsv(
                fileOrUrl = nasdaqListedSecuritiesSource,
                delimiter = '|',
                parserOptions = markEmptyFieldsAsNull,
            )
                .cast<NasdaqListing>(verify = true)

            val otherUsListings = DataFrame.readCsv(
                fileOrUrl = otherUsListedSecuritiesSource,
                delimiter = '|',
                parserOptions = markEmptyFieldsAsNull,
            )
                .cast<OtherUsListing>(verify = true)

            nasdaqListings.filter { Symbol == "NA" }
                .print(rowsLimit = null, valueLimit = null)

        } catch (e: Exception) {
            logger.error("Failed to ingest tickers", e)
        }
    }
}

fun main() {
    DataIngestor().ingestTickers()
}

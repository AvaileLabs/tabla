package com.availelabs.tabla

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory

@DataSchema
interface NasdaqListing {
    val Symbol: String?
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
            val nasdaqListings = DataFrame.readCsv(nasdaqListedSecuritiesSource, delimiter = '|')
                .cast<NasdaqListing>(verify = true)
            val otherUsListings = DataFrame.readCsv(otherUsListedSecuritiesSource, delimiter = '|')
                .cast<OtherUsListing>(verify = true)

            println(nasdaqListings.Symbol)
            println(otherUsListings.`Security Name`)
            println(nasdaqListings)
        } catch (e: Exception) {
            logger.error("Failed to ingest tickers", e)
        }
    }
}

fun main() {
    DataIngestor().ingestTickers()
}

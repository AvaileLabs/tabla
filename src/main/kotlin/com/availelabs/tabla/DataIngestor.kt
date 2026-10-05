package com.availelabs.tabla

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory

class DataIngestor {
    private val logger = LoggerFactory.getLogger(DataIngestor::class.java)
    private val nasdaqListedSecuritiesSource = "https://www.nasdaqtrader.com/dynamic/SymDir/nasdaqlisted.txt"
    private val otherUsListedSecuritiesSource = "https://www.nasdaqtrader.com/dynamic/SymDir/otherlisted.txt"

    fun ingestTickers() {
        try {
            val nasdaqListings = DataFrame.readCsv(nasdaqListedSecuritiesSource, delimiter = '|')
            val otherUsListings = DataFrame.readCsv(otherUsListedSecuritiesSource, delimiter = '|')

            println(nasdaqListings)
            print(otherUsListings)
        } catch (e: Exception) {
            logger.error("Failed to ingest tickers", e)
        }
    }
}

fun main() {
    DataIngestor().ingestTickers()
}
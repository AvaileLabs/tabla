package com.availelabs.tabla.edgar

import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.ParserOptions
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.io.ColType
import org.jetbrains.kotlinx.dataframe.io.Compression
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component

@Component
class EdgarIngestor(
    private val properties: EdgarProperties
) {
    fun ingest(quarter: EdgarQuarter): DataFrame<EdgarMasterIndexEntry> {
        val connection = quarter.indexUri.toURL().openConnection().apply {
            setRequestProperty(HttpHeaders.USER_AGENT, properties.userAgent)
        }

        val df = connection.getInputStream().use { input ->
            DataFrame.readCsv(
                inputStream = input,
                compression = Compression.Gzip,
                delimiter = '|',
                // Skip the preamble before the header, then the separator after it.
                skipLines = 9,
                adjustCsvSpecs = { it.skipRows(1) },
                colTypes = mapOf(
                    ColType.DEFAULT to ColType.String,
                    "Date Filed" to ColType.LocalDate
                ),
                quote = '\u0000',
                parserOptions = ParserOptions(nullStrings = setOf(""))
            ).cast<EdgarMasterIndexEntry>(verify = true)
        }

        TODO("Map tickers to CIK before processing the index.")
    }
}

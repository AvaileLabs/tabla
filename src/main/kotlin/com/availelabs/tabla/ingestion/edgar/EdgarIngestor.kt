package com.availelabs.tabla.ingestion.edgar

import com.availelabs.tabla.provenance.Sourced
import com.availelabs.tabla.provenance.sourcedBy
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.ParserOptions
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.io.ColType
import org.jetbrains.kotlinx.dataframe.io.Compression
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.springframework.http.HttpHeaders
import kotlin.time.Clock

class EdgarIngestor(
    private val properties: EdgarProperties
) {
    fun ingest(quarter: EdgarQuarter): Sourced<DataFrame<EdgarMasterIndexEntry>> {
        val connection = quarter.indexUri.toURL().openConnection().apply {
            setRequestProperty(HttpHeaders.USER_AGENT, properties.userAgent)
        }

        return connection.getInputStream().use { input ->
            DataFrame.readCsv(
                inputStream = input,
                compression = Compression.Gzip,
                delimiter = '|',
                skipLines = 9,
                adjustCsvSpecs = { it.skipRows(1) },
                colTypes = mapOf(
                    ColType.DEFAULT to ColType.String,
                    "Date Filed" to ColType.LocalDate
                ),
                quote = '\u0000',
                parserOptions = ParserOptions(nullStrings = setOf(""))
            ).cast<EdgarMasterIndexEntry>(verify = true)
                .sourcedBy(uri = quarter.indexUri, retrievedAt = Clock.System.now(), createdAt = null)
        }
    }
}

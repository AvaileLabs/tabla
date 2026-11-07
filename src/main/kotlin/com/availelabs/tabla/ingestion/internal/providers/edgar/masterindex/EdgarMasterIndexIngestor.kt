package com.availelabs.tabla.ingestion.internal.providers.edgar.masterindex

import com.availelabs.tabla.ingestion.internal.providers.edgar.EdgarClient
import com.availelabs.tabla.provenance.Sourced
import com.availelabs.tabla.provenance.sourcedBy
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.ParserOptions
import org.jetbrains.kotlinx.dataframe.api.cast
import org.jetbrains.kotlinx.dataframe.io.ColType
import org.jetbrains.kotlinx.dataframe.io.Compression
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.springframework.stereotype.Component
import kotlin.time.Clock

@Component
class EdgarMasterIndexIngestor(
    private val edgarClient: EdgarClient
) {
    fun ingest(quarter: EdgarQuarter): Sourced<DataFrame<EdgarMasterIndexEntry>> =
        edgarClient.withResponseBody(quarter.indexUri) { input ->
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

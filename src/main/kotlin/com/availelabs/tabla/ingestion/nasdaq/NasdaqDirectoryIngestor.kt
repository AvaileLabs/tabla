package com.availelabs.tabla.ingestion.nasdaq

import com.availelabs.tabla.ingestion.source.NasdaqDirectoryRawFile
import com.availelabs.tabla.ingestion.source.SourceCatalog
import com.availelabs.tabla.ingestion.source.sourcedBy
import com.availelabs.tabla.provenance.SourceTimestamp
import com.availelabs.tabla.provenance.Sourced
import com.availelabs.tabla.provenance.sourcedBy
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import kotlinx.io.Buffer
import kotlinx.io.asInputStream
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.io.readByteString
import kotlinx.io.write
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.springframework.stereotype.Component
import kotlin.time.Clock

@Component
class NasdaqDirectoryIngestor {
    val source = SourceCatalog.nasdaqDirectory

    fun download(): Sourced<NasdaqDirectoryRawFile> {
        val bytes = source.uri.toURL().openStream().asSource().buffered().use {
            it.readByteString()
        }

        return bytes.sourcedBy(
            source = source,
            retrievedAt = Clock.System.now()
        )
    }

    fun parse(file: Sourced<NasdaqDirectoryRawFile>): Sourced<DataFrame<NasdaqTradedSecurity>> {
        val rawTable = Buffer().apply { write(file.value.bytes) }.asInputStream().use {
            DataFrame.readCsv(
                inputStream = it,
                delimiter = '|',
                parserOptions = ParserOptions(nullStrings = setOf(""))
            ).cast<NasdaqTradedSecurity>(verify = false)
        }

        val prefix = "File Creation Time:"
        val fileCreationTime = rawTable
            .single { nasdaqTraded.startsWith(prefix) }
            .nasdaqTraded.removePrefix(prefix).trim()
            .let {
                LocalDateTime.Format {
                    monthNumber()
                    day()
                    year()
                    hour()
                    char(':')
                    minute()
                }.parse(it)
            }

        val df = rawTable
            .drop { nasdaqTraded.startsWith(prefix) }
            .cast<NasdaqTestIssueSchema>(verify = true)
            .filter { testIssue == 'N' }
            .remove { testIssue }
            .cast<NasdaqTradedSecurity>(verify = true)

        return df.sourcedBy(
            uri = file.sourceInfo.uri,
            retrievedAt = file.sourceInfo.retrievedAt,
            createdAt = SourceTimestamp.DateTime(fileCreationTime)
        )
    }
}

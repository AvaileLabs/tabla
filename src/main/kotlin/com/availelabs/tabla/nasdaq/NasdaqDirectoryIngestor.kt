package com.availelabs.tabla.nasdaq

import com.availelabs.tabla.SourceCatalog
import com.availelabs.tabla.SourceTimestamp
import com.availelabs.tabla.Sourced
import com.availelabs.tabla.sourcedBy
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.char
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.*
import org.jetbrains.kotlinx.dataframe.io.readCsv
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream
import kotlin.time.Clock

@Component
class NasdaqDirectoryIngestor {
    private val logger = LoggerFactory.getLogger(NasdaqDirectoryIngestor::class.java)
    val sourceUri = SourceCatalog.nasdaqDirectory

    fun download(): Sourced<ByteArray> {
        val bytes = sourceUri.toURL().openStream().use {
            it.readBytes()
        }

        return bytes.sourcedBy(
            uri = sourceUri,
            retrievedAt = Clock.System.now()
        )
    }

    fun parse(file: Sourced<ByteArray>): Sourced<DataFrame<NasdaqTradedSecurity>> {
        val rawTable = DataFrame.readCsv(
            inputStream = ByteArrayInputStream(file.value),
            delimiter = '|',
            parserOptions = ParserOptions(nullStrings = setOf(""))
        ).cast<NasdaqTradedSecurity>(verify = false)

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
            .drop { testIssue != 'N' }
            .remove { testIssue }
            .cast<NasdaqTradedSecurity>(verify = true)

        return df.sourcedBy(
            uri = file.sourceInfo.uri,
            retrievedAt = file.sourceInfo.retrievedAt,
            createdAt = SourceTimestamp.DateTime(fileCreationTime)
        )
    }
}
